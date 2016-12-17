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

unsigned char* xcv_swapFace(signed char* data, int width, int height, long long faceData,vector<Point2i> *hull1,vector<Point2i> *hull2);

void xcv_swapFace(JNIEnv *env,jlong matObj);

#endif //FINENGINE_XCVCORE_H
