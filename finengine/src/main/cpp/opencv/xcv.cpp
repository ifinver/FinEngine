//
// Created by iFinVer on 2016/12/16.
//

#include "xcv.h"
#include <jni.h>
using namespace cv;



JNIEXPORT jintArray JNICALL Java_com_ifinver_finengine_FinCv_BGRA2Grey(JNIEnv *env, jclass type, jintArray data_, jint w, jint h) {
    jint *pixels = env->GetIntArrayElements(data_, NULL);
    //转换成单通道
    cv::Mat imgData(h, w, CV_8UC4, pixels);
    uchar *ptr = imgData.ptr(0);
    for (int i = 0; i < w * h; i++) {
        int grayScale = (int) (ptr[4 * i + 2] * 0.299 + ptr[4 * i + 1] * 0.587
                               + ptr[4 * i + 0] * 0.114);
        ptr[4 * i + 1] = (uchar) grayScale;
        ptr[4 * i + 2] = (uchar) grayScale;
        ptr[4 * i + 0] = (uchar) grayScale;
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, pixels);
    env->ReleaseIntArrayElements(data_, pixels, 0);
    return result;
}