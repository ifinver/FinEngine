//
// Created by iFinVer on 2016/12/6.
//

#ifndef FINENGINE_FINRECORDER_H
#define FINENGINE_FINRECORDER_H

#include <jni.h>

extern "C"{
JNIEXPORT jlong JNICALL Java_com_ifinver_finrender_FinRender_nativeCreate(JNIEnv *env, jobject instance, jobject output);
JNIEXPORT void JNICALL Java_com_ifinver_finrender_FinRender_nativeRelease(JNIEnv *env, jobject instance,jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finrender_FinRender_nativeRenderOut(JNIEnv *env, jobject instance,jlong engine,jobject input);
JNIEXPORT jint JNICALL Java_com_ifinver_finrender_FinRender_nativeGetInputTex(JNIEnv *env, jobject instance, jlong engine);
JNIEXPORT jlong JNICALL Java_com_ifinver_finrender_FinRender_nativeGetEglContext(JNIEnv *env, jobject instance, jlong engine);
};

#endif //FINENGINE_FINRECORDER_H
