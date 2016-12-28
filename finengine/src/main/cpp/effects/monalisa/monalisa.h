//
// Created by iFinVer on 2016/12/28.
//

#ifndef FINENGINE_MONALISA_H
#define FINENGINE_MONALISA_H


#include <jni.h>
#include <opencv2/core/mat.hpp>

int initMonalisa(JNIEnv *env, jobject ctx, jstring trackDataPath);

jlong detectMonaFace(const char* monaPath);

cv::Mat* effect_monaLisa(jbyte *data, jint width, jint height);

void releaseMonalisa();

#endif //FINENGINE_MONALISA_H
