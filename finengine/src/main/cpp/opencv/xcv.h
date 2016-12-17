//
// Created by iFinVer on 2016/12/16.
//

#ifndef FINENGINE_XCV_H
#define FINENGINE_XCV_H


#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

extern "C"{
JNIEXPORT jintArray JNICALL Java_com_ifinver_finengine_FinCv_BGRA2Grey(JNIEnv *env, jclass type, jintArray data_, jint width, jint height);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinCv_swapFace(JNIEnv *env, jclass type,jlong);
};

#endif //FINENGINE_XCV_H
