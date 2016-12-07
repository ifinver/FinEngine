package com.ifinver.finenginesample;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finengine.FinEngine;
import com.ifinver.finrecorder.FinRecorder;
import com.ifinver.finrender.FinRender;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class Renderer implements TextureView.SurfaceTextureListener, FinRender.FinRenderListener {

    private FinRecorder mRecorder;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        FinRender.getInstance().prepare(new Surface(surface), width,height,this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        FinRender.getInstance().onSizeChange(width,height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        FinEngine.getInstance().release();
        FinRender.getInstance().release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onRenderPrepared(boolean isPrepared, SurfaceTexture inputSurface, int texName, long eglContext, int surfaceWidth, int surfaceHeight) {
        FinEngine.getInstance().prepare(new Surface(inputSurface),surfaceWidth,surfaceHeight);
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
        return FinRender.getInstance().getInputTex();
    }

    public long getSharedCtx() {
        return FinRender.getInstance().getSharedCtx();
    }

    public Object getLocker() {
        return FinRender.getInstance().getLocker();
    }

    public void setRecorder(FinRecorder mRecorder) {
        this.mRecorder = mRecorder;
    }
}
