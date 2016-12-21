//
// Created by iFinVer on 2016/12/16.
//

#include "xcvcore.h"
#include <string>
#include "inc/faceresult.h"
#include <opencv2/opencv.hpp>
#include <jni.h>
#include "../log.h"
#include "FaceSwapper.h"

using namespace cv;
using namespace std;

#define LOG_TAG "XC OPENCV"

Mat *yuvFrame = NULL;
Mat *rgbFrame = NULL;
FaceSwapper *face_swapper = NULL;
Rect2i *faceRectAnn, *faceRectBob;
vector<Point2i> facePointAnn, facePointBob;

unsigned char *xcv_swapFace(jbyte *data, jint width, jint height, long long int faceDataPtr) {

    if (yuvFrame == NULL) {
        yuvFrame = new Mat(height * 3 / 2, width, CV_8UC1);
        rgbFrame = new Mat(height, width, CV_8UC3, Scalar(255, 255, 255));
        face_swapper = new FaceSwapper();
        faceRectAnn = new Rect2i();
        faceRectBob = new Rect2i();
    }
    yuvFrame->data = (uchar *) data;

    FaceDetectResult *faceData = NULL;
    try {
        if(faceDataPtr != 0) {
            faceData = (FaceDetectResult *) faceDataPtr;
        }
    } catch (...) {
        LOGE("%s", "不能转换出face detect result");
    }
    if (faceData == NULL ) {
//        LOGE("%s", "人脸不够两张");
        return NULL;
    }
    MInt32 faces = faceData->nFaceCountInOut;

    if (faces > 0) {
        // ann face rect
        MRECT rcFace = faceData->rcFaceRectOut[0];
        faceRectAnn->x = rcFace.left;
        faceRectAnn->y = rcFace.top;
        faceRectAnn->width = rcFace.right - rcFace.left;
        faceRectAnn->height = rcFace.bottom - rcFace.top;
    }
    if (faces > 1) {
        // ann face rect
        MRECT rcFace = faceData->rcFaceRectOut[1];
        faceRectBob->x = rcFace.left;
        faceRectBob->y = rcFace.top;
        faceRectBob->width = rcFace.right - rcFace.left;
        faceRectBob->height = rcFace.bottom - rcFace.top;
    }
    // face outline points
    facePointAnn.clear();
    facePointBob.clear();
    if (faces > 0) {
        for (int i = 0; i <= 34; i++) {
            if(i > 24 && i < 29) continue;
            MPOINT ptIndex = faceData->pFaceOutlinePointOut[0 * faceData->faceOutlinePointCount + i];
            facePointAnn.push_back(Point2i(ptIndex.x, ptIndex.y));
            if (faces > 1) {
                ptIndex = faceData->pFaceOutlinePointOut[1 * faceData->faceOutlinePointCount + i];
                facePointBob.push_back(Point2i(ptIndex.x, ptIndex.y));
            }
        }
    }
    cvtColor(*yuvFrame, *rgbFrame, CV_YUV2RGB_NV21);
    if (faces > 1) {
        try {
            face_swapper->swapFaces(*rgbFrame, *faceRectAnn, *faceRectBob, facePointAnn, facePointBob);
        } catch (...) {
            return NULL;
        }
    }
    return rgbFrame->data;
}