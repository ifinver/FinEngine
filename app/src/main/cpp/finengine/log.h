//
// Created by iFinVer on 2016/11/29.
//

#ifndef FINENGINE_LOG_H
#define FINENGINE_LOG_H

#include <android/log.h>
#define LOG_TAG "Fin Engine"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#endif //FINENGINE_LOG_H
