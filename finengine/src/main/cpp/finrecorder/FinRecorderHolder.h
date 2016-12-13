//
// Created by iFinVer on 2016/12/7.
//

#ifndef FINENGINE_FINRECORDERHOLDER_H
#define FINENGINE_FINRECORDERHOLDER_H


#include <EGL/egl.h>
#include <GLES2/gl2.h>

class FinRenderHolder {
public:
    EGLDisplay eglDisplay;
    EGLContext eglContext;
    EGLSurface eglSurface;
    GLuint program;
    GLuint posAttrVertices;
    GLuint posAttrTexCoords;
    GLuint posUniTextureS;
    GLuint vertexBuffer;
    size_t vertexStride;
    size_t texOffset;
};


#endif //FINENGINE_FINRECORDERHOLDER_H
