package com.ifinver.finengine;

import android.content.Context;
import android.content.res.AssetManager;
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

    private FinEngine(Surface output, int width, int height) {
        mEngineThread = new FinEngineThread(output, width, height);
        mEngineThread.start();
        mEngineId = ++sFinEngineCount;
    }

    public static FinEngine prepare(Surface output, int width, int height) {
        return new FinEngine(output, width, height);
    }

    public void process(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera, long facePtr) {
        mEngineThread.process(data, frameWidth, frameHeight, degree, isFrontCamera, facePtr);
    }

    public void release() {
        mEngineThread.release();
    }

    public void resizeInput(int surfaceWidth, int surfaceHeight) {
        mEngineThread.resizeInput(surfaceWidth, surfaceHeight);
    }

    public void switchFilter(Context ctx, int filterType) {
        mEngineThread.switchFilter(ctx, filterType);
    }

    public void switchModeToNormal(){
        mEngineThread.switchModeToNormal();
    }

    public void switchModeToSwapFace(){
        mEngineThread.switchModeToSwapFace();
    }

    public void switchModeToMonaLisa(Context ctx) {
        synchronized (this) {
            File monaFile = new File(ctx.getFilesDir() + "/monalisa.jpg");
            if (!monaFile.exists()) {
                //不存在了
                try {
                    InputStream in = ctx.getAssets().open("monalisa.jpg");
                    FileOutputStream fos = new FileOutputStream(monaFile);
                    byte[] buffer = new byte[1024];
                    int readCount;
                    while ((readCount = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, readCount);
                    }
                    in.close();
                    fos.close();
                } catch (Exception ignored) {
                    Log.e(TAG, "切换引擎模式失败！", ignored);
                }
            }
            mEngineThread.switchModeToMonaLisa(ctx,monaFile.getAbsolutePath());
        }
    }

    public int getCurrentFilter() {
        return mEngineThread.mFilterType;
    }

    private class FinEngineThread extends HandlerThread implements Handler.Callback {
        private final int MSG_INIT = 0x101;
        private final int MSG_RELEASE = 0x104;
        private final int MSG_PROCESS = 0x105;
        private final int MSG_SWITCH_FILTER = 0x106;
        private final int MSG_SWITCH_MODE_NORMAL = 0x107;
        private final int MSG_SWITCH_MODE_MONALISA = 0x108;
        private final int MSG_SWITCH_MODE_SWAP_FACE = 0x109;

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
        private String mMonaLisaFilePath;
        private Context mAppCtx;
        private String mTrackDataFilePath;

        FinEngineThread(Surface output, int width, int height) {
            super("FinEngineThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            this.mFilterType = FILTER_TYPE_NORMAL;
            this.mOutputSurface = output;
            this.mOutWidth = width;
            this.mOutHeight = height;
        }

        @Override
        protected void onLooperPrepared() {
            mSelfHandler = new Handler(getLooper(), this);
            Log.w(TAG, "FinEngine" + mEngineId + "开始初始化");
            mSelfHandler.sendEmptyMessage(MSG_INIT);
        }

        public void switchFilter(Context ctx, int filterType) {
            synchronized (FinEngineThread.class) {
                this.mAssetManager = ctx.getApplicationContext().getAssets();
                this.mFilterType = filterType;
            }
            mSelfHandler.removeCallbacksAndMessages(null);
            mSelfHandler.sendEmptyMessage(MSG_SWITCH_FILTER);
        }

        public void switchModeToNormal(){
            mSelfHandler.removeCallbacksAndMessages(null);
            mSelfHandler.sendEmptyMessage(MSG_SWITCH_MODE_NORMAL);
        }

        public void switchModeToSwapFace() {
            mSelfHandler.removeCallbacksAndMessages(null);
            mSelfHandler.sendEmptyMessage(MSG_SWITCH_MODE_SWAP_FACE);
        }

        public void switchModeToMonaLisa(Context ctx,String path) {
            this.mAppCtx = ctx.getApplicationContext();
            synchronized (FinEngineThread.class) {
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
                        Log.e(TAG, "图片人脸检测初始化失败!无法操作track_data文件");
                        return ;
                    }
                }
                this.mMonaLisaFilePath = path;
                this.mTrackDataFilePath = trackFile.getAbsolutePath();
                mSelfHandler.removeCallbacksAndMessages(null);
                mSelfHandler.sendEmptyMessage(MSG_SWITCH_MODE_MONALISA);
            }
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
                    Log.w(TAG, "FinEngine" + mEngineId + "已释放");
                    sFinEngineCount--;
                    isPrepared = false;
                    return true;
                case MSG_SWITCH_FILTER:
                    switchFilterInternal();
                    return true;
                case MSG_SWITCH_MODE_MONALISA:
                    switchToMonaLisaInternal();
                    return true;
                case MSG_SWITCH_MODE_NORMAL:
                    switchToNormalModeInternal();
                    return true;
                case MSG_SWITCH_MODE_SWAP_FACE:
                    switchToFaceSwapModeInternal();
                    return true;
                case MSG_PROCESS:
                    if (isPrepared && mData != null) {
                        nativeRender(mEngine, mData, mFrameWidth, mFrameHeight, mDegree, isFrontCamera, mOutWidth, mOutHeight, mFacePtr);
                    }
                    return true;
            }
            return false;
        }

        private void switchToMonaLisaInternal() {
            if (isPrepared) {
                synchronized (FinEngineThread.class) {
                    nativeSwitchToModeMonaLisa(mEngine, mMonaLisaFilePath,mAppCtx,mTrackDataFilePath);
                }
            }
        }
        private void switchToFaceSwapModeInternal() {
            if (isPrepared) {
                synchronized (FinEngineThread.class) {
                    nativeSwitchToModeFaceSwap(mEngine);
                }
            }
        }
        private void switchToNormalModeInternal() {
            if (isPrepared) {
                synchronized (FinEngineThread.class) {
                    nativeSwitchToModeNormal(mEngine);
                }
            }
        }

        private void switchFilterInternal() {
            if (isPrepared) {
                Log.w(TAG, "FinEngine" + mEngineId + "开始切换滤镜");
                synchronized (FinEngineThread.class) {
                    FinFiltersManager.Shader shader = FinFiltersManager.findShader(mFilterType);
                    nativeSwitchFilter(mEngine, mAssetManager, mFilterType, shader.vertex, shader.fragment);
                }
            }
        }

        private void init() {
            mEngine = nativeInit(mOutputSurface);
            isPrepared = mEngine != 0;
            if (isPrepared) {
                Log.w(TAG, "FinEngine" + mEngineId + "初始化成功");
            } else {
                Log.e(TAG, "FinEngine" + mEngineId + "初始化失败!");
                sendReleaseMsg();
            }
        }

        private void sendReleaseMsg() {
            isPrepared = false;
            mSelfHandler.removeCallbacksAndMessages(null);
            mSelfHandler.sendEmptyMessage(MSG_RELEASE);
        }

        public void release() {
            Log.w(TAG, "FinEngine" + mEngineId + "开始释放");
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
    private native long nativeInit(Surface output);

    private native void nativeRelease(long engine);

    private native void nativeSwitchFilter(long engine, AssetManager mAssetManager, int mFilterType, String mVertexName, String mFragmentName);

    private native void nativeSwitchToModeMonaLisa(long engine, String filePath,Context ctx,String trackDataPath);
    private native void nativeSwitchToModeNormal(long engine);
    private native void nativeSwitchToModeFaceSwap(long engine);

    private native void nativeRender(long engine, byte[] data, int frameWidth, int frameHeight, int degree, boolean mirror, int mOutWidth, int mOutHeight, long mFacePtr);
}
