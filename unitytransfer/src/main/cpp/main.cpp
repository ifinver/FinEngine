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
        JNIEnv *env, jclass , jbyteArray data_, jint frameWidth, jint frameHeight, jint degree) {
    jbyte *data = env->GetByteArrayElements(data_, 0);
    if(mConnector != nullptr){
        mConnector->transformToUnity(data,frameWidth,frameHeight,degree);
    }
    env->ReleaseByteArrayElements(data_, data, JNI_ABORT);
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

