//
// Created by iFinVer on 2016/12/13.
//

#ifndef FINENGINE_MAIN_H
#define FINENGINE_MAIN_H

#include "UnityTransfer.h"

extern "C" {

/**
 * will be invoked by java code
 */
JNIEXPORT void JNICALL Java_com_ifinver_unitytransfer_UnityTransfer_onVideoBuffer(JNIEnv *, jclass, jbyteArray, jint, jint, jint,jboolean);

/**
 * will be invoked by unity's scripts
 */
void setTransferByUnity(UnityTransfer::Transfer);

}

#endif //FINENGINE_MAIN_H
