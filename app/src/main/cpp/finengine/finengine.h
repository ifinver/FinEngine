//
// Created by iFinVer on 2016/11/27.
//
//

#ifndef MYOPENGLES_FINEGINE_H
#define MYOPENGLES_FINEGINE_H

#include <jni.h>
#include "FinEngineHolder.h"

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1startEngine(JNIEnv *, jclass, int, int, int, int, jobject);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine__1stopEngine(JNIEnv *env, jclass type, jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_sdk_FinEngine_process(JNIEnv *, jclass, jlong, jobject, jint mFrameDegree);
};

FinEngineHolder *newOffScreenGLContext(JNIEnv *env, int frameWidth, int frameHeight, int filterType, jobject jAssetsManager);

void releaseEngine(FinEngineHolder *pHolder);

void renderFrame(FinEngineHolder *pHolder, jint degree);

#endif //MYOPENGLES_FINEGINE_H
