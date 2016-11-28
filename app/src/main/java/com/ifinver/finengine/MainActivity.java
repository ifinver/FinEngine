package com.ifinver.finengine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.ifinver.finengine.multiscreen.OpenGLActivity;
import com.ifinver.finengine.singleswitch.CameraActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int REQUEST_CODE_REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivOri = (ImageView) findViewById(R.id.iv_show_ori);
        ImageView ivGrey = (ImageView) findViewById(R.id.iv_show_grey);

        ivOri.setOnClickListener(this);
        ivGrey.setOnClickListener(this);

        ivOri.setImageResource(R.drawable.t);
        ivGrey.setImageBitmap(generateGreyBmp(R.drawable.t));

        //permission
        if (ContextCompat.checkSelfPermission(MyApp.getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_REQUEST_PERMISSION);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_show_ori:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            case R.id.iv_show_grey:
                startActivity(new Intent(this, OpenGLActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            String per = permissions[i];
            if (android.Manifest.permission.CAMERA.equals(per)) {
                if (grantResults[i] != 0) {
                    new AlertDialog.Builder(this).setTitle("需要相机权限").setMessage("请在设置或者手机管家中打开app的相机使用权限").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Bitmap generateGreyBmp(int res) {
        Bitmap resultImg = null;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] pixels = new int[w * h];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        //recall JNI
//        int[] resultInt = GLNative.getGrayImage(pixels, w, h);
        int[] resultInt = null;
        bmp.recycle();
        if (resultInt != null) {
            resultImg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
        }
        return resultImg;
    }

}
