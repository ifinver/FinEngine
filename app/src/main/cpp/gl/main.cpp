#include "main.h"
#include "utils.h"
#include <android/native_window_jni.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_myopengles_GLNative_createGLContext(JNIEnv *env, jclass, jobject jSurface) {
    GLContextHolder *pHolder = newGLContext(env, jSurface);
    if (pHolder == NULL) {
        return 0;
    }
    return (jlong) pHolder;
}

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_releaseGLContext(JNIEnv *, jclass, jlong nativeContext) {
    releaseGLContext((GLContextHolder *) nativeContext);
}

JNIEXPORT void JNICALL
Java_com_ifinver_myopengles_GLNative_renderOnContext(JNIEnv *env, jclass, jlong nativeGlContext, jbyteArray data_, jint frameWidth,
                                                     jint frameHeight, jint imageFormat) {
    jbyte *data = env->GetByteArrayElements(data_, 0);

    switch (imageFormat) {
        case 0x11://ImageFormat.NV21
            renderFrame((GLContextHolder *) nativeGlContext, data, frameWidth, frameHeight);
            break;
        default:
            LOGE("不支持的视频编码格式！");
            break;
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}
//.........................................................................................................................

void renderFrame(GLContextHolder *holder, jbyte *data, jint width, jint height) {
    glUseProgram(holder->program);
    glClear(GL_COLOR_BUFFER_BIT);

    /**
     * 输入定点坐标
     */
    glEnableVertexAttribArray(holder->positions[0]);
    glVertexAttribPointer(holder->positions[0], 2, GL_FLOAT, GL_FALSE, 4* sizeof(GLfloat),VERTICES_BASE);

    /**
     * 输入纹理坐标
     */
    glEnableVertexAttribArray(holder->positions[1]);
    glVertexAttribPointer(holder->positions[1], 2, GL_FLOAT, GL_FALSE, 4* sizeof(GLfloat),VERTICES_BASE + 2);

    /**
     * Y texture
     */
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, holder->textures[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, data);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    /**
     * uv texture
     */
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, holder->textures[1]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, data + (width * height));
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glUniform1i(holder->positions[2], 0);
    glUniform1i(holder->positions[3], 1);

    glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,INDICES_BASE);

    glDisableVertexAttribArray(holder->positions[0]);
    glDisableVertexAttribArray(holder->positions[1]);

    eglSwapBuffers(holder->eglDisplay, holder->eglSurface);

}

//释放指定上下文
void releaseGLContext(GLContextHolder *holder) {
    glDeleteTextures(holder->textureNums, holder->textures);
    glDeleteProgram(holder->program);
    eglDestroySurface(holder->eglDisplay, holder->eglSurface);
    eglDestroyContext(holder->eglDisplay, holder->eglContext);
    delete[](holder->positions);
    delete[](holder->textures);
    delete (holder);
}

//创建一个新的绘制上下文
GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface) {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        checkGlError("eglGetDisplay");
        return NULL;
    }
    EGLint majorVer, minVer;
    if (!eglInitialize(display, &majorVer, &minVer)) {
        checkGlError("eglInitialize");
        LOGE("eglInitialize");
        return NULL;
    }
    LOGI("majorVer=%d,minVer=%d", majorVer, minVer);
    // EGL attributes
    const EGLint confAttr[] =
            {
                    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,// very important!
                    EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
                    EGL_RED_SIZE, 8,
                    EGL_GREEN_SIZE, 8,
                    EGL_BLUE_SIZE, 8,
                    EGL_ALPHA_SIZE, 8,// if you need the alpha channel
                    EGL_DEPTH_SIZE, 0,// if you need the depth buffer
                    EGL_STENCIL_SIZE, 0,
                    EGL_NONE
            };

    EGLConfig config;
    EGLint numConfigs;
    if (!eglChooseConfig(display, confAttr, &config, 1, &numConfigs)) {
        checkGlError("eglChooseConfig");
        return NULL;
    }
    ANativeWindow *surface = ANativeWindow_fromSurface(env, jSurface);
    EGLSurface eglSurface = eglCreateWindowSurface(display, config, surface, NULL);
    if (surface == EGL_NO_SURFACE) {
        checkGlError("eglCreateWindowSurface");
        return NULL;
    }
    EGLint attrib_list[] =
            {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };
    EGLContext eglContext = eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    if (eglContext == EGL_NO_CONTEXT) {
        checkGlError("eglCreateContext");
        return NULL;
    }

    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return NULL;
    }

    //context create success,now create program
    ShaderYuv shaderYuv;
    GLuint programYUV = createProgram(shaderYuv.vertexShader, shaderYuv.fragmentShader);
    if (programYUV == 0) {
        return NULL;
    }

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    //success
    GLContextHolder *gl_holder = new GLContextHolder();
    gl_holder->eglDisplay = display;
    gl_holder->eglContext = eglContext;
    gl_holder->eglSurface = eglSurface;
    gl_holder->program = programYUV;

    GLint posAttrVertices = glGetAttribLocation(programYUV, "aPosition");
    GLint posAttrTexCoords = glGetAttribLocation(programYUV, "aTexCoord");
    GLint posUniYTexture = glGetUniformLocation(programYUV, "yTexture");
    GLint posUniUvTexture = glGetUniformLocation(programYUV, "uvTexture");
    GLuint *positions = new GLuint[4];
    positions[0] = (GLuint) posAttrVertices;
    positions[1] = (GLuint) posAttrTexCoords;
    positions[2] = (GLuint) posUniYTexture;
    positions[3] = (GLuint) posUniUvTexture;
    gl_holder->positions = positions;
//    LOGE("posAttrVertices=%d,posAttrTexCoords=%d,posUniYTexture=%d,posUniUvTexture=%d",posAttrVertices,posAttrTexCoords,posUniYTexture,posUniUvTexture);

    GLuint *textures = new GLuint[2];
    glGenTextures(2, textures);
    gl_holder->textureNums = 2;
    gl_holder->textures = textures;

    return gl_holder;
}