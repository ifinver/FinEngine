package com.ifinver.finengine;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class Renderer implements TextureView.SurfaceTextureListener, FinRender.FinRenderListener {

    private FinRecorder mRecorder;
    private RenderListener mListener;
    private FinRender mRenderEngine;
    private FinEngine mFinEngine;

    public Renderer(RenderListener listener){
        this.mListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mRenderEngine = FinRender.prepare(new Surface(surface), width,height,this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mRenderEngine.onSizeChange(width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if(mFinEngine != null) {
            mFinEngine.release();
        }
        mRenderEngine.release();
        mRenderEngine = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onRenderPrepared(boolean isPrepared, SurfaceTexture inputSurface, int texName, long eglContext, int surfaceWidth, int surfaceHeight) {
        mFinEngine = FinEngine.prepare(new Surface(inputSurface), surfaceWidth, surfaceHeight);
        if(mListener != null){
            mListener.onRenderPrepared(surfaceWidth,surfaceHeight);
        }
    }

    @Override
    public void onInputSurfaceChanged(int surfaceWidth, int surfaceHeight) {
        if(mFinEngine != null) {
            mFinEngine.resizeInput(surfaceWidth,surfaceHeight);
        }
    }

    @Override
    public void onFrameRendered() {
        if(mRecorder != null){
            mRecorder.record();
        }
        if(mListener != null){
            mListener.onFrameRendered();
        }
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera, long facePtr) {
        if(mFinEngine != null) {
            mFinEngine.process(data,frameWidth,frameHeight,degree,isFrontCamera,facePtr);
        }
    }

    public int getInputTex() {
        return mRenderEngine != null ? mRenderEngine.getInputTex():0;
    }

    public long getSharedCtx() {
        return mRenderEngine != null ? mRenderEngine.getSharedCtx():0;
    }

    public Object getLocker() {
        return mRenderEngine != null ? mRenderEngine.getLocker():0;
    }

    public void setRecorder(FinRecorder mRecorder) {
        this.mRecorder = mRecorder;
    }

    public void nextFilter(Context ctx) {
        switchFilter(ctx,FinFiltersManager.nextFilter(mFinEngine));
    }

    public void switchFilter(Context ctx,int filter){
        if(mFinEngine != null){
            mFinEngine.switchFilter(ctx, filter);
        }
    }

    public interface RenderListener{
        void onRenderPrepared(int outputWidth,int outputHeight);
        void onFrameRendered();
    }
}