package org.ar.meet_kit;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.webrtc.CameraVideoCapturer;

import java.nio.ByteBuffer;

/**
 * Created by Eric on 2018/10/16.
 */
@SuppressWarnings("deprecation")
public class HwRender extends Thread implements SurfaceHolder.Callback{
    public static final String TAG = "HwRender";

    private long mCPPtr = 0;
    private boolean isRuning = false;
    private static final String H264_MIME = "video/avc";
    /**
     * 1920,640x480
     */
    private int mWidth = 1920;
    private int mHeight = 1080;
    private MediaCodec mDecoder = null;
    ByteBuffer myinputBuffers;
    byte[] buf;
    MediaCodec.BufferInfo mInfo;
    ByteBuffer[] mInputBuffers;
    ByteBuffer[] mOutputBuffers;
    SurfaceView mSurfaceView = null;
    SurfaceHolder mH264SurfaceHolder = null;
    private CameraVideoCapturer.CameraEventsHandler mCameraEventsHandler;

    public HwRender() {
        myinputBuffers = ByteBuffer.allocateDirect(1024 * 1024);
        buf = new byte[1024 * 1024];
    }

    public void SetSurface(SurfaceView sfv)
    {
        mSurfaceView = sfv;
        if( mSurfaceView != null) {
            mSurfaceView.getHolder().addCallback(this);
        }

        if(!isRuning) {
            isRuning = true;
            this.start();
        }
    }

    public void setmCameraEventsHandler(CameraVideoCapturer.CameraEventsHandler mCameraEventsHandler) {
        this.mCameraEventsHandler = mCameraEventsHandler;
    }

    public void close() {
        Log.d(TAG, "close");
        isRuning = false;
        closeCodec();
    }

    public void closeCodec() {
        Log.d(TAG, "closeCodec");
        synchronized (HwRender.this) {
            if (mDecoder != null) {
                mDecoder.stop();
                mDecoder.release();
                mDecoder = null;
            }
            mH264SurfaceHolder = null;
        }
    }

    public void openCodec()
    {
        synchronized (HwRender.this){
            if (mDecoder != null) {
                return;
            }
            MediaFormat format = MediaFormat.createVideoFormat(H264_MIME, mWidth, mHeight);
            try {
                mDecoder = MediaCodec.createDecoderByType(H264_MIME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mDecoder.configure(format, mH264SurfaceHolder.getSurface(), null, 0);
                // mDecoder.configure(format, null, null, 0);
                mDecoder.start();

                mInfo = new MediaCodec.BufferInfo();
                mInputBuffers = mDecoder.getInputBuffers();
                mOutputBuffers = mDecoder.getOutputBuffers();
                Log.d(TAG, "starting decoder success inputbuffers size:" + mInputBuffers.length + " outputbuffers size:"
                        + mOutputBuffers.length);
            } catch (Exception e) {
                Log.e(TAG, "configure or start exception");
                e.printStackTrace();
            }
        }
    }

    private static native int nativeGetCodecBuffer(long idd, ByteBuffer buffer);

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
            int frameSize = 0;

            while (isRuning) {
                frameSize = 0;
//                Log.v(TAG, "GetVideoPacket  mCPPtr:   " + mCPPtr);
                if(mCPPtr != 0)
                {
                    myinputBuffers.clear();
                    frameSize = nativeGetCodecBuffer(mCPPtr, myinputBuffers);
                    if(frameSize > 0) {
                        int inputIndex = 0;
                        synchronized (HwRender.this) {
                            if(mDecoder != null) {
                                myinputBuffers.get(buf, 0, frameSize);
//                                Log.v(TAG, "n:" + frameSize);

                                try {
                                    inputIndex = mDecoder.dequeueInputBuffer(0);
                                    if (inputIndex >= 0) {
                                        ByteBuffer inputBuffer = mInputBuffers[inputIndex];
                                        inputBuffer.clear();
                                        inputBuffer.put(buf, 0, frameSize);
                                        mDecoder.queueInputBuffer(inputIndex, 0, frameSize, System.currentTimeMillis(), 0);
                                    } else {
                                        Log.v(TAG, "GetVideoPacket  inputIndex:   " + mCPPtr);
                                    }
                                    int outIndex = 0;
                                    while (outIndex >= 0) {
                                        outIndex = mDecoder.dequeueOutputBuffer(mInfo, 0);
                                        if (outIndex >= 0) {
                                            ByteBuffer decoderoutbuffer = mOutputBuffers[outIndex];
                                            mDecoder.releaseOutputBuffer(outIndex, /*true*/true);
                                        } else if (outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                            mOutputBuffers = mDecoder.getOutputBuffers();
                                        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                Thread.sleep(1);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            close();
        }
    };

    private void setInfo(SurfaceHolder holder) {
        mH264SurfaceHolder = holder;
        mH264SurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        openCodec();
    }

    public void setCPPtr(long idd) {
        mCPPtr = idd;
    }

    public boolean doDecode(byte[] inputBuf, int frameSize) {
        try {
            int inputIndex = mDecoder.dequeueInputBuffer(0);
            if (inputIndex >= 0) {
                ByteBuffer inputBuffer = mInputBuffers[inputIndex];
                inputBuffer.clear();
                inputBuffer.put(inputBuf, 0, frameSize);
                mDecoder.queueInputBuffer(inputIndex, 0, frameSize, System.currentTimeMillis(), 0);
            }
            int outIndex = 0;
            while (outIndex >= 0) {
                outIndex = mDecoder.dequeueOutputBuffer(mInfo, 0);
                if (outIndex >= 0) {
                    ByteBuffer decoderoutbuffer = mOutputBuffers[outIndex];
                    mDecoder.releaseOutputBuffer(outIndex, true);
                } else if (outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    mOutputBuffers = mDecoder.getOutputBuffers();
                } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                }
            }

            if(inputIndex >= 0) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        Log.d(TAG, String.format("HWRender-----------surfaceCreated-----------"));
        //setInfo(surfaceHolder);
        mH264SurfaceHolder = surfaceHolder;
        mH264SurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        openCodec();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mH264SurfaceHolder = surfaceHolder;
//        Log.d(TAG, String.format("HWRender-----------surfaceChanged-----------"));
        if(null != mCameraEventsHandler) {
            mCameraEventsHandler.onFirstFrameAvailable();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        Log.d(TAG, String.format("HWRender-----------surfaceDestroyed-----------"));
        closeCodec();
    }
}


