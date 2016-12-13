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
        void* yPtr;
        unsigned char* uvPtr = nullptr;
    } UnityMsg;
    typedef void (*Transfer)(void * intPtr);

    UnityTransfer();
    void setTransferByUnity(Transfer);
    void transformToUnity(jbyte*,int width,int height,int degree);
    //will never be destroyed after app starting.
//    ~UnityConnector();
private:
    UnityMsg *mUnityMsg = nullptr;
    Transfer mTransfer = nullptr;
    inline void transform(){
        if(mTransfer != nullptr && mUnityMsg != nullptr){
            mTransfer(mUnityMsg);
        }
    }
};

#endif //FINENGINE_UNITYCONNECTOR_H
