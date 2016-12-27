package com.ifinver.finengine;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by iFinVer on 2016/12/15.
 * ilzq@foxmail.com
 */

public class FaceDetector {
    public static int ASL_PROCESS_MODEL_FACEOUTLINE = 1;
    public static int ASL_PROCESS_MODEL_FACEBEAUTY = 2;
    static {
        System.loadLibrary("fin-engine-lib");
    }

    private static final String TAG = "FaceDetector";

    private static boolean initialized = false;

    public static boolean init(Context ctx){
        if(!initialized) {
            //检查文件
            File trackFile = new File(ctx.getFilesDir()+"/track_data.dat");
            if(!trackFile.exists()){
                //不存在了
                try {
                    InputStream in = ctx.getAssets().open("track_data.dat");
                    FileOutputStream fos = new FileOutputStream(trackFile);
                    byte[] buffer = new byte[1024];
                    int readCount;
                    while ((readCount = in.read(buffer)) != -1){
                        fos.write(buffer,0,readCount);
                    }
                    in.close();
                    fos.close();
                }catch (Exception ignored){
                    Log.e(TAG, "人脸检测初始化失败!无法操作track_data文件");
                    return false;
                }
            }

            int ret = nativeInit(ctx,trackFile.getAbsolutePath());
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

    public static void setProcessModel(long model) {
        if(initialized) {
            nativeSetProcessModel(model);
        }
    }

    public static void setFaceBrightLevel(int brightLevel) {
        if(initialized) {
            nativeSetFaceBrightLevel(brightLevel);
        }
    }

    public static void setFaceSkinSoftenLevel(int skinSoftenLevel) {
        if(initialized) {
            nativeSetFaceSkinSoftenLevel(skinSoftenLevel);
        }
    }


    private static native int nativeInit(Context ctx, String absolutePath);

    private static native long nativeProcess(byte[] data, int width, int height);

    private static native void nativeRelease();

    private static native void nativeSetProcessModel(long model);
    private static native void nativeSetFaceBrightLevel(int brightLevel);
    private static native void nativeSetFaceSkinSoftenLevel(int skinSoftenLevel);

}
