//
// Created by iFinVer on 2016/12/7.
//

#ifndef FINENGINE_FINRECORDERHOLDER_H
#define FINENGINE_FINRECORDERHOLDER_H


class FinRecorderHolder {
public:
    EGLDisplay eglDisplay;
    EGLContext eglContext;
    EGLSurface eglSurface;
    GLuint program;
    GLuint posAttrVertices;
    GLuint posAttrTexCoords;
    GLuint posUniTextureS;
};


#endif //FINENGINE_FINRECORDERHOLDER_H
