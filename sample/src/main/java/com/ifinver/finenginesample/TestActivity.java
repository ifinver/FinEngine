package com.ifinver.finenginesample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ifinver.finengine.FaceDetector;

/**
 * Created by iFinVer on 2017/1/18.
 * ilzq@foxmail.com
 */

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        FaceDetector.decodePNGData("/sdcard/nature.png");
    }
}
