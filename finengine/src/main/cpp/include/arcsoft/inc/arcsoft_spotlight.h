/*******************************************************************************
 Copyright(c) ArcSoft, All right reserved.
 
 This file is ArcSoft's property. It contains ArcSoft's trade secret, proprietary
 and confidential information.
 
 The information and code contained in this file is only for authorized ArcSoft
 employees to design, create, modify, or review.
 
 DO NOT DISTRIBUTE, DO NOT DUPLICATE OR TRANSMIT IN ANY FORM WITHOUT PROPER
 AUTHORIZATION.
 
 If you are not an intended recipient of this file, you must not copy,
 distribute, modify, or take any action in reliance on it.
 
 If you have received this file in error, please immediately notify ArcSoft and
 permanently delete the original and any copy of any file and any printout
 thereof.
 *******************************************************************************/

#ifndef     _ARCSOFT_SPOTLIGHT_H_
#define     _ARCSOFT_SPOTLIGHT_H_

#ifdef __cplusplus
extern "C" {
#endif
    
#include "asvloffscreen.h"
#include "merror.h"

#define ASL_MERR_BUNDLEID_ERROR                 0X8000
#define ASL_MERR_PROCESSMODEL_UNSUPPORT         0X8001
    
#define ASL_MAX_FACE_NUM   4
    
#define ASL_PROCESS_MODEL_NONE          0x00000000
#define ASL_PROCESS_MODEL_FACEOUTLINE   0x00000001
#define ASL_PROCESS_MODEL_FACEBEAUTY    0x00000002
    
MHandle         ASL_CreateEngine();
MVoid           ASL_DestroyEngine(MHandle hHandle);
    
MRESULT         ASL_Initialize(MHandle hHandle,const MChar* szTrackDataPath,MUInt32 nProcessFaceCount,MVoid* JNIEnv, MVoid** jcontext);
MRESULT         ASL_Uninitialize(MHandle hHandle);
    
MRESULT         ASL_SetProcessModel(MHandle hHandle,MUInt32 nProcessModel);
    
MVoid           ASL_SetFaceSkinSoftenLevel(MHandle hHandle,MLong lLevel); // 0-100
MVoid           ASL_SetFaceBrightLevel(MHandle hHandle,MLong lLevel); // 0-100
    
MUInt32         ASL_GetFaceOutlinePointCount();
MRESULT         ASL_Process(MHandle hHandle,
                                LPASVLOFFSCREEN pOffScreenIn,
                                LPASVLOFFSCREEN pOffScreenOut,
                                MInt32* pFaceCountInOut,
                                MPOINT* pFaceOutlinePointOut,
                                MRECT*  pFaceRectOut,
                                MFloat* pFaceOrientOut
                                );
const ASVL_VERSION* ASL_GetVersion();
    
#ifdef __cplusplus
}

#endif

#endif

