//
// Created by iFinVer on 2016/11/29.
//

#include "ShaderHolder.h"

ShaderHolder::ShaderHolder(int filterType) {

}

const char* ShaderHolder::getVertexShader() {
    return vertexShader;
}

const char* ShaderHolder::getFragmentShader() {
    return fragmentShader;
}