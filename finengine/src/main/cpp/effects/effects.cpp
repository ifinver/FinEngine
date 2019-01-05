//
// Created by iFinVer on 2016/12/28.
//
#include <cstring>
#include <opencv2/imgcodecs.hpp>
#include "effects.h"
#include "monalisa/monalisa.h"
#include "../log.h"

#define LOG_TAG "FinEngine"

MonalisaMsg *mMonalisaMsg;

JNIEXPORT jboolean JNICALL
Java_com_ifinver_finengine_FinEffect_nativeInitMonalisa(JNIEnv *env, jclass type, jobject ctx, jstring monalisaPath_, jstring trackFilePath_) {
    if (initMonalisa(env, ctx, trackFilePath_) == 0) {
        //初始化成功
        const char *monalisaPath = env->GetStringUTFChars(monalisaPath_, 0);
        jlong result = detectMonaFace(monalisaPath);
        env->ReleaseStringUTFChars(monalisaPath_, monalisaPath);
        if (result > 0) {
            LOGE("%s", "检测图片成功！");
            mMonalisaMsg = new MonalisaMsg();
            return JNI_TRUE;
        } else {
            LOGE("图片人脸检测失败！");
            return JNI_FALSE;
        }
    }
}

//int write = 0;

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_FinEffect_nativeProcessMonalisa(JNIEnv *env, jclass type, jbyteArray data_, jint width, jint height, jlong facePtr) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    cv::Mat *monaLisaMat = effect_monaLisa(data,width,height,facePtr);
    mMonalisaMsg->width = monaLisaMat->cols;
    mMonalisaMsg->height = monaLisaMat->rows;
    mMonalisaMsg->texSize = mMonalisaMsg->width * mMonalisaMsg->height * 3;
    mMonalisaMsg->texData = monaLisaMat->data;
//
//    if(write == 0) {
//        cv::imwrite("/sdcard/f.png", *monaLisaMat);
//        write = 1;
//    }

    env->ReleaseByteArrayElements(data_, data, 0);

    return (jlong) mMonalisaMsg;
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FinEffect_nativeReleaseMonalisa(JNIEnv *env, jclass type){
    releaseMonalisa();
}

