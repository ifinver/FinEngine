//
// Created by iFinVer on 2016/12/15.
//

#ifndef FINENGINE_FACEDETECT_H
#define FINENGINE_FACEDETECT_H

#include <jni.h>

extern "C" {
JNIEXPORT jint JNICALL Java_com_ifinver_finengine_FaceDetector_nativeInit(JNIEnv *env, jclass type,jobject ctx,jstring trackDataPath);
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FaceDetector_nativeProcess(JNIEnv *env, jclass type, jbyteArray data_, jint width, jint height);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeRelease(JNIEnv *env, jclass type);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_decodePNGData(JNIEnv *env, jclass type, jstring filePath);
}

#endif //FINENGINE_FACEDETECT_H
