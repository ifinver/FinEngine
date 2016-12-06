#include "main.h"
#include "utils.h"
#include <math.h>
#include <GLES2/gl2ext.h>
#include <android/native_window_jni.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_finrender_FinRender_createGLContext(JNIEnv *env, jclass, jobject jSurface, jboolean isSurfaceThreadExclusive,int frameFormat) {
    GLContextHolder *pHolder = NULL;

    pHolder = newGLContext(env, jSurface, isSurfaceThreadExclusive);

    if (pHolder == NULL) {
        return 0;
    }
    return (jlong) pHolder;
}

JNIEXPORT void JNICALL
Java_com_ifinver_finrender_FinRender_releaseGLContext(JNIEnv *, jclass, jlong nativeContext) {
    releaseGLContext((GLContextHolder *) nativeContext);
}

JNIEXPORT void JNICALL
Java_com_ifinver_finrender_FinRender_renderOnContext(JNIEnv *env, jclass, jlong nativeGlContext, jbyteArray data_,jint frameWidth,jint frameHeight,jint degree,jboolean mirror) {
//    jboolean isCopy;
    jbyte *data = env->GetByteArrayElements(data_, 0);
//    if (isCopy) {
//        LOGI("isCopy=true");
//    }else{
//        LOGI("isCopy=false");
//    }

    renderFrame((GLContextHolder *) nativeGlContext, data,frameWidth, frameHeight,degree,mirror);

//    if (isCopy) {
        env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
//    }
}
//.........................................................................................................................

void renderFrame(GLContextHolder *holder, jbyte *data ,jint width, jint height,jint degree,jboolean mirror) {
    if(!holder->isSurfaceThreadExclusive) {
        if (!eglMakeCurrent(holder->eglDisplay, holder->eglSurface, holder->eglSurface, holder->eglContext)) {
            LOGE("make current failed!!! [当前surface是多个线程共享的]");
            checkGlError("eglMakeCurrent");
            return ;
        }
    }
    /**
     * 输入定点坐标
     */
    glEnableVertexAttribArray(holder->positions[0]);
    glVertexAttribPointer(holder->positions[0], 2, GL_FLOAT, GL_FALSE, holder->vertexStride, (const void *) holder->offsetVertex);

    /**
     * 输入纹理坐标
     */
    glEnableVertexAttribArray(holder->positions[1]);
    glVertexAttribPointer(holder->positions[1], 2, GL_FLOAT, GL_FALSE, holder->texStride, (const void *) holder->offsetTex);

    /**
     * rgb texture
     */
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, holder->textures[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glUniform1i(holder->positions[2], 0);

    //no need concern this.
    //rotation
//    if (holder->frameDegree != frameDegree) {
//        holder->rotationMatrix = new GLfloat[4]{cosf(d2r(frameDegree)), -sinf(d2r(frameDegree)), sinf(d2r(frameDegree)), cosf(d2r(frameDegree))};
//        holder->frameDegree = frameDegree;
//    }
//    glVertexAttrib4fv(holder->positions[3], holder->rotationMatrix);

//    glClear(GL_COLOR_BUFFER_BIT);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(holder->positions[0]);
    glDisableVertexAttribArray(holder->positions[1]);

    eglSwapBuffers(holder->eglDisplay, holder->eglSurface);
}

//释放指定上下文
void releaseGLContext(GLContextHolder *holder) {
    glDeleteBuffers(1,&(holder->vertexBuff));
    glDeleteTextures(holder->textureNums, holder->textures);
    glDeleteProgram(holder->program);
    eglDestroySurface(holder->eglDisplay, holder->eglSurface);
    eglDestroyContext(holder->eglDisplay, holder->eglContext);
    delete[](holder->positions);
    delete[](holder->textures);
    delete (holder);
}

//创建一个新的绘制上下文
GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface, jboolean isSurfaceThreadExclusive) {
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
//    LOGI("majorVer=%d,minVer=%d", majorVer, minVer);
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
    GLContextHolder *gl_holder = new GLContextHolder();
    gl_holder->isSurfaceThreadExclusive = isSurfaceThreadExclusive;


    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return NULL;
    }
    //如果是线程独享的，不会被占用makeCurrent
    if(!gl_holder->isSurfaceThreadExclusive){
        LOGI("当前surface是多个线程共享的");
    }

    //context create success,now create program
    ShaderBase shader = ShaderRGBA();
    GLuint programYUV = createProgram(shader.vertexShader, shader.fragmentShader);
//    delete shader;
    if (programYUV == 0) {
        return NULL;
    }

    // Use tightly packed data
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    //success
    gl_holder->eglDisplay = display;
    gl_holder->eglContext = eglContext;
    gl_holder->eglSurface = eglSurface;
    gl_holder->program = programYUV;

    GLint posAttrVertices = glGetAttribLocation(programYUV, "aPosition");
    GLint posAttrTexCoords = glGetAttribLocation(programYUV, "aTexCoord");
    GLint posUniRgbTexture = glGetUniformLocation(programYUV, "rgbTexture");
    GLuint *positions = new GLuint[3];
    positions[0] = (GLuint) posAttrVertices;
    positions[1] = (GLuint) posAttrTexCoords;
    positions[2] = (GLuint) posUniRgbTexture;
    gl_holder->positions = positions;
//    gl_holder->frameDegree = -1;

    //tex
    GLuint *textures = new GLuint[2];
    glGenTextures(2, textures);
    gl_holder->textureNums = 2;
    gl_holder->textures = textures;

    //buff
    glUseProgram(programYUV);
    GLuint vertexBuff;
    glGenBuffers(1,&vertexBuff);
    glBindBuffer(GL_ARRAY_BUFFER,vertexBuff);
    glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES_COORD),VERTICES_COORD,GL_STATIC_DRAW);
    gl_holder->vertexBuff = vertexBuff;
    gl_holder->vertexStride = 4 * sizeof(GLfloat);
    gl_holder->texStride = 4 * sizeof(GLfloat);
    gl_holder->offsetVertex = 0;
    gl_holder->offsetTex = 2 * sizeof(GLfloat);

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);

    return gl_holder;
}