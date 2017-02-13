package com.ifinver.finengine;

import android.content.Context;
import android.util.Log;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by iFinVer on 2017/2/7.
 * ilzq@foxmail.com
 */

public class FinEffect {
    static {
        System.loadLibrary("fin-engine-lib");
    }
    public static class Monalisa {
        private static boolean inited = false;

        public static boolean init(Context ctx) {
            ctx = ctx.getApplicationContext();
            synchronized (FinEffect.class) {
                //检查文件
                File monaFile = new File(ctx.getFilesDir() + "/face_mei.jpg");
                if (!FinUtils.checkFile(ctx, monaFile)) {
                    Log.e(TAG, "Monalisa初始化失败！");
                    return false;
                }
                File trackFile = new File(ctx.getFilesDir() + "/track.dat");
                if (!FinUtils.checkFile(ctx, trackFile)) {
                    Log.e(TAG, "图片人脸检测初始化失败!无法操作track_data文件");
                    return false;
                }
                //invoke
                inited = nativeInitMonalisa(ctx, monaFile.getAbsolutePath(), trackFile.getAbsolutePath());
                if(inited){
                    Log.w("FinEngine","monalisa inited");
                }
                return inited;
            }
        }

        public static long process(byte[] data, int width, int height, long facePtr) {
            if (inited) {
                return nativeProcessMonalisa(data, width, height, facePtr);
            } else {
                Log.e("FinEngine", "mona have not init yet");
                return 0;
            }
        }

        public static void release() {
            nativeReleaseMonalisa();
            inited = false;
            Log.w("FinEngine","monalisa released");
        }

    }

    private static native boolean nativeInitMonalisa(Context ctx, String monalisaPath, String trackFilePath);

    private static native long nativeProcessMonalisa(byte[] data, int width, int height, long facePtr);

    private static native void nativeReleaseMonalisa();
}
