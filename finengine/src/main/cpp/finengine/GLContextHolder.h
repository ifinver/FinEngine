//
// Created by iFinVer on 2016/11/21.
//

#ifndef MYOPENGLES_GL_CONTEXT_H
#define MYOPENGLES_GL_CONTEXT_H

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <jni.h>
#include "shaders.h"

class GLContextHolder{
public:
    EGLContext eglContext;

    EGLSurface eglSurface;

    EGLDisplay eglDisplay;

    GLuint posAttrVertices;

    GLuint posAttrTexCoords;

    GLuint posUniTextureY;

    GLuint posUniTextureUV;

    int textureNums;

    GLuint *textures;

    GLuint posAttrScaleX;

    GLuint posAttrScaleY;

    jint frameDegree;

    const float *inputTextureCorrd;

    jint frameWidth;

    jint frameHeight;
    jint outWidth;
    jint outHeight;
    float frameScaleX;
    float frameScaleY;

    GLuint targetProgram;

    GLuint currentProgram;
};

#endif //MYOPENGLES_GL_CONTEXT_H
