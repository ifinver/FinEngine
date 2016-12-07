#include "main.h"
#include "utils.h"
#include <math.h>
#include <GLES2/gl2ext.h>
#include <android/native_window_jni.h>

GLContextHolder *engineHolder = NULL;

JNIEXPORT jboolean JNICALL
Java_com_ifinver_finengine_FinEngine_nativeInit(JNIEnv *env, jclass, jobject jSurface) {
    releaseGLContext();
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        checkGlError("eglGetDisplay");
        return JNI_FALSE;
    }
    EGLint majorVer, minVer;
    if (!eglInitialize(display, &majorVer, &minVer)) {
        checkGlError("eglInitialize");
        LOGE("eglInitialize");
        return JNI_FALSE;
    }
    // EGL attributes
    const EGLint confAttr[] =
            {
                    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,// very important!
                    EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
                    EGL_RED_SIZE, 8,
                    EGL_GREEN_SIZE, 8,
                    EGL_BLUE_SIZE, 8,
                    EGL_ALPHA_SIZE, 8,
                    EGL_DEPTH_SIZE, 0,
                    EGL_STENCIL_SIZE, 0,
                    EGL_NONE
            };

    EGLConfig config;
    EGLint numConfigs;
    if (!eglChooseConfig(display, confAttr, &config, 1, &numConfigs)) {
        checkGlError("eglChooseConfig");
        return JNI_FALSE;
    }
    ANativeWindow *surface = ANativeWindow_fromSurface(env, jSurface);
    EGLSurface eglSurface = eglCreateWindowSurface(display, config, surface, NULL);
    if (surface == EGL_NO_SURFACE) {
        checkGlError("eglCreateWindowSurface");
        return JNI_FALSE;
    }
    EGLint attrib_list[] =
            {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };
    EGLContext eglContext = eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    if (eglContext == EGL_NO_CONTEXT) {
        checkGlError("eglCreateContext");
        return JNI_FALSE;
    }


    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return JNI_FALSE;
    }

    //context create success,now create program
    ShaderBase shader = ShaderNV21();
    GLuint programYUV = createProgram(shader.vertexShader, shader.fragmentShader);
//    delete shader;
    if (programYUV == 0) {
        return JNI_FALSE;
    }

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    engineHolder = new GLContextHolder();
    //success
    engineHolder->eglDisplay = display;
    engineHolder->eglContext = eglContext;
    engineHolder->eglSurface = eglSurface;
    engineHolder->program = programYUV;

    engineHolder->posAttrVertices = (GLuint) glGetAttribLocation(programYUV, "aPosition");
    engineHolder->posAttrTexCoords = (GLuint) glGetAttribLocation(programYUV, "aTexCoord");
    engineHolder->posAttrScaleX = (GLuint) glGetAttribLocation(programYUV, "aScaleX");
    engineHolder->posAttrScaleY = (GLuint) glGetAttribLocation(programYUV, "aScaleY");
    engineHolder->posUniTextureY = (GLuint) glGetUniformLocation(programYUV, "yTexture");
    engineHolder->posUniTextureUV = (GLuint) glGetUniformLocation(programYUV, "uvTexture");

    //tex
    GLuint *textures = new GLuint[2];
    glGenTextures(2, textures);
    glBindTexture(GL_TEXTURE_2D, textures[0]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glBindTexture(GL_TEXTURE_2D, textures[1]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    engineHolder->textureNums = 2;
    engineHolder->textures = textures;

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);

    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeRelease(JNIEnv *, jclass) {
    releaseGLContext();
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEngine_nativeRender(JNIEnv *env, jclass, jbyteArray data_, jint frameWidth, jint frameHeight,
                                                  jint degree, jboolean mirror, jint outWidth, jint outHeight) {
    jbyte *data = env->GetByteArrayElements(data_, 0);

    renderFrame(data, frameWidth, frameHeight, degree, mirror, outWidth, outHeight);

    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
}

//.........................................................................................................................
void renderFrame(jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth, jint outHeight) {

    glUseProgram(engineHolder->program);

//    glViewport(0, 0, outWidth, outHeight);

    //输入顶点
    glEnableVertexAttribArray(engineHolder->posAttrVertices);
    glVertexAttribPointer(engineHolder->posAttrVertices, 2, GL_FLOAT, GL_FALSE, 0, VERTICES_COORD);

    //输入纹理坐标，处理旋转和镜像
    glEnableVertexAttribArray(engineHolder->posAttrTexCoords);
    {
        degree %= 360;
        if (degree < 0) degree += 360;
        int idx;
        if (mirror) {
            idx = degree / 90 * 2;
            glVertexAttribPointer(engineHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD_MIRROR + idx);
        } else {
            degree = 360 - degree;
            idx = degree / 90 * 2;
            glVertexAttribPointer(engineHolder->posAttrTexCoords, 2, GL_FLOAT, GL_FALSE, 0, TEXTURE_COORD_NOR + idx);
        }
    }

    //上传纹理 Y通道
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, engineHolder->textures[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, data);
    glUniform1i(engineHolder->posUniTextureY, 0);

    //上传UV通道
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, engineHolder->textures[1]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, data + (width * height));
    glUniform1i(engineHolder->posUniTextureUV, 1);

    jint odd = degree / 90;
    if (odd == 1 || odd == 3) {
        //如果旋转了90°，交换长和宽
        int temp = width;
        width = height;
        height = temp;
    }

    float fixWidth,fixHeight;
    if((float)width / height >= (float)outWidth / outHeight){
        fixHeight = height;
        fixWidth = (float)height / outHeight * outWidth;
    }else{
        fixWidth = width;
        fixHeight = (float)width / outWidth * outHeight;
    }

    float scaleX =  width/fixWidth;
    float scaleY =  height/fixHeight;

    glVertexAttrib1f(engineHolder->posAttrScaleX, scaleX);
    glVertexAttrib1f(engineHolder->posAttrScaleY, scaleY);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(engineHolder->posAttrVertices);
    glDisableVertexAttribArray(engineHolder->posAttrTexCoords);

//    glFinish();
    eglSwapBuffers(engineHolder->eglDisplay, engineHolder->eglSurface);
}

//释放指定上下文
void releaseGLContext() {
    if (engineHolder != NULL) {
        glDeleteTextures(engineHolder->textureNums, engineHolder->textures);
        glDeleteProgram(engineHolder->program);
        eglDestroySurface(engineHolder->eglDisplay, engineHolder->eglSurface);
        eglDestroyContext(engineHolder->eglDisplay, engineHolder->eglContext);
        delete (engineHolder);
        engineHolder = NULL;
    }

}