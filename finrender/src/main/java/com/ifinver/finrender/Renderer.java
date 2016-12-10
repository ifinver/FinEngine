package com.ifinver.finrender;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finengine.FinEngine;
import com.ifinver.finrecorder.FinRecorder;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class Renderer implements TextureView.SurfaceTextureListener, FinRender.FinRenderListener {

    private FinRecorder mRecorder;
    private RenderListener mListener;
    private FinRender mRenderEngine;

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
        FinEngine.getInstance().release();
        mRenderEngine.release();
        mRenderEngine = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onRenderPrepared(boolean isPrepared, SurfaceTexture inputSurface, int texName, long eglContext, int surfaceWidth, int surfaceHeight) {
        FinEngine.getInstance().prepare(new Surface(inputSurface),surfaceWidth,surfaceHeight);
        if(mListener != null){
            mListener.onRenderPrepared(surfaceWidth,surfaceHeight);
        }
    }

    @Override
    public void onInputSurfaceChanged(int surfaceWidth, int surfaceHeight) {
        FinEngine.getInstance().resizeInput(surfaceWidth,surfaceHeight);
    }

    @Override
    public void onFrameRendered() {
        if(mRecorder != null){
            mRecorder.record();
        }
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
        FinEngine.getInstance().process(data,frameWidth,frameHeight,degree,isFrontCamera);
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

    public interface RenderListener{
        void onRenderPrepared(int outputWidth,int outputHeight);
    }
}
