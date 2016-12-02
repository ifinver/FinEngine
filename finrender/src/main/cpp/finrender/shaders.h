//
// Created by iFinVer on 2016/11/22.
//

#ifndef MYOPENGLES_GL_SHADERS_H
#define MYOPENGLES_GL_SHADERS_H

#include <GLES2/gl2.h>

const GLfloat VERTICES_COORD[] =
        {
                -1.0f, 1.0f,   // Position 0
                0.0f, 0.0f,   // TexCoord 0
                -1.0f, -1.0f,  // Position 1
                0.0f, 1.0f,   // TexCoord 1
                1.0f, -1.0f,  // Position 2
                1.0f, 1.0f,   // TexCoord 2
                1.0f, 1.0f,   // Position 3
                1.0f, 0.0f    // TexCoord 3
        };
class ShaderBase{
public:
    const char *vertexShader;
    const char *fragmentShader;

    ShaderBase(){
        vertexShader =
                        "attribute vec4 aPosition;                          \n"
                        "attribute vec2 aTexCoord;                          \n"
                        "varying vec2 vTexCoord;                            \n"
                        "void main(){                                       \n"
                        "   vTexCoord = aTexCoord;                          \n"
                        "   gl_Position = aPosition;                        \n"
                        "}                                                  \n";
    }

//    ~ShaderBase(){
//        delete vertexShader;
//        delete fragmentShader;
//    }
};
class ShaderRGBA : public ShaderBase {
public:
    ShaderRGBA() {
        fragmentShader =
                        "#extension GL_OES_EGL_image_external : require     \n"
                        "precision mediump float;                           \n"
                        "varying vec2 vTexCoord;                            \n"
                        "uniform sampler2D rgbTexture;                      \n"
                        "void main(){                                       \n"
                        "   gl_FragColor = texture2D(rgbTexture,vTexCoord); \n"
                        "}                                                  \n";

    }
};
#endif