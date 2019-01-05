package com.ifinver.finengine;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by iFinVer on 2017/2/7.
 * ilzq@foxmail.com
 */

public class FinUtils {
    //看其是否存在，若不存在则从assets中复制
    public static boolean checkFile(Context ctx,File file){
        try {
            InputStream in = ctx.getAssets().open(file.getName());
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int readCount;
            while ((readCount = in.read(buffer)) != -1) {
                fos.write(buffer, 0, readCount);
            }
            in.close();
            fos.close();
            return true;
        } catch (Exception ignored) {
            Log.e("FinEngine","文件操作失败",ignored);
            return false;
        }
    }
}
