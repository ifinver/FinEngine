//
// Created by iFinVer on 2016/11/29.
//

#ifndef FINENGINE_SHADERHOLDER_H
#define FINENGINE_SHADERHOLDER_H

#include <GLES2/gl2.h>
#include <jni.h>

const GLfloat VERTICES_COORD[] =
        {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f,
        };

//无镜像，左上-左下-右下-右上  左上-左下-右下-右上
//循环一次是为了处理旋转问题，
const GLfloat TEXTURE_COORD_NOR[] =
        {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f, // new loop for rotate
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };

//镜像，左上-左下-右下-右上  左上-左下-右下-右上
//循环一次是为了处理旋转问题，
const GLfloat TEXTURE_COORD_MIRROR[] =
        {
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,// new loop for rotate
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
        };

const int FILTER_TYPE_NORMAL = 0;
const int FILTER_TYPE_CYAN = 1;
const int FILTER_TYPE_FISH_EYE = 2;
const int FILTER_TYPE_GREY_SCALE = 3;
const int FILTER_TYPE_NEGATIVE_COLOR = 4;

class ShaderHolder {
public:
    ShaderHolder(int filterType, JNIEnv *env, jobject jAssetsManager);

    const char *getVertexShader();

    const char *getFragmentShader();

private:
    const char *vertexShader;
    const char *fragmentShader;
};


#endif //FINENGINE_SHADERHOLDER_H
