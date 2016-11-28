//
// Created by iFinVer on 2016/11/27.
//
//
#include <jni.h>
#include <android/log.h>

#include "../finrender/GLContextHolder.h"

#ifndef MYOPENGLES_FINEGINE_H
#define MYOPENGLES_FINEGINE_H

#define LOG_TAG "Fin Engine"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *env, jclass type,int imageFormat,int filterType);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *env, jclass type, jlong glContext, jbyteArray mData_, jint mFrameDegree, jint mFrameWidth,
                                                                        jint mFrameHeight);
};
GLContextHolder* newOffScreenGLContext(JNIEnv *env,int filterType);
#endif //MYOPENGLES_FINEGINE_H
