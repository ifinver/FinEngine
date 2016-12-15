//
// Created by Administrator on 2016/12/6 0006.
//

#ifndef FINENGINE_ARCSOFTSPOTLIGHT_H
#define FINENGINE_ARCSOFTSPOTLIGHT_H

#include <jni.h>
#include <cwchar>
#include "inc/amcomdef.h"
#include "inc/asvloffscreen.h"

struct FaceDetectResult{
    MPOINT* pFaceOutlinePointOut;
    MInt32 nFaceCountInOut;
    MRECT *rcFaceRectOut;
    MFloat *faceOrientOut;
};

class ArcSoftSpotlight {
public:
    ArcSoftSpotlight();

    ~ArcSoftSpotlight();

    int init(JNIEnv *env, jobject context, const char *trackDataPath);

    jlong process(jbyte *data, jint width, jint height);

    void setProcessModel(long model);

    void setFaceSkinSoftenLevel(long skinLevel);

    void setFaceBrightLevel(long brightLevel);

private:
    MHandle m_hEngine;
    int skinFaceLevel;
    int brightLevel;
    MUInt32 processModel;
    FaceDetectResult *faceDetectResult;
    ASVLOFFSCREEN OffScreenIn;
};
#endif //FINENGINE_ARCSOFTSPOTLIGHT_H
