//
// Created by iFinVer on 2016/11/27.
//
//

#ifndef MYOPENGLES_FINEGINE_H
#define MYOPENGLES_FINEGINE_H

#include <jni.h>
#include "FinEngineHolder.h"

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *env, jclass type,int imageFormat,int frameWidth,int frameHeight,int filterType);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *env, jclass type, jlong glContext, jbyteArray mData_, jint mFrameDegree, jint mFrameWidth,
                                                                        jint mFrameHeight);
};
FinEngineHolder *newOffScreenGLContext(JNIEnv *env, int frameWidth, int frameHeight, int filterType);
#endif //MYOPENGLES_FINEGINE_H
