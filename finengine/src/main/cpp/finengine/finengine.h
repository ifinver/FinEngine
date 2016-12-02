//
// Created by iFinVer on 2016/11/27.
//
//

#ifndef MYOPENGLES_FINEGINE_H
#define MYOPENGLES_FINEGINE_H

#include <jni.h>
#include "FinEngineHolder.h"

extern "C" {
JNIEXPORT int JNICALL Java_com_ifinver_finengine_FinEngine__1startEngine(JNIEnv *, jclass, int, int, jobject);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine__1stopEngine(JNIEnv *env, jclass type);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_process(JNIEnv *, jclass, jobject, jint mFrameDegree,jboolean mirror,jbyteArray _data);
};

FinEngineHolder *newOffScreenGLContext(JNIEnv *env, int frameWidth, int frameHeight, jobject jAssetsManager);

void releaseEngine();

void renderFrame(jint degree, jboolean mirror,jbyte *buff);

#endif //MYOPENGLES_FINEGINE_H
