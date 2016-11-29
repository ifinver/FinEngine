//
// Created by iFinVer on 2016/11/21.
//

#ifndef MYOPENGLES_GL_UTILS_H
#define MYOPENGLES_GL_UTILS_H

#include <GLES2/gl2.h>

#ifndef LOG_TAG
#include <android/log.h>
#define LOG_TAG "Fin Engine"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#endif

void checkGlError(const char *op);

GLuint loadShader(GLenum shaderType, const char *shaderSource);

GLuint createProgram(const char *pVertexSrc, const char *pFragmentSrc);

// 角度转弧度
float d2r(float d);

#endif //MYOPENGLES_GL_UTILS_H
