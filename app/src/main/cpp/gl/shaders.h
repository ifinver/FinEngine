//
// Created by iFinVer on 2016/11/22.
//

#ifndef MYOPENGLES_GL_SHADERS_H
#define MYOPENGLES_GL_SHADERS_H

class ShaderYuv {
public:
    const char *vertexShader;
    const char *fragmentShader;

    ShaderYuv() {
        vertexShader =
                "attribute vec4 vPosition;   \n"
                "void main()                 \n"
                "{                           \n"
                "   gl_Position = vPosition; \n"
                "}                           \n";

        fragmentShader =
                "precision mediump float;                    \n"
                "void main()                                 \n"
                "{                                           \n"
                "   gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); \n"
                "}                                           \n";

    }

    ~ShaderYuv() {
        vertexShader = nullptr;
        fragmentShader = nullptr;
    }
};

#endif //MYOPENGLES_GL_SHADERS_H
