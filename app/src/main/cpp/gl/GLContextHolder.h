//
// Created by iFinVer on 2016/11/21.
//

#ifndef MYOPENGLES_GL_CONTEXT_H
#define MYOPENGLES_GL_CONTEXT_H

#include "include-header.h"
#include "utils.h"

class GLContextHolder{
public:
    EGLContext eglContext;

    EGLSurface eglSurface;

    EGLDisplay eglDisplay;

    GLuint program;
};

#endif //MYOPENGLES_GL_CONTEXT_H
