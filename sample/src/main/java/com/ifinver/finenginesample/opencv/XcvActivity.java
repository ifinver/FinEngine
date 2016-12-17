package com.ifinver.finenginesample.opencv;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ifinver.finengine.FinCv;
import com.ifinver.finenginesample.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by iFinVer on 2016/12/16.
 * ilzq@foxmail.com
 */

public class XcvActivity extends AppCompatActivity {

    private ImageView ivShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        ivShow = (ImageView) findViewById(R.id.iv_show);
        Button btnSwap = (Button) findViewById(R.id.btn_swap);
        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long start = SystemClock.elapsedRealtime();
                Mat resultMat = new Mat();
                FinCv.swapFace(resultMat.nativeObj);
                long spend = SystemClock.elapsedRealtime();
                Bitmap bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultMat,bitmap);
                ivShow.setImageBitmap(bitmap);
                spend = SystemClock.elapsedRealtime() - spend;
                Log.e("XC OPENCV","显示图片耗时："+spend);
                Log.e("XC OPENCV","总耗时："+(SystemClock.elapsedRealtime() - start));
            }
        });

    }
}
