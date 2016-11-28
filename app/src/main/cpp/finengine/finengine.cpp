//
// Created by iFinVer on 2016/11/27.
//
#include "finengine.h"
#include "utils.h"
#include <math.h>
#include <GLES2/gl2ext.h>
#include <android/native_window_jni.h>

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *env, jclass type,int imageFormat,int filterType) {
    GLContextHolder *pHolder = NULL;
    switch (imageFormat) {
        case 0x11://ImageFormat.NV21
            pHolder = newOffScreenGLContext(env, filterType);
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

JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine){

}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *env, jclass type, jlong glContext, jbyteArray mData_, jint mFrameDegree, jint mFrameWidth,
                                                                        jint mFrameHeight){

}

GLContextHolder* newOffScreenGLContext(JNIEnv *env,int filterType){
//    glGenBuffers()
}
