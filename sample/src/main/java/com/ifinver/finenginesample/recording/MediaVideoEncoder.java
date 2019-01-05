package com.ifinver.finenginesample.recording;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MediaVideoEncoder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compl	iance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaVideoEncoder extends MediaEncoder {
    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "MediaVideoEncoder";

    private static final String MIME_TYPE = "video/avc";
    // parameters for recording
    private static final int FRAME_RATE = 23;
    private static final float BPP = 0.12f;

    private final int mWidth;
    private final int mHeight;
    private Surface mSurface;

    public MediaVideoEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener, final int width, final int height) {
        super(muxer, listener);
        if (DEBUG) Log.i(TAG, "MediaVideoEncoder: ");
        mWidth = width;
        mHeight = height;
    }


    public Surface getSurface() {
        return mSurface;
    }


    @Override
    public boolean frameAvailableSoon() {
        if (DEBUG) Log.i(TAG, "frameAvailableSoon");
        return super.frameAvailableSoon();
    }

    @Override
    protected void prepare() throws IOException, MediaCodec.CodecException {
        if (DEBUG) Log.i(TAG, "prepare: ");
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;

        final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        if (DEBUG) Log.i(TAG, "selected codec: " + videoCodecInfo.getName());
        if (DEBUG) Log.i(TAG, "mWidth =  " + mWidth + " mHeight = " + mHeight);
        final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);    // API >= 18
        format.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, -1);
        if (DEBUG) Log.i(TAG, "format: " + format);

//        mMediaCodec = MediaCodec.createByCodecName(videoCodecInfo.getName());
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // get Surface for encoder input
        // this method only can call between #configure and #start
        mSurface = mMediaCodec.createInputSurface();    // API >= 18
        mMediaCodec.start();
        if (DEBUG) Log.i(TAG, "prepare finishing");
        if (mListener != null) {
            try {
                mListener.onPrepared(this);
            } catch (final Exception e) {
                Log.e(TAG, "prepare:", e);
            }
        }
    }

    @Override
    protected void release() {
        if (DEBUG) Log.i(TAG, "release:");
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        super.release();
    }

    private int calcBitRate() {
        final int bitrate = (int) (BPP * FRAME_RATE * mWidth * mHeight);
        Log.i(TAG, String.format("bitrate=%d", bitrate / 1024));
        return bitrate;
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        List<MediaCodecInfo> supportList = new ArrayList<>();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    boolean support = isSupportSurface(codecInfo, mimeType);
                    if(support){
                        if (DEBUG) Log.i(TAG, "supported codec:" + codecInfo.getName());
                        supportList.add(codecInfo);
                    }
//                    if (support) {
//                        return codecInfo;
//                    }
                }
            }
        }
        if(supportList.size() > 0){
            for(int i = 0;i < supportList.size();i++){
                MediaCodecInfo mediaCodecInfo = supportList.get(i);
                if(!mediaCodecInfo.getName().contains("google")){
                    return mediaCodecInfo;
                }
            }
            return supportList.get(0);
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     *
     * @return 0 if no colorFormat is matched
     */
    protected static final boolean isSupportSurface(final MediaCodecInfo codecInfo, final String mimeType) {
        if (DEBUG) Log.i(TAG, "selectColorFormat: ");
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface == colorFormat){
                return true;
            }
        }
        Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return false;
    }

    /**
     * color formats that we can use in this class
     */
    protected static int[] recognizedFormats;

    static {
        recognizedFormats = new int[]{
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
        if (DEBUG) Log.i(TAG, "isRecognizedViewoFormat:colorFormat=" + colorFormat);
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void signalEndOfInputStream() {
        if (DEBUG) Log.d(TAG, "sending EOS to encoder");
        if (mIsEOS) {
            return;
        }
        try {
            mMediaCodec.signalEndOfInputStream();    // API >= 18
            mIsEOS = true;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
