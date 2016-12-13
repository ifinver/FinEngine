//
// Created by iFinVer on 2016/12/7.
//

#ifndef FINENGINE_FINRECORDER_H
#define FINENGINE_FINRECORDER_H

#include <jni.h>

extern "C"{
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinRecorder_nativeCreate(JNIEnv *env, jobject instance, jlong sharedCtx, jobject output);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinRecorder_nativeProcess(JNIEnv *env, jobject instance, jlong recorder, jint inputTex);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinRecorder_nativeRelease(JNIEnv *env, jobject instance, jlong recorder);
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinRecorder_nativeFetchGLCtx(JNIEnv *env, jobject instance);
};

#endif //FINENGINE_FINRECORDER_H
