//
// Created by iFinVer on 2016/11/27.
//
#include "finengine.h"
#include "utils.h"
#include "log.h"
#include "ShaderHolder.h"
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <math.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *env, jclass type, int imageFormat, int frameWidth, int frameHeight, int filterType,jobject jAssetsManager) {
    FinEngineHolder *pHolder = NULL;
    switch (imageFormat) {
        case 0x11://ImageFormat.NV21
            pHolder = newOffScreenGLContext(env, frameWidth, frameHeight, filterType,jAssetsManager);
            break;
        default:
            LOGE("不支持的视频编码格式！");
            break;
    }

    if (pHolder == NULL) {
        return 0;
    }
    return (jlong) pHolder;
}

 void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine) {
    releaseEngine((FinEngineHolder *) engine);
}

 void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *env, jclass, jlong engine,  jobject surfaceTexture,jint mFrameDegree) {
    FinEngineHolder *holder = (FinEngineHolder *) engine;
    env->CallVoidMethod(surfaceTexture,holder->midAttachToGlContext,holder->inputTexture);
    renderFrame(holder,mFrameDegree);
    env->CallVoidMethod(surfaceTexture,holder->midAttachToGlContext);
}

void renderFrame(FinEngineHolder *holder, jint degree) {
    glEnableVertexAttribArray(holder->localVertexPos);
    glVertexAttribPointer(holder->localVertexPos, 2, GL_FLOAT, GL_FALSE, holder->texStride, (const void *) holder->offsetVertex);

    glEnableVertexAttribArray(holder->localTexturePos);
    glVertexAttribPointer(holder->localTexturePos, 2, GL_FLOAT, GL_FALSE, holder->texStride, (const void *) holder->offsetTex);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES,holder->inputTexture);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glUniform1i(holder->localInputTexture,0);

    //rotation
    if (holder->frameDegree != degree) {
        float r = d2r(degree);
        float cosR = cosf(r);
        float sinR = sinf(r);
        holder->rotationMatrix = new GLfloat[4]{cosR, -sinR, sinR, cosR};
        holder->frameDegree = degree;
    }
    glVertexAttrib4fv(holder->localRotateVec, holder->rotationMatrix);

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

    glDisableVertexAttribArray(holder->localVertexPos);
    glDisableVertexAttribArray(holder->localTexturePos);

    GLint readType, readFormat;
    glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_TYPE, &readType);
    glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_FORMAT, &readFormat);
    unsigned int bytesPerPixel = 0;
    switch(readType)
    {
        case GL_UNSIGNED_BYTE:
            switch(readFormat)
            {
                case GL_RGBA:
                case GL_RGBA8_OES:
                    LOGE("读取格式为 ： GL_RGBA8_OES");
                    bytesPerPixel = 4;
                    break;
                case GL_RGB:
                    bytesPerPixel = 3;
                    break;
                case GL_LUMINANCE_ALPHA:
                    bytesPerPixel = 2;
                    break;
                case GL_ALPHA:
                case GL_LUMINANCE:
                    bytesPerPixel = 1;
                    break;
            }
            break;
        case GL_UNSIGNED_INT_24_8_OES: // GL_RGBA format
            LOGE("读取格式为 ： GL_UNSIGNED_INT_24_8_OES");
        case GL_UNSIGNED_SHORT_5_6_5: // GL_RGB format
            bytesPerPixel = 2;
            break;
    }
    unsigned int size = holder->frameWidth * holder->frameHeight * bytesPerPixel;
    LOGE("读取格式为 ：bytes size = %d",size);

    //done.
//    glReadPixels(0,0,holder->frameWidth,holder->frameHeight,readFormat,0.0);
}

