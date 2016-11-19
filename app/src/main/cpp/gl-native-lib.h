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


static void checkGlError(const char *string);

GLuint createProgram(const char* pVertexSrc,const char* pFragmentSrc);

GLuint loadShader(GLenum shaderType,const char* shaderSource);

extern "C" {
JNIEXPORT jintArray JNICALL Java_com_ifinver_myopengles_GLNative_getGrayImage(JNIEnv *env, jclass type, jintArray pixels_, jint w, jint h);
}

#endif //MYOPENGLES_GL_NATIVE_LIB_H


