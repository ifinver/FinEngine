//
// Created by iFinVer on 2016/12/15.
//

#include <cwchar>
#include "facedetector.h"
#include "../log.h"
#include "faceresultcv.h"
#include <opencv2/opencv.hpp>
#include <fstream>

using namespace cv;
using namespace std;

#define LOG_TAG "facedetector"

extern "C" {
CascadeClassifier *mXcFaceDetector;

JNIEXPORT jint JNICALL
Java_com_ifinver_finengine_FaceDetector_nativeInit(JNIEnv *env, jclass, jobject ctx,
                                                   jstring trackDataPath) {
    if (!mXcFaceDetector) {
        mXcFaceDetector = new CascadeClassifier();
        const char *path = env->GetStringUTFChars(trackDataPath, 0);
        int ret = mXcFaceDetector->load(path);
        env->ReleaseStringUTFChars(trackDataPath, path);
        return ret != 0 ? 0 : 1;
    }
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_FaceDetector_nativeProcess(JNIEnv *env, jclass, jbyteArray data_,
                                                      jint width, jint height) {
    if (mXcFaceDetector) {
        jbyte *data = env->GetByteArrayElements(data_, nullptr);
        Mat frameMat(height,width, CV_8UC1, data);// nv21 直接只使用y通道，就是灰度图。
        Mat small;
        double scale = 1 / 4.0;
//        double scale = 1;
        //缩小提高性能
        resize(frameMat, small, Size(), scale, scale, INTER_LINEAR_EXACT);
//        cvtColor( frame, frame_gray, COLOR_BGR2GRAY );
        equalizeHist( small, small );
        vector<Rect> *result = new vector<Rect>;
        mXcFaceDetector->detectMultiScale(small, *result, 1.1, 2,
                                          0
                                          //                                          |CASCADE_FIND_BIGGEST_OBJECT
                                          //                                          |CASCADE_DO_ROUGH_SEARCH
                                          | CASCADE_SCALE_IMAGE,
                                          Size(30, 30));
        for(int i = 0; i < result->size();i++){
            (*result)[i].width /= scale;
            (*result)[i].height /= scale;
            (*result)[i].x /= scale;
            (*result)[i].y /= scale;
        }
        FaceDetectResultCV *fdr = new FaceDetectResultCV();
        fdr->nFaceCountInOut = result->size();
        fdr->rcFaceRectOut = result;
        env->ReleaseByteArrayElements(data_, data, 0);
        return (jlong) fdr;
    }
    return 0;
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeRelease(JNIEnv *, jclass) {
    if (mXcFaceDetector) {
        //todo 这里会导致 Invalid address 0x12db0070 passed to free: value not allocated
//        delete mXcFaceDetector;
        mXcFaceDetector = nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_ifinver_finengine_FaceDetector_decodePNGData(JNIEnv *env, jclass type, jstring filePath) {
    const char *path = env->GetStringUTFChars(filePath, 0);
    Mat ori = imread(path, 1);
    LOGI("ori.cols=%d,rows=%d,channels=%d", ori.cols, ori.rows, ori.channels());
    cvtColor(ori, ori, COLOR_BGR2RGB);
    env->ReleaseStringUTFChars(filePath, path);
    ofstream out("/sdcard/encoded.fil", ios_base::out | ios_base::binary);
    //写width
    out.write(reinterpret_cast<char *>(&ori.cols), sizeof(int));
    //写height
    out.write(reinterpret_cast<char *>(&ori.rows), sizeof(int));
    //写data
    out.write((const char *) ori.data, ori.cols * ori.rows * 3);
    out.close();
    LOGI("%s", "滤镜文件encode成功！");
}
}