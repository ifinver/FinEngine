//
// Created by iFinVer on 2017/2/7.
//

#ifndef FINENGINE_EFFECTS_H
#define FINENGINE_EFFECTS_H

#include <jni.h>

typedef struct MonalisaMessage{
    int width;
    int height;
    int texSize;
    uchar* texData;
}MonalisaMsg;

extern "C"{

    JNIEXPORT jboolean JNICALL
    Java_com_ifinver_finengine_FinEffect_nativeInitMonalisa(JNIEnv *env, jclass type, jobject ctx, jstring monalisaPath_, jstring trackFilePath_);

    JNIEXPORT jlong JNICALL
    Java_com_ifinver_finengine_FinEffect_nativeProcessMonalisa(JNIEnv *env, jclass type, jbyteArray data_, jint width, jint height, jlong facePtr);

    JNIEXPORT void JNICALL
    Java_com_ifinver_finengine_FinEffect_nativeReleaseMonalisa(JNIEnv *env, jclass type);

}

#endif //FINENGINE_EFFECTS_H
