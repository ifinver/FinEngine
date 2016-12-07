package com.ifinver.finenginesample;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.ifinver.finengine.FinEngine;
import com.ifinver.finrender.FinRender;

/**
 * Created by iFinVer on 2016/12/6.
 * ilzq@foxmail.com
 */

public class Renderer implements TextureView.SurfaceTextureListener, FinRender.FinRenderListener {

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        FinRender.getInstance().prepare(new Surface(surface), this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        FinEngine.getInstance().release();
        FinRender.getInstance().release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    public void onCameraStart(int frameWidth, int frameHeight) {
        FinRender.getInstance().setFrameSize(frameWidth, frameHeight);
    }

    @Override
    public void onRenderPrepared(boolean isPrepared, SurfaceTexture inputSurface, int texName, long eglContext) {
        FinEngine.getInstance().prepare(new Surface(inputSurface));
    }

    @Override
    public void onFrameRendered(Object locker) {

    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean isFrontCamera) {
        FinEngine.getInstance().process(data,frameWidth,frameHeight,degree,isFrontCamera);
    }


}
