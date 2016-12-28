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

    ArcSoftSpotlight(int);

    ~ArcSoftSpotlight();

    int init(JNIEnv *env, jobject context, const char *trackDataPath);

    jlong process(void *data, int width, int height,int format);

    void setProcessModel(long model);

    void setFaceSkinSoftenLevel(long skinLevel);

    void setFaceBrightLevel(long brightLevel);

    jlong getFaceDataPtr();

private:
    MHandle m_hEngine;
    int skinFaceLevel;
    int brightLevel;
    MUInt32 processModel;
    FaceDetectResult *faceDetectResult;
};
#endif //FINENGINE_ARCSOFTSPOTLIGHT_H
