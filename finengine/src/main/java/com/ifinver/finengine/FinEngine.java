package com.ifinver.finengine;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinEngine {

    static {
        System.loadLibrary("fin-engine-lib");
    }

    private static final String TAG = "FinEngine";
    private static int sFinEngineCount = 0;

    public static final int FILTER_TYPE_NORMAL = 0; //must be zero here.
    public static final int FILTER_TYPE_GREY_SCALE = 1;
    public static final int FILTER_TYPE_SEPIA_STONE = 2;
    public static final int FILTER_TYPE_CYAN = 3;
    public static final int FILTER_TYPE_RADIAL_BLUR = 4;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 5;
    public static final int FILTER_TYPE_V_MIRROR = 6;
    public static final int FILTER_TYPE_H_MIRROR = 7;
    public static final int FILTER_TYPE_FISH_EYE = 8;

    private final FinEngineThread mEngineThread;
    private final int mEngineId;

    private FinEngine(Context ctx,Surface output, int width, int height) {
        mEngineThread = new FinEngineThread(ctx,output, width, height);
        mEngineThread.start();
        mEngineId  = ++sFinEngineCount;
    }

    public static FinEngine prepare(Context ctx,Surface output, int width, int height) {
        return new FinEngine(ctx,output, width, height);
    }

    public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera, long facePtr) {
        mEngineThread.process(data, frameWidth, frameHeight, degree, isFrontCamera,facePtr);
    }

    public void release() {
        mEngineThread.release();
    }

    public void resizeInput(int surfaceWidth, int surfaceHeight) {
        mEngineThread.resizeInput(surfaceWidth, surfaceHeight);
    }

    public void switchFilter(int filterType) {
        mEngineThread.switchFilter(filterType);
    }

    public int getCurrentFilter() {
        return mEngineThread.mFilterType;
    }

    private class FinEngineThread extends HandlerThread implements Handler.Callback {
        private static final String TAG = "FinEngineThread";
        private static final String ROOT_DIR_NAME = "FinEngine";
        private static final String DIR_NAME = "FaceDetection";

        private final int MSG_INIT = 0x101;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;
        private final int MSG_SWITCH_FILTER = 0x106;

        private Handler mSelfHandler;
        private boolean delayStart = false;
        private Surface mOutputSurface;
        private boolean isPrepared;
        private int mFrameWidth;
        private int mFrameHeight;
        private byte[] mData;
        private int mDegree;
        private boolean isFrontCamera;
        private int mOutWidth;
        private int mOutHeight;
        private AssetManager mAssetManager;
        private int mFilterType;
        private long mEngine;
        private long mFacePtr;
        private String faceStickerPath;

        FinEngineThread(Context ctx,Surface output, int width, int height) {
            super("FinEngineThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            this.mAssetManager = ctx.getApplicationContext().getAssets();
            this.mFilterType = FILTER_TYPE_NORMAL;
            this.mOutputSurface = output;
            this.mOutWidth = width;
            this.mOutHeight = height;
            try {
                InputStream faceLibInputStream = ctx.getResources().getAssets().open("monalisa.jpg");
                File dir = new File(Environment.getExternalStorageDirectory(), ROOT_DIR_NAME);
                File faceModelDir = new File(dir, DIR_NAME);
                if (!faceModelDir.exists()) {
                    if (!faceModelDir.mkdirs() || !faceModelDir.canWrite()) {
                        Log.e(TAG, "can not write face detector file");
                    }
                }
                File faceModel = new File(faceModelDir, "glass.png.f");
//                if (!faceModel.exists()) {
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
                    }
//                }
                faceStickerPath = faceModel.getAbsolutePath();
            }catch (Throwable t){
                Log.e(TAG,"",t);
            }
        }

        @Override
        protected void onLooperPrepared() {
            mSelfHandler = new Handler(getLooper(), this);
            Log.w(TAG, "FinEngine"+mEngineId+"开始初始化");
            mSelfHandler.sendEmptyMessage(MSG_INIT);
        }

        public void switchFilter(int filterType) {
            synchronized (FinEngineThread.class) {
                this.mFilterType = filterType;
            }
            mSelfHandler.sendEmptyMessage(MSG_SWITCH_FILTER);
        }

        public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera, long facePtr) {
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mData = data;
            this.mDegree = degree;
            this.isFrontCamera = isFrontCamera;
            this.mFacePtr = facePtr;
            if (mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_PROCESS);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    init();
                    return true;
                case MSG_RELEASE:
                    nativeRelease(mEngine);
                    Log.w(TAG, "FinEngine"+mEngineId+"已释放");
                    sFinEngineCount --;
                    isPrepared = false;
                    return true;
                case MSG_SWITCH_FILTER:
                    switchFilterInternal();
                    return true;
                case MSG_PROCESS:
                    if (isPrepared && mData != null) {
                        nativeRender(mEngine,mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera, mOutWidth, mOutHeight,mFacePtr);
                    }
                    return true;
            }
            return false;
        }

        private void switchFilterInternal() {
            if (isPrepared) {
                Log.w(TAG, "FinEngine"+mEngineId+"开始切换滤镜");
                synchronized (FinEngineThread.class) {
                    FinFiltersManager.Shader shader = FinFiltersManager.findShader(mFilterType);
                    nativeSwitchFilter(mEngine,mAssetManager, mFilterType, shader.vertex, shader.fragment);
                }
            }
        }

        private void init() {
            synchronized (FinEngine.class) {
                mEngine = nativeInit(mOutputSurface, mAssetManager,faceStickerPath);
                isPrepared = mEngine != 0;
                if (isPrepared) {
                    Log.w(TAG, "FinEngine" + mEngineId + "初始化成功");
                } else {
                    Log.e(TAG, "FinEngine" + mEngineId + "初始化失败!");
                    sendReleaseMsg();
                }
            }
        }

        private void sendReleaseMsg() {
            isPrepared = false;
            if (mSelfHandler != null) {
                mSelfHandler.sendEmptyMessage(MSG_RELEASE);
            } else {
                nativeRelease(mEngine);
                Log.w(TAG, "FinEngine"+mEngineId+"已释放");
                sFinEngineCount --;
                isPrepared = false;
            }

        }

        public void release() {
            Log.w(TAG, "FinEngine"+mEngineId+"开始释放");
            sendReleaseMsg();
        }

        public void resizeInput(int surfaceWidth, int surfaceHeight) {
            this.mOutWidth = surfaceWidth;
            this.mOutHeight = surfaceHeight;
        }
    }


    /**
     * @return 0 means failed
     */
    private native long nativeInit(Surface output,AssetManager mAssetManager,String stickerPath);

    private native void nativeRelease(long engine);

    private native void nativeSwitchFilter(long engine,AssetManager mAssetManager, int mFilterType, String mVertexName, String mFragmentName);

    private native void nativeRender(long engine, byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror, int mOutWidth, int mOutHeight, long mFacePtr);
}
