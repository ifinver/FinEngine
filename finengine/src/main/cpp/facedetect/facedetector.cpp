//
// Created by iFinVer on 2016/12/15.
//

#include <cwchar>
#include "facedetector.h"
#include "../log.h"
#include "arcsoft/arcsoft.h"
#include <opencv2/opencv.hpp>
#include <fstream>

using namespace cv;
using namespace std;


#define LOG_TAG "facedetector"

ArcSoftSpotlight *mXcFaceDetector;

JNIEXPORT jint JNICALL Java_com_ifinver_finengine_FaceDetector_nativeInit(JNIEnv *env, jclass ,jobject ctx, jstring trackDataPath) {
    if (mXcFaceDetector == NULL) {
        mXcFaceDetector = new ArcSoftSpotlight();
        const char *path = env->GetStringUTFChars(trackDataPath, 0);
        int ret = mXcFaceDetector->init(env, ctx, path);
        env->ReleaseStringUTFChars(trackDataPath, path);
        return ret;
    }
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_ifinver_finengine_FaceDetector_nativeProcess(JNIEnv *env, jclass, jbyteArray data_, jint width, jint height) {
    jlong result = 0;
    if (mXcFaceDetector != NULL) {
        jbyte *data = env->GetByteArrayElements(data_, NULL);
        result = mXcFaceDetector->process(data, width, height,0);
        env->ReleaseByteArrayElements(data_, data, 0);
    }
    return result;
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeRelease(JNIEnv *, jclass ) {
    if (mXcFaceDetector != NULL) {
        delete mXcFaceDetector;
        mXcFaceDetector = NULL;
    }
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeSetProcessModel(JNIEnv *, jclass , jlong model){
    if(mXcFaceDetector != NULL){
        mXcFaceDetector->setProcessModel((long) model);
    }
}
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeSetFaceBrightLevel(JNIEnv *, jclass , jint brightLevel){
    if(mXcFaceDetector != NULL){
        mXcFaceDetector->setFaceBrightLevel(brightLevel);
    }
}
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeSetFaceSkinSoftenLevel(JNIEnv *, jclass , jint skinSoftenLevel){
    if(mXcFaceDetector != NULL){
        mXcFaceDetector->setFaceSkinSoftenLevel(skinSoftenLevel);
    }
}

JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_decodePNGData(JNIEnv *env, jclass type, jstring filePath){
    const char *path = env->GetStringUTFChars(filePath, 0);
    Mat ori = imread(path,1);
    LOGI("ori.cols=%d,rows=%d,channels=%d",ori.cols,ori.rows,ori.channels());
    cvtColor(ori,ori,CV_BGR2RGB);
    env->ReleaseStringUTFChars(filePath, path);
    ofstream out("/sdcard/encoded.fil",ios_base::out | ios_base::binary);
    //写width
    out.write(reinterpret_cast<char*>(&ori.cols), sizeof(int));
    //写height
    out.write(reinterpret_cast<char*>(&ori.rows), sizeof(int));
    //写data
    out.write((const char *) ori.data, ori.cols * ori.rows * 3);
    out.close();
    LOGI("%s","滤镜文件encode成功！");
}