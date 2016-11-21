//
// Created by iFinVer on 2016/11/21.
//

#ifndef MYOPENGLES_GL_UTILS_H
#define MYOPENGLES_GL_UTILS_H

#define  LOG_TAG    "gl-utils"

#include "include-header.h"

static void checkGlError(const char *op);

GLuint loadShader(GLenum shaderType, const char *shaderSource);

GLuint createProgram(const char *pVertexSrc, const char *pFragmentSrc);

#endif //MYOPENGLES_GL_UTILS_H