FinEngineHolder *newOffScreenGLContext(JNIEnv *env, int frameWidth, int frameHeight, int filterType,jobject jAssetsManager) {
    /**
     * 初始化Context
     */
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        checkGlError("eglGetDisplay");
        return NULL;
    }
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
    const EGLint surfaceAttr[] = {
            EGL_WIDTH,512,
            EGL_HEIGHT,512,
            EGL_NONE
    };
    EGLSurface eglSurface = eglCreatePbufferSurface(display, config, surfaceAttr);
    if (!eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
        checkGlError("eglMakeCurrent");
        return NULL;
    }

    /**
     * 初始化FrameBuffer
     */
    FinEngineHolder *holder = new FinEngineHolder();
    glGenFramebuffers(1,&holder->objFrameBuffer);
    glBindFramebuffer(GL_FRAMEBUFFER,holder->objFrameBuffer);

    /**
     * 以RenderBuffer作为依附
     */
    glGenRenderbuffers(1,&holder->objRenderBuffer);
    glBindRenderbuffer(GL_RENDERBUFFER,holder->objRenderBuffer);
    glRenderbufferStorage(GL_RENDERBUFFER,GL_RGBA8_OES,frameWidth,frameHeight);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_RENDERBUFFER,holder->objRenderBuffer);
    holder->frameWidth = frameWidth;
    holder->frameHeight = frameHeight;
    /**
     * 以Texture作为依附
     */
//    glGenTextures(1,&holder->objRenderBuffer);
//    glBindTexture(GL_TEXTURE_2D,holder->objRenderBuffer);
//    glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,frameWidth,frameHeight,0,GL_RGBA,GL_UNSIGNED_BYTE,NULL);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
//                           GL_TEXTURE_2D, holder->objRenderBuffer, 0);

    glDepthMask(GL_FALSE);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_DITHER);

    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    bool success = false;
    switch (status){
        case GL_FRAMEBUFFER_COMPLETE:
            success = true;
            break;
        case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
            LOGE("引擎启动失败！msg = [%s]","GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            break;
        case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
            LOGE("引擎启动失败！msg = [%s]","GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS");
            break;
        case GL_FRAMEBUFFER_UNSUPPORTED:
            LOGE("引擎启动失败！msg = [%s]","GL_FRAMEBUFFER_UNSUPPORTED");
            break;
        default:
            LOGE("引擎启动失败！msg = [%s],code=[%d]","un know",status);
            break;
    }

    if(success){
        ShaderHolder shaderHolder(filterType,env,jAssetsManager);
        GLuint program = createProgram(shaderHolder.getVertexShader(), shaderHolder.getFragmentShader());
        holder->program = program;
        holder->localVertexPos = (GLuint) glGetAttribLocation(program, "aPosition");
        holder->localTexturePos = (GLuint) glGetAttribLocation(program, "aTexCoord");
        holder->localRotateVec = (GLuint) glGetAttribLocation(program, "aRotVector");
        holder->localInputTexture = (GLuint) glGetUniformLocation(program, "sTexture");

        GLuint vertexBuff;
        glGenBuffers(1,&vertexBuff);
        glBindBuffer(GL_ARRAY_BUFFER,vertexBuff);
        glBufferData(GL_ARRAY_BUFFER, sizeof(VERTICES_BASE),VERTICES_BASE,GL_STATIC_DRAW);
        holder->vertexBuff = vertexBuff;
        holder->vertexStride = 4 * sizeof(GLfloat);
        holder->texStride = 4 * sizeof(GLfloat);
        holder->offsetVertex = 0;
        holder->offsetTex = 2 * sizeof(GLfloat);

        //tex
        glGenTextures(1, &holder->inputTexture);

        //输入纹理的方法
        jclass jcSurfaceTexture = env->FindClass("android/graphics/SurfaceTexture");
        holder->midAttachToGlContext = env->GetMethodID(jcSurfaceTexture, "attachToGLContext", "(I)V");
        holder->midDetachFromGLContext = env->GetMethodID(jcSurfaceTexture, "detachFromGLContext", "()V");

    }else{
        return NULL;
    }
    return holder;
}

void releaseEngine(FinEngineHolder *pHolder) {
    glDeleteBuffers(1,&pHolder->vertexBuff);
    glDeleteTextures(1,&pHolder->inputTexture);
    glDeleteProgram(pHolder->program);
    glDeleteRenderbuffers(1,&pHolder->objRenderBuffer);
    glDeleteFramebuffers(1,&pHolder->objFrameBuffer);
//    delete[](pHolder->positions);
    delete (pHolder);
}
