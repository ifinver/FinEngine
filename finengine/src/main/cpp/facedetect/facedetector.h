//
// Created by iFinVer on 2016/12/15.
//

#ifndef FINENGINE_FACEDETECT_H
#define FINENGINE_FACEDETECT_H

#include <jni.h>
#include <amcomdef.h>

extern "C" {
JNIEXPORT jint JNICALL Java_com_ifinver_finengine_FaceDetector_nativeInit(JNIEnv *env, jclass type,jobject ctx,jstring trackDataPath);
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FaceDetector_nativeProcess(JNIEnv *env, jclass type, jbyteArray data_, jint width, jint height);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeRelease(JNIEnv *env, jclass type);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeSetProcessModel(JNIEnv *env, jclass type, jlong model);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeSetFaceBrightLevel(JNIEnv *env, jclass type, jint brightLevel);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FaceDetector_nativeSetFaceSkinSoftenLevel(JNIEnv *env, jclass type, jint skinSoftenLevel);
}

/**
 * 返回值 -1 means 人脸检测未初始化； -2 means 检测出错； 1 means 未检测出人脸； 0 success；
 */
int face_processSingleFrame(void *data, int width, int height, MPOINT *faceOutlinePointOut, MRECT *faceRectOut, MFloat *faceOrientOut);

#endif //FINENGINE_FACEDETECT_H
