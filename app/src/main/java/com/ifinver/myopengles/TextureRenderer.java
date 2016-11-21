package com.ifinver.myopengles;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by iFinVer on 2016/11/21.
 * ilzq@foxmail.com
 */

public class TextureRenderer implements TextureView.SurfaceTextureListener {
    private static final String TAG = "TextureRenderer";
    private final CameraHolder mCameraHolder;
    private long nativeGlContext = 0;

    public TextureRenderer(CameraHolder mCameraHolder) {
        this.mCameraHolder = mCameraHolder;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG,"onSurfaceTextureAvailable");
        nativeGlContext = GLNative.createGLContext(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //do nothing.
        Log.d(TAG,"onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG,"onSurfaceTextureDestroyed");
        GLNative.releaseGLContext(nativeGlContext);
        nativeGlContext = 0;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.d(TAG,"onSurfaceTextureUpdated");
    }

    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int imageFormat) {
        if(nativeGlContext != 0){
            GLNative.renderOnContext(nativeGlContext,data,frameWidth,frameHeight,imageFormat);
        }
    }
}
