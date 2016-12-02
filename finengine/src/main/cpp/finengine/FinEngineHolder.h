//
// Created by iFinVer on 2016/11/29.
//

#ifndef FINENGINE_FINENGINEHOLDER_H
#define FINENGINE_FINENGINEHOLDER_H

#include <GLES2/gl2.h>
#include <jni.h>

class FinEngineHolder {
public:
    GLuint objFrameBuffer;
    GLuint objRenderBuffer;
    GLuint program;
    GLuint inputTexture;
    GLuint localVertexPos;
    GLuint localTexturePos;
    GLuint localInputTexture;
//    jmethodID midAttachToGlContext;
//    jmethodID midDetachFromGLContext;
    jmethodID midUpdateTexImage;
    int frameWidth;
    int frameHeight;
};


#endif //FINENGINE_FINENGINEHOLDER_H
