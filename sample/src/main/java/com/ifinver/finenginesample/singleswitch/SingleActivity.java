package com.ifinver.finenginesample.singleswitch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ifinver.finengine.CameraHolder;
import com.ifinver.finengine.FaceDetector;
import com.ifinver.finengine.FinRecorder;
import com.ifinver.finengine.Renderer;
import com.ifinver.finenginesample.FrameMeter;
import com.ifinver.finenginesample.R;
import com.ifinver.finenginesample.recording.VideoRecordManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

@SuppressWarnings({"FieldCanBeLocal", "deprecation"})
public class SingleActivity extends AppCompatActivity implements FilterAdapter.OnItemClickListener, CameraHolder.CameraListener, View.OnTouchListener, Renderer.RenderListener {

    private static final String TAG = "SingleActivity";
    private static final int PERMISSION_RECORD_VIDEO_REQUEST_CODE = 10001;

    private TextView tvFps;
    private Timer mFpsTimer;
    private Handler mHandler;
    private Renderer mRenderer;
    private FrameMeter mFrameMeter;
    private FrameLayout flContainer;
    private TextureView tvRender;
    private RecyclerView rvFilter;
    private VideoRecordManager mVideoRecorder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        // init views
        tvFps = (TextView) findViewById(R.id.tv_fps);
//        flContainer = (FrameLayout) findViewById(R.id.tv_container);
        tvRender = (TextureView) findViewById(R.id.tex);
        rvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        //init video and audio recorder
        mVideoRecorder = new VideoRecordManager();

        mRenderer = new Renderer(getApplicationContext(),this);
        tvRender.setSurfaceTextureListener(mRenderer);

        rvFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFilter.addItemDecoration(new SpaceItemDecoration(10));
        rvFilter.setAdapter(new FilterAdapter(this, this));

        //fps
        initFPS();
        //touch event
        tvRender.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == tvRender) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!grantCameraAndAudioPermission()) {
                        return false;
                    }
                    Log.w("FinEngine", "开始录制");
                    mVideoRecorder.startRecording(mRenderer.getInputTex(), mRenderer.getSharedCtx(),
                            mRenderer.getLocker(), new VideoRecordManager.RecordStoppedListener() {
                                @Override
                                public void onRecordStopped(final String filePath) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SingleActivity.this, "录制完成，录制文件保存在:"
                                                    + filePath, Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            });
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.w("FinEngine", "停止录制");
                    mVideoRecorder.stopRecording();
                    break;
            }
            return true;
        }
        return false;
    }

    private boolean grantCameraAndAudioPermission() {

        List<String> permissionList = new ArrayList<>();
        addPermission(permissionList, Manifest.permission.CAMERA);
        addPermission(permissionList, Manifest.permission.RECORD_AUDIO);
        addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(
                    SingleActivity.this,
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

    @Override
    public void onFilterItemClick(int filter) {
        mRenderer.switchFilter(filter);
    }

    @Override
    public void onCameraStart(boolean success, RuntimeException e) {
        FaceDetector.init(this,getFilesDir()+"/track_data.dat");
    }

    //驱动
    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean frontCurrent) {
        long facePtr = FaceDetector.process(data,frameWidth,frameHeight);
        mRenderer.onVideoBuffer(data, frameWidth, frameHeight, degree, frontCurrent,facePtr);
    }

    @Override
    public void onToggleCameraComplete(boolean success, int cameraIndex) {

    }

    @Override
    public void onZoomCamera(int state) {

    }

    @Override
    public void onFlashLightOpenComplete(boolean success, boolean isOpen) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_camera:
                if (CameraHolder.getInstance().toggleCamera()) {
                    Toast.makeText(this, "switching", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "can't switch", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFPS() {
        mHandler = new Handler();
        mFpsTimer = new Timer(true);
        mFrameMeter = new FrameMeter();
        mFpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvFps.setText(getString(R.string.tip_fps, mFrameMeter.getFPSString()));
                    }
                });
            }
        }, 500, 300);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraHolder.getInstance().start(1280, 720, this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraHolder.getInstance().stopCamera();
        FaceDetector.release();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onRenderPrepared(int outputWidth, int outputHeight) {
        if (mVideoRecorder != null) {
            mVideoRecorder.setPreviewSize(outputWidth, outputHeight);
        }

    }

    @Override
    public void onFrameRendered() {
        if (mVideoRecorder != null) {
            mVideoRecorder.recordVideo();
        }

        //计算帧率
        mFrameMeter.meter();
    }
}
