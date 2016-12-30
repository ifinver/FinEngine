//
// Created by iFinVer on 2016/12/13.
//

#ifndef FINENGINE_UNITYCONNECTOR_H
#define FINENGINE_UNITYCONNECTOR_H

#include <jni.h>

class UnityTransfer{
public:
    typedef struct UnityTransferMessage{
        int degree;
        int width;
        int height;
        int mirror;
        void* yPtr;
        unsigned char* uvPtr = nullptr;
    } UnityMsg;
    typedef void (*Transfer)(void * intPtr);

    typedef struct UnityFaceMessage{
        int degree;
        int width;
        int height;
        int mirror;
        int faceCount;
        void* faceOutlinePoint;
        void* faceDetectRect;
        void* faceOrientation;
    }FaceMsg;
    typedef void (*FaceTransfer)(void * intPtr);

    UnityTransfer();
    void setTransferByUnity(Transfer);
    void setFaceTransferByUnity(FaceTransfer);
    void transformToUnity(jbyte*,int width,int height,int degree,jboolean mirror,jlong facePtr);
    //will never be destroyed after app starting.
//    ~UnityConnector();
private:
    UnityMsg *mUnityMsg = nullptr;
    Transfer mTransfer = nullptr;

    FaceMsg *mFaceMsg = nullptr;
    FaceTransfer mFaceTransfer = nullptr;
    inline void transform(){
        if(mTransfer != nullptr && mUnityMsg != nullptr){
            mTransfer(mUnityMsg);
        }
        if(mFaceTransfer != nullptr && mFaceMsg != nullptr){
            mFaceTransfer(mFaceMsg);
        }
    }
};

#endif //FINENGINE_UNITYCONNECTOR_H
