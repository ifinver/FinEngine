//
// Created by iFinVer on 2016/11/27.
//
#include "finengine.h"
#include "utils.h"
#include "log.h"
#include "shader/ShaderHolder.h"
#include <GLES2/gl2ext.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *env, jclass type, int imageFormat, int frameWidth, int frameHeight, int filterType) {
    FinEngineHolder *pHolder = NULL;
    switch (imageFormat) {
        case 0x11://ImageFormat.NV21
            pHolder = newOffScreenGLContext(env, frameWidth, frameHeight, filterType);
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

JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine) {

}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *env, jclass type, jlong glContext, jbyteArray mData_,
                                                                        jint mFrameDegree, jint mFrameWidth,
                                                                        jint mFrameHeight) {

}

FinEngineHolder *newOffScreenGLContext(JNIEnv *env, int frameWidth, int frameHeight, int filterType) {
    GLint maxRenderbufferSize;
    glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, &maxRenderbufferSize);
    if ((maxRenderbufferSize <= frameWidth) ||
        (maxRenderbufferSize <= frameHeight)) {
        LOGE("不支持这个宽或高");
        return NULL;
    }
    FinEngineHolder *holder = new FinEngineHolder();
    glGenFramebuffers(1,&holder->objFrameBuffer);
    glGenRenderbuffers(1,&holder->objRenderBuffer);

    glBindRenderbuffer(GL_RENDERBUFFER,holder->objRenderBuffer);
    glRenderbufferStorage(GL_RENDERBUFFER,GL_RGBA8_OES,frameWidth,frameHeight);

    glBindFramebuffer(GL_FRAMEBUFFER,holder->objFrameBuffer);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_RENDERBUFFER,holder->objRenderBuffer);

    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status == GL_FRAMEBUFFER_COMPLETE){
        ShaderHolder shaderHolder(filterType);
        GLuint program = createProgram(shaderHolder.getVertexShader(), shaderHolder.getFragmentShader());
    }else{
        LOGE("引擎启动失败！ code=1");
        return NULL;
    }
    return holder;
}
