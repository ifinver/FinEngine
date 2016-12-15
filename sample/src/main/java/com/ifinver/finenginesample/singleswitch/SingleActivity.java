package com.ifinver.finenginesample.singleswitch;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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

import com.ifinver.facedetect.FaceDetector;
import com.ifinver.finengine.CameraHolder;
import com.ifinver.finengine.FinRecorder;
import com.ifinver.finengine.Renderer;
import com.ifinver.finenginesample.FrameMeter;
import com.ifinver.finenginesample.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

@SuppressWarnings({"FieldCanBeLocal", "deprecation"})
public class SingleActivity extends AppCompatActivity implements FilterAdapter.OnItemClickListener, CameraHolder.CameraListener, View.OnTouchListener, Renderer.RenderListener {

    private static final String TAG = "SingleActivity";

    private TextView tvFps;
    private Timer mFpsTimer;
    private Handler mHandler;
    private Renderer mRenderer;
    private FrameMeter mFrameMeter;
    private TextureView tvLittle;
    private FinRecorder mRecorder;
    private FrameLayout flContainer;
    private TextureView tvRender;
    private RecyclerView rvFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        // init views
        tvFps = (TextView) findViewById(R.id.tv_fps);
        tvLittle = (TextureView) findViewById(R.id.tex_l);
        flContainer = (FrameLayout) findViewById(R.id.tv_container);
        tvRender = (TextureView) findViewById(R.id.tex);
        rvFilter = (RecyclerView) findViewById(R.id.rv_filter);

        mRenderer = new Renderer(this);
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
                    Log.w("FinEngine", "开始录制");
                    flContainer.setVisibility(View.VISIBLE);
                    mRecorder = FinRecorder.prepare(
                            new Surface(tvLittle.getSurfaceTexture()),
                            mRenderer.getInputTex(),
                            mRenderer.getSharedCtx(),
                            mRenderer.getLocker(),
                            new FinRecorder.RecorderListener() {
                                @Override
                                public void onFrameRendered() {

                                }
                            });
                    mRenderer.setRecorder(mRecorder);
                    flContainer.setBackgroundColor(getResources().getColor(R.color.red));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.w("FinEngine", "停止录制");
                    mRecorder.release();
                    mRenderer.setRecorder(null);
                    flContainer.setVisibility(View.INVISIBLE);
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onFilterItemClick(int filter) {
        mRenderer.switchFilter(this,filter);
    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean frontCurrent) {
        long facePtr = FaceDetector.process(data,frameWidth,frameHeight);
        mRenderer.onVideoBuffer(data, frameWidth, frameHeight, degree, frontCurrent);
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
    public void onToggleCameraComplete(boolean success) {
        Log.w(TAG, "onToggleCameraComplete,success= " + success);
    }

    @Override
    public void onCameraStart(boolean success) {
        FaceDetector.init(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onRenderPrepared(int outputWidth, int outputHeight) {

    }

    @Override
    public void onFrameRendered() {
        //计算帧率
        mFrameMeter.meter();
    }
}
