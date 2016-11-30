//
// Created by iFinVer on 2016/11/29.
//

#include "ShaderHolder.h"
#include <sstream>
#include "log.h"
#include <assert.h>
#include <android/asset_manager_jni.h>

char *loadContent(AAssetManager *mgr, const char* name) {
    AAsset* asset = AAssetManager_open(mgr, name, AASSET_MODE_BUFFER);
    if(asset == NULL){
        LOGE("无法打开shader文件[%s]",name);
        return NULL;
    }
    off_t length = AAsset_getLength(asset);
    char* buff = new char[length+1];
    AAsset_read(asset, buff, (size_t) length);
    AAsset_close(asset);
    buff[length] = '\0';
//    std::string temp(buff);
//    return temp.c_str();
    return buff;
}

ShaderHolder::ShaderHolder(int filterType,JNIEnv* env,jobject jAssetsManager) {
//    LOGE("here we are");
    AAssetManager* mgr = AAssetManager_fromJava(env, jAssetsManager);
    assert(NULL != mgr);
    vertexShader = loadContent(mgr, "vertex.glsl");
    switch (filterType){
        default:
        case FILTER_TYPE_NORMAL:
            fragmentShader = loadContent(mgr,"fragment_normal.glsl");
            break;
    }
    LOGI("[%s] shader content:\n%s","vertex",vertexShader);
    LOGI("[%s] shader content:\n%s","fragment",fragmentShader);
}


const char* ShaderHolder::getVertexShader() {
    return vertexShader;
}

const char* ShaderHolder::getFragmentShader() {
    return fragmentShader;
}