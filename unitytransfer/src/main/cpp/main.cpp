//
// Created by iFinVer on 2016/12/13.
//

#include "main.h"
#include "UnityTransfer.h"


UnityTransfer *mConnector = nullptr;

/**
 * will be invoked by java code
 */
JNIEXPORT void JNICALL Java_com_ifinver_unitytransfer_UnityTransfer_onVideoBuffer(
        JNIEnv *env, jclass , jbyteArray data_, jint frameWidth, jint frameHeight, jint degree,jboolean mirror,jlong facePtr) {
    jbyte *data = env->GetByteArrayElements(data_, 0);
    if(mConnector != nullptr){
        mConnector->transformToUnity(data,frameWidth,frameHeight,degree,mirror,facePtr);
    }
    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_ifinver_unitytransfer_UnityTransfer_onMonalisaData(JNIEnv *env, jclass type, jlong msgPtr) {
    if(mConnector != nullptr){
        mConnector->transformMonalisa(msgPtr);
    }
}

/**
 * will be invoked by unity's scripts
 */
void setTransferByUnity(UnityTransfer::Transfer transfer) {
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setTransferByUnity(transfer);
}

void setFaceTransferByUnity(UnityTransfer::FaceTransfer transfer){
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setFaceTransferByUnity(transfer);
}

void setMonalisaCallbackByUnity(UnityTransfer::MonalisaTransfer transfer){
    if (mConnector == nullptr) {
        mConnector = new UnityTransfer();
    }
    mConnector->setMonalisaTransferByUnity(transfer);
}
