//
// Created by Administrator on 2016/12/6 0006.
//

#ifndef FINENGINE_ARCSOFTSPOTLIGHT_H
#define FINENGINE_ARCSOFTSPOTLIGHT_H

#include <jni.h>
#include <cwchar>
#include <faceresult.h>
#include "inc/amcomdef.h"
#include "inc/asvloffscreen.h"

class ArcSoftSpotlight {
public:
    ArcSoftSpotlight();

    ~ArcSoftSpotlight();

    int init(JNIEnv *env, jobject context, const char *trackDataPath);

    jlong process(void *data, int width, int height);

    void setProcessModel(long model);

    void setFaceSkinSoftenLevel(long skinLevel);

    void setFaceBrightLevel(long brightLevel);

    /**
     * 返回值  -1 means 未初始化；-2 means 人脸检测出错； 1 means 未检测出人脸； 0 success
     */
    int processSingleFrame(void *data, int width, int height, MPOINT *faceOutlinePointOut, MRECT *faceRectOut, MFloat *faceOrientOut);

private:
    MHandle m_hEngine;
    int skinFaceLevel;
    int brightLevel;
    MUInt32 processModel;
    FaceDetectResult *faceDetectResult;
    ASVLOFFSCREEN OffScreenIn;
};
#endif //FINENGINE_ARCSOFTSPOTLIGHT_H
