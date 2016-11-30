//
// Created by iFinVer on 2016/11/29.
//

#ifndef FINENGINE_SHADERHOLDER_H
#define FINENGINE_SHADERHOLDER_H
#include <GLES2/gl2.h>
#include <jni.h>

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

const int FILTER_TYPE_NORMAL = 0;
const int FILTER_TYPE_CYAN = 1;
const int FILTER_TYPE_FISH_EYE = 2;
const int FILTER_TYPE_GREY_SCALE = 3;
const int FILTER_TYPE_NEGATIVE_COLOR = 4;

class ShaderHolder {
public:
    ShaderHolder(int filterType,JNIEnv* env,jobject jAssetsManager);
    const char* getVertexShader(); 
    const char* getFragmentShader(); 
private:
    const char *vertexShader;
    const char *fragmentShader;
};


#endif //FINENGINE_SHADERHOLDER_H
