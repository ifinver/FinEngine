//
// Created by iFinVer on 2016/12/6.
//

#ifndef FINENGINE_FINRECORDER_H
#define FINENGINE_FINRECORDER_H

#include <jni.h>

extern "C"{
JNIEXPORT jint JNICALL Java_com_ifinver_finrecorder_FinRecorder_nativePrepare(JNIEnv *env, jobject instance, jobject surface);
JNIEXPORT void JNICALL Java_com_ifinver_finrecorder_FinRecorder_nativeRelease(JNIEnv *env, jobject instance);
JNIEXPORT void JNICALL Java_com_ifinver_finrecorder_FinRecorder_nativeRenderOutput(JNIEnv *env, jobject instance, jobject input);
};

#endif //FINENGINE_FINRECORDER_H
