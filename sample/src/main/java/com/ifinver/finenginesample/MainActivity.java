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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ifinver.finengine.FinCv;
import com.ifinver.finenginesample.singleswitch.OpenCVActivity;
import com.ifinver.finenginesample.unity.UnityActivity;
import com.ifinver.finenginesample.singleswitch.SingleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int REQUEST_CODE_REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivOri = findViewById(R.id.iv_show_ori);

        ivOri.setImageResource(R.drawable.t);
//        ivGrey.setImageBitmap(generateGreyBmp(R.drawable.t));

        findViewById(R.id.fl_show_ori).setOnClickListener(this);
        findViewById(R.id.fl_show_grey).setOnClickListener(this);

        //permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_REQUEST_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView ivGrey = findViewById(R.id.iv_show_grey);
        ivGrey.setImageBitmap(generateGreyBmp(R.drawable.t));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fl_show_ori:
                startActivity(new Intent(this, SingleActivity.class));
                break;
            case R.id.fl_show_grey:
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
                startActivity(new Intent(this, OpenCVActivity.class));
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
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int[] pixels = new int[width * height];
            bmp.getPixels(pixels,0, width,0,0, width, height);
            int[] resultPixels = FinCv.BGRA2Grey(pixels, width, height);
            resultImg = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            resultImg.setPixels(resultPixels,0,width,0,0,width,height);
            return resultImg;
        }catch (Throwable e){
            Log.e("opencv","cannot convert",e);
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_enter, menu);
        return true;
    }

}
