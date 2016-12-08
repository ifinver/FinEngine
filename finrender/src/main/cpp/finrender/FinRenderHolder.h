//
// Created by iFinVer on 2016/12/6.
//

#ifndef FINENGINE_FINRECORDERHOLDER_H
#define FINENGINE_FINRECORDERHOLDER_H


#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <jni.h>

class FinRecorderHolder {
public:
    EGLDisplay eglDisplay;
    EGLContext eglContext;
    EGLSurface eglSurface;
    GLuint program;
    GLuint posAttrVertices;
    GLuint posAttrTexCoords;
    GLuint posUniTextureS;
    GLuint inputTex;
    jmethodID midAttachToGlContext;
    jmethodID midDetachFromGLContext;
    jmethodID midUpdateTexImage;
    GLuint vertexBuffer;
    size_t vertexStride;
    size_t texOffset;
};


#endif //FINENGINE_FINRECORDERHOLDER_H
