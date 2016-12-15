//
// Created by Administrator on 2016/12/6 0006.
//

#include <android/log.h>
#include "arcsoft.h"
#include "../log.h"
#include "inc/arcsoft_spotlight.h"

#define LOG_TAG "face detect"

ArcSoftSpotlight::ArcSoftSpotlight() {
    m_hEngine = MNull;
    skinFaceLevel = 100;
    brightLevel = 49;
    processModel = ASL_PROCESS_MODEL_FACEOUTLINE | ASL_PROCESS_MODEL_FACEBEAUTY;
}

ArcSoftSpotlight::~ArcSoftSpotlight() {
    if (m_hEngine != MNull) {
        ASL_Uninitialize(m_hEngine);
        ASL_DestroyEngine(m_hEngine);
        m_hEngine = MNull;
    }
    if (faceDetectResult != MNull) {
        delete[] faceDetectResult->pFaceOutlinePointOut;
        delete[] faceDetectResult->rcFaceRectOut;
        delete[] faceDetectResult->faceOrientOut;
        delete faceDetectResult;
        faceDetectResult = MNull;
    }
}

int ArcSoftSpotlight::init(JNIEnv *env, jobject context,
                           const char *trackDataPath) {
    //创建引擎
    if (m_hEngine != MNull) {
        LOGE("%s", "美颜库已经初始化过了！");
        return -1;
    }
    m_hEngine = ASL_CreateEngine();
    if (m_hEngine == MNull) {
        return -1;
    }
    //初始化引擎
    MRESULT hRet = ASL_Initialize(m_hEngine, trackDataPath, ASL_MAX_FACE_NUM, (MVoid *) env, (MVoid **) &context);
    if (hRet == MOK) {
        ASL_SetProcessModel(m_hEngine, processModel);
        ASL_SetFaceSkinSoftenLevel(m_hEngine, skinFaceLevel);
        ASL_SetFaceBrightLevel(m_hEngine, brightLevel);
    } else {
        return -1;
    }

    //变量初始化
    OffScreenIn.u32PixelArrayFormat = ASVL_PAF_NV21;

    faceDetectResult = new FaceDetectResult();
    faceDetectResult->pFaceOutlinePointOut = new MPOINT[ASL_MAX_FACE_NUM * ASL_GetFaceOutlinePointCount()];
    faceDetectResult->rcFaceRectOut = new MRECT[ASL_MAX_FACE_NUM];
    faceDetectResult->faceOrientOut = new MFloat[ASL_MAX_FACE_NUM * 3];;

    return 0;
}

void ArcSoftSpotlight::setProcessModel(long model) {
    processModel = (MUInt32) model;
    ASL_SetProcessModel(m_hEngine, processModel);
}

void ArcSoftSpotlight::setFaceSkinSoftenLevel(long skinLevel) {
    skinFaceLevel = skinLevel;
    ASL_SetFaceSkinSoftenLevel(m_hEngine, skinFaceLevel);
}

void ArcSoftSpotlight::setFaceBrightLevel(long _brightLevel) {
    brightLevel = _brightLevel;
    ASL_SetFaceBrightLevel(m_hEngine, brightLevel);
}

jlong ArcSoftSpotlight::process(jbyte *data, jint width, jint height) {
    if (m_hEngine == MNull) {
        return 0;
    }

    OffScreenIn.i32Width = width;
    OffScreenIn.i32Height = height;
    OffScreenIn.pi32Pitch[0] = width;
    OffScreenIn.pi32Pitch[1] = width;
    OffScreenIn.ppu8Plane[0] = (MUInt8 *) data;
    OffScreenIn.ppu8Plane[1] = (MUInt8 *) (data + width * height);

    faceDetectResult->nFaceCountInOut = ASL_MAX_FACE_NUM;

    MRESULT hr = ASL_Process(m_hEngine,
                             &OffScreenIn,
                             MNull,
                             &faceDetectResult->nFaceCountInOut,
                             faceDetectResult->pFaceOutlinePointOut,
                             faceDetectResult->rcFaceRectOut,
                             faceDetectResult->faceOrientOut);

    if (hr == MOK) {
        return (jlong) faceDetectResult;
    } else {
        return 0;
    }

//    if (hr == MOK) {
//        if (nFaceCountInOut > 0) {
//            for (int nFaceIndex = 0; nFaceIndex < nFaceCountInOut; nFaceIndex++) {
//                // face rect
//                MRECT rcFace = rcFaceRectOut[nFaceIndex];
//                // face orientation
//                MFloat fRoll = faceOrientOut[nFaceIndex * 3 + 0];
//                MFloat fYaw = faceOrientOut[nFaceIndex * 3 + 1];
//                MFloat fPitch = faceOrientOut[nFaceIndex * 3 + 2];
//                // face outline points
//                for (int i = 0; i < ASL_GetFaceOutlinePointCount(); i++) {
//                    MPOINT ptIndex = pFaceOutlinePointOut[nFaceIndex * ASL_GetFaceOutlinePointCount() + i];
//                }
//            }
//        }
//    }
}