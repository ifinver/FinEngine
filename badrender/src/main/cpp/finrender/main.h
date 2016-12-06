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
JNIEXPORT jint JNICALL Java_com_ifinver_badrender_BadRender_createGLContext(JNIEnv *env, jclass type, jobject jSurface);
JNIEXPORT void JNICALL Java_com_ifinver_badrender_BadRender_release(JNIEnv *env, jclass type);
JNIEXPORT void JNICALL Java_com_ifinver_badrender_BadRender_render(JNIEnv *env, jclass type, jlong nativeGlContext,
                                                                            jbyteArray data_,jint frameWidth, jint frameHeight,jint degree,jboolean mirror);
}

GLContextHolder *newGLContext(JNIEnv *env, jobject jSurface, jboolean isSurfaceThreadExclusive);

void releaseGLContext();

void renderFrame(GLContextHolder *holder, jbyte *data, jint width, jint height,jint degree,jboolean mirror);

#endif //MYOPENGLES_GL_NATIVE_LIB_H