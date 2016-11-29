//
// Created by iFinVer on 2016/11/29.
//

#include "ShaderHolder.h"
#include <fstream>
#include "../log.h"
using namespace std;

const char* loadContent(const char* name){
    ifstream fin(name,ifstream::binary);
    if(fin){
//        fin.seekg(0,fin.end);
//        int length = (int) fin.tellg();
//        fin.seekg(0,fin.beg);

        string buff;
        fin>>buff;
        const char *str = buff.c_str();
        LOGE(str);
        return str;
        
//        char* buff = new char[length];
//        fin.read(buff,length);
//        const char* result = buff;
//        LOGE(result);
//        return result;
    }else{
        LOGE("无法打开shader文件");
        return "";
    }
}

ShaderHolder::ShaderHolder(int filterType) {
    vertexShader = loadContent("vertex.glsl");
    switch (filterType){
        default:
        case FILTER_TYPE_NORMAL:

            break;
    }
}


const char* ShaderHolder::getVertexShader() {
    return vertexShader;
}

const char* ShaderHolder::getFragmentShader() {
    return fragmentShader;
}