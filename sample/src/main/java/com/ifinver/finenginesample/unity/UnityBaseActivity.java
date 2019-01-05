package com.ifinver.finenginesample.unity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.ifinver.finengine.FinRender;
import com.ifinver.finenginesample.recording.VideoRecordManager;
import com.ifinver.finenginesample.singleswitch.SingleActivity;
import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iFinVer on 2016/12/13.
 * ilzq@foxmail.com
 */

public class UnityBaseActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnTouchListener {

    private static final int PERMISSION_RECORD_VIDEO_REQUEST_CODE = 1001;
    private UnityPlayer mUnityPlayer;
    private VideoRecordManager mUnityVideoRecorder;
    private FinRender mU3dRenderEngine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBX_8888);
        mUnityPlayer = new FinUnityPlayer(this);
        //init Unity Video Recorder
        mUnityVideoRecorder = new VideoRecordManager();
    }

    protected void initUnitySurface(SurfaceView unitySurface) {
        unitySurface.getHolder().setFormat(PixelFormat.RGBX_8888);
        unitySurface.getHolder().addCallback(this);
        unitySurface.setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mU3dRenderEngine == null) {
            mU3dRenderEngine = FinRender.prepare(holder.getSurface(), width, height, new FinRender.FinRenderListener() {
                @Override
                public void onRenderPrepared(boolean isPrepared, SurfaceTexture inputSurface,
                        int texName, long eglContext, int surfaceWidth, int surfaceHeight) {
                    if (mUnityVideoRecorder != null) {
                        mUnityVideoRecorder.setPreviewSize(surfaceWidth, surfaceHeight);
                    }
                    mUnityPlayer.displayChanged(0, new Surface(inputSurface));
                }

                @Override
                public void onFrameRendered() {
                    if (mUnityVideoRecorder != null) {
                        mUnityVideoRecorder.recordVideo();
                    }
                }

                @Override
                public void onInputSurfaceChanged(int surfaceWidth, int surfaceHeight) {

                }
            });
        } else {
            mU3dRenderEngine.onSizeChange(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mU3dRenderEngine != null) {
            mU3dRenderEngine.release();
            mU3dRenderEngine = null;
        }
    }

    // ===================================================== unity needed below
    @Override
    protected void onDestroy() {
        mUnityPlayer.quit();
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mUnityPlayer.pause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            onBackPressed();
        }
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!grantCameraAndAudioPermission()) {
                    return false;
                }
                Log.w("FinEngine", "开始录制");
                mUnityVideoRecorder.startRecording(mU3dRenderEngine.getInputTex(), mU3dRenderEngine.getSharedCtx(),
                        mU3dRenderEngine.getLocker(), new VideoRecordManager.RecordStoppedListener() {
                            @Override
                            public void onRecordStopped(final String filePath) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UnityBaseActivity.this, "录制完成，录制文件保存在:"
                                                + filePath, Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        });
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.w("FinEngine", "停止录制");
                if (mUnityVideoRecorder != null) {
                    mUnityVideoRecorder.stopRecording();
                }
                break;
        }
        return true;
    }

    private boolean grantCameraAndAudioPermission() {

        List<String> permissionList = new ArrayList<>();
        addPermission(permissionList, Manifest.permission.CAMERA);
        addPermission(permissionList, Manifest.permission.RECORD_AUDIO);
        addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(
                    UnityBaseActivity.this,
                    permissionList.toArray(new String[permissionList.size()]),
                    PERMISSION_RECORD_VIDEO_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    private boolean addPermission(List<String> permissionList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(permission);
            return true;
        }
        return false;
    }
}
