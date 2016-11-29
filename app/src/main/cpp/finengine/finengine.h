//
// Created by iFinVer on 2016/11/27.
//
//

#ifndef MYOPENGLES_FINEGINE_H
#define MYOPENGLES_FINEGINE_H

#include <jni.h>
#include "FinEngineHolder.h"

#ifndef LOG_TAG
#include <android/log.h>
#define LOG_TAG "Fin Engine"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#endif

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *env, jclass type,int imageFormat,int frameWidth,int frameHeight,int filterType);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *env, jclass type, jlong glContext, jbyteArray mData_, jint mFrameDegree, jint mFrameWidth,
                                                                        jint mFrameHeight);
};
FinEngineHolder *newOffScreenGLContext(JNIEnv *env, int frameWidth, int frameHeight, int filterType);
#endif //MYOPENGLES_FINEGINE_H
