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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ifinver.finengine.CameraHolder;
import com.ifinver.finengine.FaceDetector;
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
public class SingleActivity extends AppCompatActivity implements FilterAdapter.OnItemClickListener, CameraHolder.CameraListener, View.OnTouchListener, Renderer.RenderListener, ModeAdapter.OnItemClickListener {

    private static final String TAG = "SingleActivity";

    private TextView tvFps;
    private TextView tvBrightness;
    private TextView tvContrast;
    private Timer mFpsTimer;
    private Handler mHandler;
    private Renderer mRenderer;
    private FrameMeter mFrameMeter;
    private TextureView tvLittle;
    private FinRecorder mRecorder;
    private FrameLayout flContainer;
    private TextureView tvRender;
    private RecyclerView rvFilter;
    private RecyclerView rvMode;
    private boolean isToolsShown = true;
    private SeekBar mSbFaceSkin;
    private SeekBar mSbBright;
    private SeekBar mSbBrightSelf;
    private SeekBar mSbContrast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        // init views
        tvFps = (TextView) findViewById(R.id.tv_fps);
//        tvBrightness = (TextView) findViewById(R.id.tv_brightness);
//        tvContrast = (TextView) findViewById(R.id.tv_contrast);
        tvLittle = (TextureView) findViewById(R.id.tex_l);
        flContainer = (FrameLayout) findViewById(R.id.tv_container);
        tvRender = (TextureView) findViewById(R.id.tex);
        rvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        rvMode = (RecyclerView) findViewById(R.id.rv_mode);
//        mSbFaceSkin = (SeekBar) findViewById(R.id.seek_face_skin);
//        mSbBright = (SeekBar) findViewById(R.id.seek_bright);
//        mSbBrightSelf = (SeekBar) findViewById(R.id.seek_self_brightness);
//        mSbContrast = (SeekBar) findViewById(R.id.seek_self_contrast);

        mRenderer = new Renderer(this.getApplicationContext(),this);
        tvRender.setSurfaceTextureListener(mRenderer);

        rvFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFilter.addItemDecoration(new SpaceItemDecoration(10));
        rvFilter.setAdapter(new FilterAdapter(this, this));

        rvMode.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMode.addItemDecoration(new SpaceItemDecoration(10));
        rvMode.setAdapter(new ModeAdapter(this, this));

        //fps
        initFPS();
        //touch event
        tvRender.setOnTouchListener(this);

//        mSbFaceSkin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                FaceDetector.setFaceSkinSoftenLevel(progress);
//                Log.w("Face","FaceSkin="+progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//        mSbBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                FaceDetector.setFaceBrightLevel(progress);
//                Log.w("Face","Bright="+progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//        mSbBrightSelf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                float brightness = (float)(progress - 100 ) / 100;
//                mRenderer.setBrightness(brightness);
//                tvBrightness.setText("亮度:"+progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//        mSbContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                float contrast = (float)progress /100;
//                mRenderer.setContrast(contrast);
//                tvContrast.setText("对比度:"+progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//        findViewById(R.id.btn_toggle).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggle = !toggle;
//                Log.w("face",toggle?"open face beauty":"close face beauty");
//            }
//        });
    }

//    boolean toggle = true;

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
    public void onModeClicked(int position) {
        switch (position){
            default:
            case 0:
                mRenderer.switchModeToNormal();
                break;
            case 1:
                mRenderer.switchModeToSwapFace();
                break;
            case 2:
                mRenderer.switchModeToMonaLisa(this);
                break;
        }
    }

    @Override
    public void onFilterItemClick(int filter) {
        mRenderer.switchFilter(filter);
    }

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight, int degree, boolean frontCurrent) {
//        if(toggle) {
            long facePtr = FaceDetector.process(data, frameWidth, frameHeight);
//        }
        mRenderer.onVideoBuffer(data, frameWidth, frameHeight, degree, frontCurrent,facePtr);
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
            case R.id.toggle_tools:
                isToolsShown = !isToolsShown;
                rvMode.setVisibility(isToolsShown ? View.VISIBLE : View.INVISIBLE);
                rvFilter.setVisibility(isToolsShown ? View.VISIBLE : View.INVISIBLE);
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
