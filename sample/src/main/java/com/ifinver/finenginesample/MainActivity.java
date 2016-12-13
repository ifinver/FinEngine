package com.ifinver.finenginesample;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ifinver.finenginesample.unity.UnityActivity;
import com.ifinver.finenginesample.singleswitch.SingleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int REQUEST_CODE_REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivOri = (ImageView) findViewById(R.id.iv_show_ori);
        ImageView ivGrey = (ImageView) findViewById(R.id.iv_show_grey);

        ivOri.setImageResource(R.drawable.t);
        ivGrey.setImageBitmap(generateGreyBmp(R.drawable.t));

        ivOri.setOnClickListener(this);
        ivGrey.setOnClickListener(this);

        //permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_REQUEST_PERMISSION);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_show_ori:
                startActivity(new Intent(this, SingleActivity.class));
                break;
            case R.id.iv_show_grey:
                startActivity(new Intent(this, UnityActivity.class));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_activity_multi:
                startActivity(new Intent(this, UnityActivity.class));
                return true;
            case R.id.menu_activity_switch:
                startActivity(new Intent(this, SingleActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        try {
            Bitmap resultImg;
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
            int w = bmp.getWidth();
            int h = bmp.getHeight();
            int[] pixels = new int[w * h];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);
            int[] resultInt = new int[pixels.length];
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int col = pixels[i * w + j];
                    int alpha = col & 0xFF000000;
                    int red = (col & 0x00FF0000) >> 16;
                    int green = (col & 0x0000FF00) >> 8;
                    int blue = (col & 0x000000FF);
                    int gray = (int) ((float) red * 0.299 + (float) green * 0.587 + (float) blue * 0.114);
                    resultInt[i * w + j] = alpha | (gray << 16) | (gray << 8) | gray;
                }
            }
            bmp.recycle();
            resultImg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
            return resultImg;
        }catch (Throwable ignored){
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_enter, menu);
        return true;
    }

}
