//
// Created by iFinVer on 2016/11/12.
//

#ifndef MYOPENGLES_GL_NATIVE_LIB_H
#define MYOPENGLES_GL_NATIVE_LIB_H


#define  LOG_TAG    "FinEngine"

#include <jni.h>
#include <android/log.h>

#include "GLContextHolder.h"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

const int ENGINE_MODE_NORMAL = 100;
const int ENGINE_MODE_FACE_SWAP = 101;
const int ENGINE_MODE_MONA_LISA = 102;

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinEngine_nativeInit(JNIEnv *env, jclass, jobject jSurface,jobject mAssetManager,jstring _cleanFilePath);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeRelease(JNIEnv *env, jclass type, jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeRender(JNIEnv *, jclass, jlong, jbyteArray, jint, jint, jint, jboolean, jint, jint,jlong);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchFilter(JNIEnv *env, jobject, jlong, jobject, jint, jstring, jstring);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchToModeMonaLisa(JNIEnv *env, jobject instance, jlong,jstring filePath_,jobject,jstring);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchToModeNormal(JNIEnv *env, jobject instance, jlong);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchToModeFaceSwap(JNIEnv *env, jobject instance, jlong);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSetBrightness(JNIEnv *env, jclass type,jlong engine,jfloat brightness);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSetContrast(JNIEnv *env, jclass type, jlong engine,jfloat contrast);
}

void releaseGLContext(GLContextHolder *engineHolder);

void renderFrame(GLContextHolder *engineHolder, jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth, jint outHeight,
                 jlong facePtr);

void renderYuv(GLContextHolder *engineHolder, const jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth,
               jint outHeight, jlong facePtr);

void renderRgb(GLContextHolder *engineHolder, unsigned char *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth,
               jint outHeight, jlong i);

#endif //MYOPENGLES_GL_NATIVE_LIB_H