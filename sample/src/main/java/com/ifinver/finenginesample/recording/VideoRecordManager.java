package com.ifinver.finenginesample.recording;

import android.util.Log;

import com.ifinver.finengine.FinRecorder;

/**
 * Created by iFinVer on 2016/12/13 0013.
 */

public class VideoRecordManager {

    private static String TAG = "VideoRecordManager";

    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder videoEncoder;
    private String mRecordFilePath;
    private FinRecorder mRecorder;
    private int mInputTex;
    private long mSharedCtx;
    private Object mLocker;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mStopCount;
    RecordStoppedListener mListener;

    public interface RecordStoppedListener {
        void onRecordStopped(String filePath);
    }

    public String getRecordFilePath() {
        return mRecordFilePath;
    }

    public void setPreviewSize(int previewWidth, int previewHeight) {
        //测试结果：
        //华为H30-L01手机，支持的录制Surface，最大为352x352 .这个手机内置了两个编码器：k3 和 google.xom,k3是硬编，google是软编,k3的版本比较老旧，支持的缓冲区太小。
//        if(Build.MODEL.equals("HONOR H30-L01")){
//            mPreviewHeight = 352;
//            mPreviewWidth = (int)(previewWidth * ((float)mPreviewHeight / previewHeight));
//
//        }else {
            mPreviewWidth = 640;
            mPreviewHeight = (int) (previewHeight * ((float) mPreviewWidth / previewWidth));
//        }
        //格式化为16的倍数
        mPreviewWidth = formatToMul16(mPreviewWidth);
        mPreviewHeight = formatToMul16(mPreviewHeight);

        //进一步的测试中，发现重启手机会清空释放缓冲区。一切回归正常。
        //结论：系统FrameWork的bug，不能正确回收上层MediaCodec.release()之后的Codec.
        //上层可以尝试在release之后仍然持有对象，再次使用时不创建,直接进行config()
    }

    //格式化为16的倍数
    private int formatToMul16(int number) {
        if (number % 16 != 0) {
            int mul = number / 16;
            int h1 = mul * 16;
            int h2 = h1 + 16;
            if (Math.abs(number - h1) < Math.abs(number - h2)) {
                number = h1;
            } else {
                number = h2;
            }
        }
        return number;
    }

    public boolean startRecording(int inputTex, long sharedCtx, Object locker,
                                  RecordStoppedListener listener) {
        Log.v(TAG, "startRecording:");
        try {
            mMuxer = new MediaMuxerWrapper(".mp4");    // if you record audio only, ".m4a" is also OK.
            mInputTex = inputTex;
            mSharedCtx = sharedCtx;
            mLocker = locker;
            mListener = listener;
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mPreviewWidth, mPreviewHeight);
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            mMuxer.prepare();
            mMuxer.startRecording();
            mStopCount = 0;
            mRecorder = FinRecorder.prepare(videoEncoder.getSurface(),
                    mInputTex, mSharedCtx, mLocker,
                    new FinRecorder.RecorderListener() {
                        @Override
                        public void onFrameRendered() {
                            Log.v(TAG, "onFrameRendered:encoder=" + videoEncoder);
                            if (videoEncoder != null) {
                                videoEncoder.frameAvailableSoon();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "startCapture:", e);
            return false;
        }
        return true;
    }

    public void stopRecording() {
        Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mMuxer != null) {
            mRecordFilePath = mMuxer.getOutputPath();
            mMuxer.stopRecording();
            mMuxer = null;
        }
    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener =
            new MediaEncoder.MediaEncoderListener() {
                @Override
                public void onPrepared(final MediaEncoder encoder) {
                    Log.v(TAG, "onPrepared:encoder=" + encoder);
                    if (encoder instanceof MediaVideoEncoder) {
                        videoEncoder = (MediaVideoEncoder) encoder;
                    }
                }

                @Override
                public void onStopped(final MediaEncoder encoder) {
                    Log.v(TAG, "onStopped:encoder=" + encoder);
                    mStopCount++;

                    if (encoder instanceof MediaVideoEncoder) {
                        videoEncoder = null;
                        mInputTex = -1;
                        mSharedCtx = -1;
                        mLocker = null;
                    }
                    if (mStopCount == 2) {
                        mStopCount = 0;
                        if (mListener != null) {
                            Log.v(TAG, "onRecordStopped=" + mRecordFilePath);
                            mListener.onRecordStopped(mRecordFilePath);
                        }
                        mListener = null;
                    }
                }
            };

    public void recordVideo() {
        if (videoEncoder != null && mRecorder != null && mMuxer != null) {
            mRecorder.record();
        }
    }

}
