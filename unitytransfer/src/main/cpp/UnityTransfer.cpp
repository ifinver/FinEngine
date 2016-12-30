//
// Created by iFinVer on 2016/12/13.
//

#include "UnityTransfer.h"
#include "inc/faceresult.h"
#include "log.h"
#define LOG_TAG "UnityTransfer"

UnityTransfer::UnityTransfer() {
    this->mUnityMsg = new UnityMsg();
    this->mFaceMsg = new FaceMsg();
}

void UnityTransfer::setTransferByUnity(UnityTransfer::Transfer transfer) {
    this->mTransfer = transfer;
}

void UnityTransfer::setFaceTransferByUnity(FaceTransfer transfer) {
    this->mFaceTransfer = transfer;
}

void UnityTransfer::transformToUnity(jbyte *yuvData, int width, int height, int degree,jboolean mirror,jlong facePtr) {
    //宽高改变时重新初始化
    if(mUnityMsg->uvPtr == nullptr || mUnityMsg->width != width || mUnityMsg->height != height){
        mUnityMsg->width = width;
        mUnityMsg->height = height;
        if(mUnityMsg->uvPtr != nullptr){
            delete[] mUnityMsg->uvPtr;
        }
        mUnityMsg->uvPtr = new unsigned char[width * height * 3 / 4];
    }
    //处理uv通道
    int yLen = width * height;
    int yuvLen =yLen * 3 / 2;
    int dstPtr = 0;
    for (int i = yLen; i < yuvLen; i += 2) {
        mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[i] & 0xFF);
        mUnityMsg->uvPtr[dstPtr++] = (unsigned char) (yuvData[i + 1] & 0xFF);
        mUnityMsg->uvPtr[dstPtr++] = 0;
    }
    //y通道赋值
    mUnityMsg->yPtr = yuvData;
    //旋转角度
    mUnityMsg->degree = degree;
    //翻转控制
    mUnityMsg->mirror = mirror ? 1 : 0;

    //处理人脸数据
    FaceDetectResult *faceData = nullptr;
    try {
        if(facePtr != 0) {
            faceData = (FaceDetectResult *) facePtr;
        }
    } catch (...) {
    }
    if (faceData != nullptr) {
        mFaceMsg->degree = degree;
        mFaceMsg->width = width;
        mFaceMsg->height = height;
        mFaceMsg->mirror = mirror ? 1 : 0;
        mFaceMsg->faceCount = faceData->nFaceCountInOut;
        mFaceMsg->faceOutlinePoint = faceData->pFaceOutlinePointOut;
        mFaceMsg->faceDetectRect = faceData->rcFaceRectOut;
        mFaceMsg->faceOrientation = faceData->faceOrientOut;
//        if(faceData->nFaceCountInOut > 0){
//            LOGE("fRoll:%f,fYaw:%f,fPitch:%f",faceData->faceOrientOut[0],faceData->faceOrientOut[1],faceData->faceOrientOut[2]);
//        }
    }else{
        mFaceMsg->faceCount = 0;
    }

    //传送给Unity
    transform();
}



