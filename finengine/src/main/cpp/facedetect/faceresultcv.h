//
// Created by iFinVer on 2016/12/15.
//

#ifndef FINENGINE_FACERESULTCV_H
#define FINENGINE_FACERESULTCV_H

#include <jni.h>
#include <opencv2/core/types.hpp>

using namespace cv;
using namespace std;

typedef struct FaceDetectResultCV{
    int nFaceCountInOut;
    vector<Rect> *rcFaceRectOut;
} FaceData;
#endif //FINENGINE_FACERESULTCV_H
