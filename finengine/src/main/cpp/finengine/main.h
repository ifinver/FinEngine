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

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finengine_FinEngine_nativeInit(JNIEnv *env, jclass type, jobject jSurface,jobject mAssetManager);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeRelease(JNIEnv *env, jclass type,jlong engine);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeRender(JNIEnv *env, jclass,jlong engine,jbyteArray data_, jint frameWidth, jint frameHeight,
                                                    jint degree, jboolean mirror,jint outWidth,jint outHeight,jlong facePtr);
JNIEXPORT void JNICALL Java_com_ifinver_finengine_FinEngine_nativeSwitchFilter(JNIEnv *env, jobject instance,jlong engine,jobject mAssetManager, jint mFilterType,jstring vert,jstring frag);
}

void releaseGLContext(GLContextHolder *engineHolder);

void renderFrame(GLContextHolder *engineHolder,jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth, jint outHeight,jlong facePtr);

void renderYuv(GLContextHolder *engineHolder, const jbyte *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth,
               jint outHeight,jlong facePtr);

void renderRgb(GLContextHolder *engineHolder, unsigned char *data, jint width, jint height, jint degree, jboolean mirror, jint outWidth,
               jint outHeight, jlong i);

#endif //MYOPENGLES_GL_NATIVE_LIB_H