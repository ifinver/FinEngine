//
// Created by iFinVer on 2016/12/16.
//

#ifndef FINENGINE_XCVCORE_H
#define FINENGINE_XCVCORE_H

#include <jni.h>
#include <string>
#include <vector>
#include <opencv2/opencv.hpp>
using namespace cv;

using namespace std;

unsigned char *xcv_swapFace(jbyte *data, jint width, jint height, long long int faceData);

void xcv_swapFace(JNIEnv *env,jlong matObj);

#endif //FINENGINE_XCVCORE_H
