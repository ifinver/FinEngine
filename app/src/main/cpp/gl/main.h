//
// Created by iFinVer on 2016/11/12.
//

#ifndef MYOPENGLES_GL_NATIVE_LIB_H
#define MYOPENGLES_GL_NATIVE_LIB_H


#define  LOG_TAG    "gl-native-lib"

#include <jni.h>
#include <android/log.h>

#include "GLContextHolder.h"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" {
JNIEXPORT jlong JNICALL Java_com_ifinver_myopengles_GLNative_createGLContext(JNIEnv *env, jclass type, jobject jSurface,int frameDegree, int imageFormat);
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_releaseGLContext(JNIEnv *env, jclass type, jlong nativeContext);
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_renderOnContext(JNIEnv *env, jclass type, jlong nativeGlContext,
                                                                            jbyteArray data_, jint frameWidth, jint frameHeight);
}

GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface, int);

void releaseGLContext(GLContextHolder *pHolder);

void renderFrame(GLContextHolder *holder, jbyte *data, jint width, jint height);

#endif //MYOPENGLES_GL_NATIVE_LIB_H

