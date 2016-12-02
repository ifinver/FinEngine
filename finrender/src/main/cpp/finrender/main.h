//
// Created by iFinVer on 2016/11/12.
//

#ifndef MYOPENGLES_GL_NATIVE_LIB_H
#define MYOPENGLES_GL_NATIVE_LIB_H


#define  LOG_TAG    "FinRender"

#include <jni.h>
#include <android/log.h>

#include "GLContextHolder.h"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

const int FORMAT_RGBA = 0x101;

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_finrender_FinRender_createGLContext(JNIEnv *env, jclass type, jobject jSurface,jboolean isSurfaceThreadExclusive,int);
JNIEXPORT void JNICALL Java_com_ifinver_finrender_FinRender_releaseGLContext(JNIEnv *env, jclass type, jlong nativeContext);
JNIEXPORT void JNICALL Java_com_ifinver_finrender_FinRender_renderOnContext(JNIEnv *env, jclass type, jlong nativeGlContext,
                                                                            jbyteArray data_,jint frameWidth, jint frameHeight);
}

GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface, jboolean isSurfaceThreadExclusive);

void releaseGLContext(GLContextHolder *pHolder);

void renderFrame(GLContextHolder *holder, jbyte *data, jint width, jint height);

#endif //MYOPENGLES_GL_NATIVE_LIB_H