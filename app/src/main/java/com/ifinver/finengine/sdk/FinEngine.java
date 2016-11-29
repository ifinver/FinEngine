package com.ifinver.finengine.sdk;

import android.content.res.AssetManager;

import com.ifinver.finengine.MyApp;

/**
 * Created by iFinVer on 2016/11/27.
 * ilzq@foxmail.com
 */

public class FinEngine {
    static {
        System.loadLibrary("fin-engine-lib");
    }

    public static final int FILTER_TYPE_NORMAL = 0;
    public static final int FILTER_TYPE_CYAN = 1;
    public static final int FILTER_TYPE_FISH_EYE = 2;
    public static final int FILTER_TYPE_GREY_SCALE = 3;
    public static final int FILTER_TYPE_NEGATIVE_COLOR = 4;

    private FinEngine(){}
    private static FinEngine mInstance;
    public static FinEngine getInstance(){
        if(mInstance == null){
            synchronized (FinEngine.class){
                if(mInstance == null){
                    mInstance = new FinEngine();
                }
            }
        }
        return mInstance;
    }

    private FinEngineThread mEngineThread;

    public void startEngine(int imageFormat, int frameWidth,int frameHeight,int mFilterType){
        mEngineThread = new FinEngineThread(imageFormat,frameWidth,frameHeight,mFilterType);
        mEngineThread.start();
    }

    public void stopEngine(){
        mEngineThread.quit();
        mEngineThread = null;
    }

    public void notifyProcess(){
        mEngineThread.notifyProcess();
    }

    private class FinEngineThread extends Thread{

        private final int mImageFormat;
        private final int mFilterType;
        private final int mFrameWidth;
        private final int mFrameHeight;
        private long mEngine;
        private boolean exit = false;

        FinEngineThread(int imageFormat, int frameWidth,int frameHeight,int filterType){
            this.mImageFormat = imageFormat;
            this.mFilterType = filterType;
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
        }

        @Override
        public void run() {
            mEngine = _startEngine(mImageFormat,mFrameWidth,mFrameHeight,mFilterType, MyApp.getContext().getAssets());
            while (!exit){
                synchronized (this) {

// TODO: 2016/11/28 process here


                    try {
                        wait();
                    } catch (InterruptedException e) {
                        exit = true;
                    }
                }
            }
            _stopEngine(mEngine);
        }

        public void quit() {
            exit = true;
            interrupt();
        }

        public void notifyProcess() {
            synchronized (this) {
                notify();
            }
        }
    }

    private native long _startEngine(int imageFormat, int frameWidth, int frameHeight, int filterType, AssetManager assets);

    private native void _stopEngine(long engine);

    private native void process(long glContext, byte[] mData, int mFrameDegree, int mFrameWidth, int mFrameHeight);
}
