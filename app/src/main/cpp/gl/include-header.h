//
// Created by iFinVer on 2016/11/21.
//

#ifndef MYOPENGLES_INCLUDE_HEADER_H
#define MYOPENGLES_INCLUDE_HEADER_H

#define  LOG_TAG    "gl-utils"


#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include <math.h>
#include <string>
#include <stdio.h>

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "GLContextHolder.h"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#endif //MYOPENGLES_INCLUDE_HEADER_H
