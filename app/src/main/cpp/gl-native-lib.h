//
// Created by iFinVer on 2016/11/12.
//

#ifndef MYOPENGLES_GL_NATIVE_LIB_H
#define MYOPENGLES_GL_NATIVE_LIB_H

#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include <math.h>
#include <string>
#include <stdio.h>

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <opencv2/opencv.hpp>

bool setupGraphics(int width, int height);

void onDrawFrame();

static void checkGlError(const char *string);

void printGLString(const char* name, GLenum sn);

GLuint createProgram(const char* pVertexSrc,const char* pFragmentSrc);

GLuint loadShader(GLenum shaderType,const char* shaderSource);

extern "C" {
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_init(JNIEnv *env, jclass type, jint width, jint height);
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_onDraw(JNIEnv *env, jclass type);
JNIEXPORT jintArray JNICALL Java_com_ifinver_myopengles_GLNative_getGrayImage(JNIEnv *env, jclass type, jintArray pixels_, jint w, jint h);
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_processFrame(JNIEnv *env, jclass ,jint size, jbyteArray frameBuffer_);
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_initProcesser(JNIEnv *env, jclass , jint mFrameWidth, jint mFrameHeight, jint imageFormat);
JNIEXPORT void JNICALL Java_com_ifinver_myopengles_GLNative_releaseProcesser(JNIEnv *env, jclass type);
JNIEXPORT jlong JNICALL
Java_com_ifinver_myopengles_GLNative_processFrameMat(JNIEnv *env, jclass type, jint length, jbyteArray frameBuffer_);
}


#endif //MYOPENGLES_GL_NATIVE_LIB_H


