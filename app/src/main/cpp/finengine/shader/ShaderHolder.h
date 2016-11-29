//
// Created by iFinVer on 2016/11/29.
//

#ifndef FINENGINE_SHADERHOLDER_H
#define FINENGINE_SHADERHOLDER_H
#include <GLES2/gl2.h>

const GLfloat VERTICES_BASE[] =
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

class ShaderHolder {
public:
    ShaderHolder(int filterType);
    const char* getVertexShader(); 
    const char* getFragmentShader(); 
private:
    const char *vertexShader;
    const char *fragmentShader;
};


#endif //FINENGINE_SHADERHOLDER_H
