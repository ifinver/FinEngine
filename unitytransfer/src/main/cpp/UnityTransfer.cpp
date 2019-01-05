//
// Created by iFinVer on 2016/12/13.
//

#include "UnityTransfer.h"
#include "inc/faceresult.h"
#include "log.h"
#include <cmath>

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

void UnityTransfer::setMonalisaTransferByUnity(MonalisaTransfer transfer) {
    this->mMonalisaTransfer = transfer;
}

#ifdef USE_UNITY_NATIVE_SEND_MESSAGE
void UnityTransfer::setVideoRecordActionTransferByUnity(VideoRecordActionTransfer transfer) {
    this->mVideoRecordActionTransfer = transfer;
}

void UnityTransfer::setEnableBlurTransferByUnity(EnableBlurTransfer transfer) {
    this->mEnableBlurTransfer = transfer;
}

void UnityTransfer::setPauseAssetAudioTransferByUnity(PauseAssetAudioTransfer transfer) {
    this->mPauseAssetAudioTransfer = transfer;
}

void UnityTransfer::setInitAssetsLoaderTransferByUnity(InitAssetsLoaderTransfer transfer) {
    this->mInitAssetsLoaderTransfer = transfer;
}

void UnityTransfer::setSelectAssetTransferByUnity(SelectAssetTransfer transfer) {
    this->mSelectAssetTransfer = transfer;
}

void UnityTransfer::setCleanUpAssetsCacheTransferByUnity(CleanUpAssetsCacheTransfer transfer) {
    this->mCleanUpAssetsCacheTransfer = transfer;
}
#endif

int dis(const MPOINT *p1, const MPOINT *p2) {
    return (int) std::sqrt((p2->x - p1->x) * (p2->x - p1->x) + (p2->y - p1->y) * (p2->y - p1->y));
}

void UnityTransfer::transformToUnity(jbyte *yuvData, int width, int height, int degree, jboolean mirror, jlong facePtr) {
    //宽高改变时重新初始化
    if (mUnityMsg->uvPtr == nullptr || mUnityMsg->width != width || mUnityMsg->height != height) {
        mUnityMsg->width = width;
        mUnityMsg->height = height;
        if (mUnityMsg->uvPtr != nullptr) {
            delete[] mUnityMsg->uvPtr;
        }
        mUnityMsg->uvPtr = new unsigned char[width * height * 3 / 4];
    }
    //处理uv通道
    int yLen = width * height;
    int yuvLen = yLen * 3 / 2;
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
        if (facePtr != 0) {
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
////            MFloat fRoll = faceData->faceOrientOut[0];
////            MFloat fRaw = faceData->faceOrientOut[1];
////            MFloat fPitch = faceData->faceOrientOut[2];
//            MRECT rect = faceData->rcFaceRectOut[0];
////            LOGE("fRoll=%.3f,fRaw=%.3f,fPitch=%.3f,degree=%d,rectWidth=%d,rectHeight=%d",fRoll,fRaw,fPitch,degree,rect.right - rect.left,rect.bottom - rect.top);
//            MPOINT &p97 = faceData->pFaceOutlinePointOut[97];
//            MPOINT &p99 = faceData->pFaceOutlinePointOut[99];
//            MPOINT &p95 = faceData->pFaceOutlinePointOut[95];
//            MPOINT &p96 = faceData->pFaceOutlinePointOut[96];
//            LOGE("rectWidth=%d,rectHeight=%d,dis97-99=%d,dis95-96=%d",rect.right - rect.left,rect.bottom - rect.top,dis(&p97,&p99),dis(&p95,&p96));
//        }

    } else {
        mFaceMsg->faceCount = 0;
    }

    //传送给Unity
    transform();
}

void UnityTransfer::transformMonalisa(jlong monalisaMsgPtr) {
    if (mMonalisaTransfer != nullptr && monalisaMsgPtr != 0) {
        MonalisaMsg *msg = (MonalisaMsg *) monalisaMsgPtr;
//        if (mMonalisaMsg == nullptr) {
//            mMonalisaMsg = new MonalisaMsg();
//            mMonalisaMsg->width = msg->cols;
//            mMonalisaMsg->height = msg->rows;
//            mMonalisaMsg->texSize = mMonalisaMsg->width*mMonalisaMsg->height*3;
//        }
//        cv::cvtColor(*msg, temp, CV_BGR2RGB);
//        //旋转180
//        if(matrix == nullptr){
//            matrix = new float[6];
//            CvMat M = cvMat( 2, 3, CV_32F, matrix);
//            CvPoint2D32f center = CvPoint2D32f(temp.cols/2,temp.rows/2);
//            cv2DRotationMatrix( center, 180,1, &M);
//            cvWarpAffine(img,img_rotate, &M,CV_INTER_LINEAR+CV_WARP_FILL_OUTLIERS,cvScalarAll(0) );
//        }
//        mMonalisaMsg->texData = temp.data;
        mMonalisaTransfer(msg);
    }
}
