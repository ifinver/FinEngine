//
// Created by iFinVer on 2016/12/28.
//

#include "monalisa.h"
#include "../../facedetect/arcsoft/arcsoft.h"
#include "../../log.h"
#include <string>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>

using namespace cv;

#define LOG_TAG "FinEngine"

ArcSoftSpotlight *mMonaDetector;
std::string gMonaPath("");
cv::Mat gMonaLisaMat;

int initMonalisa(JNIEnv *env, jobject ctx, jstring trackDataPath) {
    if (mMonaDetector == NULL) {
        mMonaDetector = new ArcSoftSpotlight(1);
        const char *path = env->GetStringUTFChars(trackDataPath, 0);
        int ret = mMonaDetector->init(env, ctx, path);
        env->ReleaseStringUTFChars(trackDataPath, path);
        return ret;
    }
    return 0;
}

jlong detectMonaFace(const char *monaPath) {
    jlong result = 1;
    if(gMonaPath != monaPath){
        gMonaLisaMat = cv::imread(monaPath);
        if(mMonaDetector != NULL){
            result = mMonaDetector->process(gMonaLisaMat.data,gMonaLisaMat.cols,gMonaLisaMat.rows,1);
            if(result != 0) {
                gMonaPath = monaPath;
            }
        }
    }
    return result;
}

void releaseMonalisa() {
    if (mMonaDetector != NULL) {
        delete mMonaDetector;
        mMonaDetector = NULL;
    }
}


cv::Mat *effect_monaLisa(jbyte *data, jint width, jint height) {

    FaceDetectResult *faceData = (FaceDetectResult *) mMonaDetector->getFaceDataPtr();
    MInt32 localFaces = faceData->nFaceCountInOut;
    if (localFaces > 0) {

        for (int j = 0; j < localFaces; j++) {
            for (int i = 0; i < faceData->faceOutlinePointCount; i++) {
                MPOINT ptIndex = faceData->pFaceOutlinePointOut[j * faceData->faceOutlinePointCount + i];

                cv::circle(gMonaLisaMat,Point(ptIndex.x,ptIndex.y),2,Scalar(0,255,0),-1);
            }
        }

    }
    return &gMonaLisaMat;
}