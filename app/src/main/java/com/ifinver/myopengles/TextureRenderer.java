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

    public TextureRenderer() {
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mRenderThread = new RenderThread(new Surface(surface));
        mRenderThread.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //do nothing.
        Log.d(TAG,"onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG,"onSurfaceTextureDestroyed");
        mRenderThread.quit();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d(TAG,"onSurfaceTextureUpdated");
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int imageFormat) {
        if(mRenderThread != null){
            mRenderThread.notifyWithBuffer(data,frameWidth,frameHeight,imageFormat);
        }
    }

    private static class RenderThread extends Thread{
        private static final String TAG = "RenderThread";
        boolean quit = false;
        Surface mSurface;
        private long glContext;
        private byte[] mData;
        private int mFrameWidth;
        private int mFrameHeight;
        private int mImageFromat;

        RenderThread(Surface surface){
            this.mSurface = surface;
        }

        public void notifyWithBuffer(byte[] data, int frameWidth, int frameHeight, int imageFormat){
            this.mData = data;
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mImageFromat = imageFormat;
            //wakeup
            synchronized (this) {
                notify();
            }
        }

        private void onDrawFrame(){
            if(glContext != 0){
                GLNative.renderOnContext(glContext, mData,mFrameWidth,mFrameHeight,mImageFromat);
            }
        }

        public void quit(){
            Log.d(TAG,"开始退出渲染线程");
            quit = true;
            interrupt();
        }

        private void initGL() {
            glContext = GLNative.createGLContext(mSurface);
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
