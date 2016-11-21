//
// Created by iFinVer on 2016/11/21.
//

#ifndef MYOPENGLES_GL_CONTEXT_H
#define MYOPENGLES_GL_CONTEXT_H

#include "gl-include-header.h"
#include "gl-utils.h"

class GL_Context_Holder{
public:
    EGLContext eglContext;

    EGLSurface eglSurface;

    EGLDisplay eglDisplay;
};

#endif //MYOPENGLES_GL_CONTEXT_H
