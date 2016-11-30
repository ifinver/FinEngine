package com.ifinver.finengine.offscreen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import com.ifinver.finengine.R;
import com.ifinver.finengine.sdk.FinEngine;
import com.ifinver.finengine.sdk.FinRender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iFinVer on 2016/11/28.
 * ilzq@foxmail.com
 */

public class OffScreenActivity extends AppCompatActivity implements FinEngine.OnVideoBufferListener {

    private static final String TAG = "OffScreenActivity";

    private TextView tvFps;
    private long mFrameCount = 0;
    private long mStartTime = 0;
    private double fps = 0;
    private Timer mFpsTimer;
    private DecimalFormat df;
    private Handler mHandler;
    private OffScreenRenderer mRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
        tvFps = (TextView) findViewById(R.id.tv_fps);
        df = new DecimalFormat("#0");
        mHandler = new Handler();
        mFpsTimer = new Timer(true);
        mFpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvFps.setText(getString(R.string.tip_fps,df.format(fps)));
                    }
                });
            }
        },1000,300);
        TextureView tvRender = (TextureView) findViewById(R.id.tex);
        mRenderer = new OffScreenRenderer(FinRender.FORMAT_RGBA);
        tvRender.setSurfaceTextureListener(mRenderer);
    }


    @Override
    protected void onResume() {
        super.onResume();
        FinEngine.getInstance().startEngine(this,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FinEngine.getInstance().stopEngine();
    }

    private int cap = 30;

    @Override
    public void onVideoBuffer(byte[] data, int frameWidth, int frameHeight) {
        Log.d(TAG,"onVideoBuffer,data[1025]="+data[1025]);
        mRenderer.onVideoBuffer(data,frameWidth,frameHeight);

        if(cap-- == 0){
            int[] ArData=new int[data.length];
            int offset1, offset2;
            for (int i = 0; i < 512; i++) {
                offset1 = i * 512;
                offset2 = (512 - i - 1) * 512;
                for (int j = 0; j < 512; j++) {
                    int texturePixel = data[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    ArData[offset2 + j] = pixel;
                }
            }
            Bitmap modelBitmap = Bitmap.createBitmap(ArData,512,512,Bitmap.Config.ARGB_8888);
            saveBitmap(modelBitmap);
        }

        //计算帧率
        mFrameCount++;
        if(mStartTime == 0){
            mStartTime = SystemClock.elapsedRealtime();
        }else{
            long spend = SystemClock.elapsedRealtime() - mStartTime;
            fps = mFrameCount * 1000.0 / spend;
        }
    }

    private void saveBitmap(Bitmap modelBitmap) {
        File file = new File("/sdcard/"
                +File.separator+"test_"+System.currentTimeMillis() + ".jpg");
        OutputStream file_out;
        try {
            file_out = new FileOutputStream(file);
            modelBitmap.compress(Bitmap.CompressFormat.JPEG, 100, file_out);
            file_out.flush();
            file_out.close();
            Log.d(TAG,"保存图片成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
