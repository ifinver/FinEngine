//
// Created by iFinVer on 2016/12/15.
//

#ifndef FINENGINE_FACERESULTCV_H
#define FINENGINE_FACERESULT_H

#include "amcomdef.h"

typedef struct FaceDetectResult{
    MInt32 nFaceCountInOut;
    MUInt32 faceOutlinePointCount;
    MPOINT* pFaceOutlinePointOut;
    MRECT *rcFaceRectOut;
    MFloat *faceOrientOut;
} FaceData;
#endif //FINENGINE_FACERESULTCV_H
