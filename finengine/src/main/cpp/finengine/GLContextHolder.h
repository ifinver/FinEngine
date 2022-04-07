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

    GLuint *textures;
    int textureNums;

    jint frameWidth;
    jint frameHeight;
    jint outWidth;
    jint outHeight;
    float frameScaleX;
    float frameScaleY;

    GLuint targetProgram;
    GLuint currentProgram;
    int currentFilter;

    GLuint defaultProgram;
    GLuint posUniScaleX;
    GLuint posUniScaleY;
    GLuint posUniRotation;
    GLuint posUniMirror;
    GLuint posAttrVertices;
    GLuint posAttrTexCoords;
    GLuint posUniTextureY;
    GLuint posUniTextureUV;
    GLuint posUniTextureFilter;

    GLuint programSticker;
    GLuint posStickerAttrVertices;
    GLuint posStickerAttrTexCoords;
    GLuint posStickerUniTexture;
    GLuint posStickerUniMvpMatrix;
    int stickerWidth;
    int stickerHeight;
    float* stickerMatrix;

    GLuint programPoint;
    GLuint posPointAttrVertices;
    GLuint posPointAttrScaleX;
    GLuint posPointAttrScaleY;
    GLuint posPointUniColor;
    GLuint posPointUniRotation;
};

#endif //MYOPENGLES_GL_CONTEXT_H
