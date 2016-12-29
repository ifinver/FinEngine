//
// Created by iFinVer on 2016/12/28.
//

#include "monalisa.h"
#include "../../facedetect/arcsoft/arcsoft.h"
#include "../../log.h"
#include "MonaLisaEffect.h"

#define LOG_TAG "FinEngine"

ArcSoftSpotlight *gMonaDetector;
MonaLisaEffect *gMonaLisaEffect;
std::string gMonaPath("");
cv::Mat gMonaLisaMat;
cv::Mat gLisaSmallFrame;
cv::Mat gLisaSmallOriFrame;
cv::Mat *gVinciYuvMat;
cv::Mat *gVinciRGBMat;
cv::Rect gLisaRect;
cv::Rect gVinciRect;
std::vector<Point2i> gLisaPoints;
std::vector<Point2i> gVinciPoints;

int initMonalisa(JNIEnv *env, jobject ctx, jstring trackDataPath) {
    if (gMonaDetector == NULL) {
        gMonaDetector = new ArcSoftSpotlight(1);
        const char *path = env->GetStringUTFChars(trackDataPath, 0);
        int ret = gMonaDetector->init(env, ctx, path);
        env->ReleaseStringUTFChars(trackDataPath, path);

        gMonaLisaEffect = new MonaLisaEffect();
        return ret;
    }
    return 0;
}

jlong detectMonaFace(const char *monaPath) {
    jlong result = 1;
    if(gMonaPath != monaPath){
        gMonaLisaMat = cv::imread(monaPath);
        if(gMonaDetector != NULL){
            result = gMonaDetector->process(gMonaLisaMat.data,gMonaLisaMat.cols,gMonaLisaMat.rows,1);
            if(result != 0) {
                FaceDetectResult *lisaFaceData = (FaceDetectResult *) gMonaDetector->getFaceDataPtr();
                MInt32 localFaces = lisaFaceData->nFaceCountInOut;
                if (localFaces > 0) {
                    MRECT rcFace = lisaFaceData->rcFaceRectOut[0];
                    gLisaRect.x = rcFace.left;
                    gLisaRect.y = rcFace.top;
                    gLisaRect.width = rcFace.right - rcFace.left;
                    gLisaRect.height = rcFace.bottom - rcFace.top;
                    gLisaSmallFrame = gMonaLisaMat(gLisaRect);
                    gLisaSmallFrame.copyTo(gLisaSmallOriFrame);
                    gLisaPoints.clear();
                    for (int i = 0; i <= 34; i++) {
                        if (i > 21 && i < 32) continue;
                        MPOINT ptIndex = lisaFaceData->pFaceOutlinePointOut[i];
                        gLisaPoints.push_back(Point2i(ptIndex.x, ptIndex.y));
                    }
                }
                gMonaPath = monaPath;
            }
        }
    }
    return result;
}

cv::Mat *effect_monaLisa(jbyte *data, jint width, jint height, jlong facePtr) {
    //复原蒙娜丽莎的图片
    gLisaSmallOriFrame.copyTo(gLisaSmallFrame);

    FaceDetectResult *faceData = NULL;
    try {
        if (facePtr != 0) {
            faceData = (FaceDetectResult *) facePtr;
        }
    } catch (...) {
        LOGE("%s", "不能转换出face detect result");
    }
    if (faceData == NULL || faceData->nFaceCountInOut < 1) {
        return &gMonaLisaMat;
    }

    //转换用户数据
    if (gVinciYuvMat == NULL) {
        gVinciYuvMat = new Mat(height * 3 / 2, width, CV_8UC1);
        gVinciRGBMat = new Mat(height, width, CV_8UC3, Scalar(255, 255, 255));
    }
    gVinciYuvMat->data = (uchar *) data;
    cvtColor(*gVinciYuvMat, *gVinciRGBMat, CV_YUV2BGR_NV21);

    // ann face rect
    MRECT rcFace = faceData->rcFaceRectOut[0];
    gVinciRect.x = rcFace.left;
    gVinciRect.y = rcFace.top;
    gVinciRect.width = rcFace.right - rcFace.left;
    gVinciRect.height = rcFace.bottom - rcFace.top;

    gVinciPoints.clear();
    for (int i = 0; i <= 34; i++) {
        if (i > 21 && i < 32) continue;
        MPOINT ptIndex = faceData->pFaceOutlinePointOut[i];
        gVinciPoints.push_back(Point2i(ptIndex.x, ptIndex.y));
    }
    gMonaLisaEffect->monaLisa(gMonaLisaMat,gLisaRect,gLisaPoints,*gVinciRGBMat,gVinciRect,gVinciPoints);
    return &gMonaLisaMat;
}

void releaseMonalisa() {
    if (gMonaDetector != NULL) {
        delete gMonaDetector;
        gMonaDetector = NULL;
    }
}