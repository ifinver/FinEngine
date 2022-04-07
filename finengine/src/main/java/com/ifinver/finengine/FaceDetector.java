package com.ifinver.finengine;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by iFinVer on 2016/12/15.
 * ilzq@foxmail.com
 */

public class FaceDetector {
    private static final String TAG = "FaceDetector";
    private static final String ROOT_DIR_NAME = "FinEngine";
    private static final String DIR_NAME = "FaceDetection";
    static {
        System.loadLibrary("fin-engine-lib");
    }

    private static boolean initialized = false;

    public static boolean init(Context ctx){
        if(!initialized) {
            InputStream faceLibInputStream = ctx.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File dir = new File(Environment.getExternalStorageDirectory(), ROOT_DIR_NAME);
            File faceModelDir = new File(dir,DIR_NAME);
            if(!faceModelDir.exists()){
                if (!faceModelDir.mkdirs() || !faceModelDir.canWrite()) {
                    Log.e(TAG,"can not write face detector file");
                    return false;
                }
            }
            File faceModel = new File(faceModelDir,"haarcascade_frontalface.f");
            if(!faceModel.exists()) {
                try {
                    FileOutputStream out = new FileOutputStream(faceModel);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = faceLibInputStream.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                    out.close();
                    faceLibInputStream.close();
                } catch (Throwable t) {
                    Log.e(TAG, "can not write face detector file", t);
                    return false;
                }
            }
            int ret = nativeInit(ctx,faceModel.getAbsolutePath());
            if (ret != 0) {
                Log.e(TAG, "人脸检测初始化失败!");
                initialized = false;
            }else{
                Log.d(TAG, "人脸检测初始化成功!");
                initialized = true;
            }
        }else{
            Log.e(TAG,"人脸检测已经初始化过了");
        }
        return initialized;
    }

    public static long process(byte[] data,int width,int height){
        if(initialized) {
            return nativeProcess(data, width, height);
        }else{
            return 0;
        }
    }

    public static void release(){
        nativeRelease();
        initialized = false;
        Log.d(TAG,"人脸检测已释放");
    }

    private static native int nativeInit(Context ctx, String absolutePath);
    private static native long nativeProcess(byte[] data, int width, int height);
    private static native void nativeRelease();
    public static native void decodePNGData(String filePath);
}
