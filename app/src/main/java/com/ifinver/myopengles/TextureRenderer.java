package com.ifinver.myopengles;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by iFinVer on 2016/11/21.
 * ilzq@foxmail.com
 */

public class TextureRenderer implements TextureView.SurfaceTextureListener {
    private static final String TAG = "TextureRenderer";
    private RenderThread mRenderThread;
    private int mFrameDegree;
    private int mImageFormat;
    private Surface mSurface;

    public TextureRenderer() {
        mFrameDegree = -1;
        mSurface = null;
        mRenderThread = null;
    }

    public void startContext(int frameDegree, int imageFormat) {
        this.mFrameDegree = frameDegree;
        this.mImageFormat = imageFormat;

        startRenderThread();
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight) {
        if(mRenderThread != null){
            mRenderThread.notifyWithBuffer(data,frameWidth,frameHeight);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);

        startRenderThread();
    }

    private void startRenderThread() {
        if(mSurface != null && mFrameDegree != -1) {
            mRenderThread = new RenderThread(mSurface,mFrameDegree,mImageFormat);
            mRenderThread.start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //do nothing.
        Log.d(TAG,"onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG,"onSurfaceTextureDestroyed");
        if(mRenderThread != null) {
            mRenderThread.quit();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //do nothing.
    }

    private static class RenderThread extends Thread{
        private static final String TAG = "RenderThread";
        private final int mframeDegree;
        private final int mImageFormat;
        boolean quit = false;
        Surface mSurface;
        private long glContext;
        private byte[] mData;
        private int mFrameWidth;
        private int mFrameHeight;

        RenderThread(Surface surface, int mFrameDegree, int mImageFormat){
            this.mSurface = surface;
            this.mframeDegree = mFrameDegree;
            this.mImageFormat = mImageFormat;
        }

        public void notifyWithBuffer(byte[] data, int frameWidth, int frameHeight){
//            if(mData == null){
//                mData = new byte[data.length];
//            }
//            System.arraycopy(data,0,mData,0,data.length);
            this.mData = data;
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            //wakeup
            synchronized (this) {
                notify();
            }
        }

        private void onDrawFrame(){
            if(glContext != 0 && mData != null){
                GLNative.renderOnContext(glContext, mData,mFrameWidth,mFrameHeight);
            }
        }

        public void quit(){
            Log.d(TAG,"开始退出渲染线程");
            quit = true;
            interrupt();
        }

        private void initGL() {
            glContext = GLNative.createGLContext(mSurface,mframeDegree,mImageFormat);
            if(glContext == 0){
                Log.e(TAG,"渲染上下文创建失败！");
            }else{
                Log.d(TAG,"渲染初始化成功");
            }
        }

        private void destroyGL() {
            if(glContext != 0){
                GLNative.releaseGLContext(glContext);
            }
            Log.d(TAG,"渲染线程已退出");
        }

        @Override
        public void run() {
            initGL();

            while (!quit){
                synchronized (this){
                    onDrawFrame();

                    try {
                        wait();
                    } catch (InterruptedException e) {
                        quit = true;
                    }
                }
            }

            destroyGL();
        }
    }
}
