package org.ar.meet_kit;

import android.content.pm.PackageManager;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;

import org.ar.common.enums.ARCaptureType;
import org.ar.common.enums.ARNetQuality;
import org.ar.common.enums.ARVideoCommon;
import org.ar.common.utils.ARUtils;
import org.ar.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public class ARMeetKit {
    private static final String TAG = "ARMeetKit";

    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private LooperExecutor mExecutor;
    private EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;
    private VideoCapturerAndroid mVideoCapturerEx;
    private boolean isFrontOpenMirrorEnable = false;
    private boolean isopenAudioCheck = true;
    private ARMeetEvent arMeetEvent;

    public ARMeetKit(final ARMeetEvent arMeetEvent) {
        ARUtils.assertIsTrue(arMeetEvent != null);
        this.arMeetEvent = arMeetEvent;
        mExecutor = ARMeetEngine.Inst().Executor();
        mEglBase = ARMeetEngine.Inst().Egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(rtMeetHelper);
                if (ARMeetEngine.Inst().getARMeetOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                    nativeSetAuidoModel(true, true);
                } else {
                    nativeSetAuidoModel(false, true);
                }
                if (ARMeetEngine.Inst().getARMeetOption().getScreenOriention() == ARVideoCommon.ARVideoOrientation.Portrait) {
                    nativeSetScreenToPortrait();
                } else {
                    nativeSetScreenToLandscape();
                }
                nativeSetVideoProfileMode(ARMeetEngine.Inst().getARMeetOption().getVideoProfile().level);
                nativeSetVideoFpsProfile(ARMeetEngine.Inst().getARMeetOption().getVideoFps().level);
                nativeSetMeetMode(ARMeetEngine.Inst().getARMeetOption().getMeetType().type);
                initCameraEngine();
            }
        });
    }

    public ARMeetKit() {
        ARUtils.assertIsTrue(rtMeetHelper != null);
        mExecutor = ARMeetEngine.Inst().Executor();
        mEglBase = ARMeetEngine.Inst().Egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(rtMeetHelper);
                if (ARMeetEngine.Inst().getARMeetOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                    nativeSetAuidoModel(true, true);
                } else {
                    nativeSetAuidoModel(false, true);
                }
                if (ARMeetEngine.Inst().getARMeetOption().getScreenOriention() == ARVideoCommon.ARVideoOrientation.Portrait) {
                    nativeSetScreenToPortrait();
                } else {
                    nativeSetScreenToLandscape();
                }
                nativeSetVideoProfileMode(ARMeetEngine.Inst().getARMeetOption().getVideoProfile().level);
                nativeSetVideoFpsProfile(ARMeetEngine.Inst().getARMeetOption().getVideoFps().level);
                nativeSetMeetMode(ARMeetEngine.Inst().getARMeetOption().getMeetType().type);
                initCameraEngine();
            }
        });
    }

    /**
     * 初始化相机及OpenGL引擎
     */
    private void initCameraEngine() {
        //初始化相机引擎及OpenGL
        int permission = PermissionChecker.checkSelfPermission(ARMeetEngine.Inst().context(), CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            if (mVideoCapturer == null) {
                mCameraId = 0;
                String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                String frontCameraDeviceName =
                        CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                if (numberOfCameras > 1 && frontCameraDeviceName != null && ARMeetEngine.Inst().getARMeetOption().isDefaultFrontCamera()) {
                    cameraDeviceName = frontCameraDeviceName;
                    mCameraId = 1;
                }
                Log.d(TAG, "cameraId: " + cameraDeviceName);
                mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                if (mVideoCapturer == null) {
                    Log.e("sys", "Failed to open camera");
                }
            } else {
            }
        } else {
        }
    }

    /**
     * 设置会议配置项
     * @param option 会议配置对象
     */
    public void setMeetOption(ARMeetOption option) {
        if(null != option) {
            if (option.getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                nativeSetAuidoModel(true, true);
            } else {
                nativeSetAuidoModel(false, true);
            }
            if (option.getScreenOriention() == ARVideoCommon.ARVideoOrientation.Portrait) {
                nativeSetScreenToPortrait();
            } else {
                nativeSetScreenToLandscape();
            }
            nativeSetVideoProfileMode(option.getVideoProfile().level);
            nativeSetVideoFpsProfile(option.getVideoFps().level);
            nativeSetMeetMode(option.getMeetType().type);
        } else {
            Log.e(TAG, "setMeetOption option is null");
        }
    }

    public void setMeetEvent(ARMeetEvent event) {
        this.arMeetEvent = event;
    }

    /**
     * 是否打开音频实时检测
     *
     * @param open
     */
    public void setAudioActiveCheck(final boolean open) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                isopenAudioCheck = open;
                if (ARMeetEngine.Inst().getARMeetOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                    nativeSetAuidoModel(true, open);
                } else {
                    nativeSetAuidoModel(false, open);
                }
            }
        });
    }

    public boolean isOpenAudioCheck() {
        return isopenAudioCheck;
    }

    /**
     * 设置视频横屏模式
     */
    public void setScreenToLandscape() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToLandscape();
            }
        });
    }

    /**
     * 设置视频竖屏模式
     */
    public void setScreenToPortrait() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToPortrait();
            }
        });
    }

    /**
     * 设置验证token
     *
     * @param userToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    private boolean setUserToken(final String userToken) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if (null == userToken || userToken.equals("")) {
                    ret = false;
                } else {
                    nativeSetUserToken(userToken);
                    ret = true;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }


    /**
     * 是否打开回音消除
     *
     * @param enable
     */
    public void setForceAecEnable(final boolean enable) {
//        mExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
                nativeForceSetAecEnable(enable);
//            }
//        });
    }

    /**
     * 打开或关闭音频数据回调开关
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setAudioNeedPcm(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioNeedPcm(bEnable);
            }
        });
    }

    /**
     * 设置本地视频是否可用
     *
     * @param enable
     */
    public void setLocalAudioEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(enable);
            }
        });
    }

    /**
     * 设置本地音频是否可用
     *
     * @param enable
     */
    public void setLocalVideoEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(enable);
            }
        });
    }

    public boolean getLocalAudioEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                ret = nativeGetAudioEnable();

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    public boolean getLocalVideoEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                ret = nativeGetVideoEnable();

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }


    /**
     * 设置远端音视频是否传输
     *
     * @param peerId      RTC服务生成的标识Id
     * @param audioEnable true传输，false不传输
     * @param videoEnable true传输，false不传输
     */
    @Deprecated
    public void setRemoteAVEnable(final String peerId, final boolean audioEnable, final boolean videoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRemotePeerAVEnable(peerId, audioEnable, videoEnable);
            }
        });
    }

    /**
     * 不接收某路的音频
     *
     * @param publishId
     * @param audioEnable true:接收远端音频， false，不接收远端音频
     */
    public void muteRemoteAudioStream(final String publishId, final boolean audioEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerAudioEnable(publishId, audioEnable);
            }
        });
    }

    /**
     * 不接收某路的视频
     *
     * @param publishId
     * @param videoEnable true:接收远端视频， false，不接收远端视频
     */
    public void muteRemoteVideoStream(final String publishId, final boolean videoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerVideoEnable(publishId, videoEnable);
            }
        });
    }

    /**
     * 设置驾驶模式
     *
     * @param enable true:打开驾驶模式；false:关闭驾驶模式
     */
    public void setDriverMode(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetDriverMode(enable);
            }
        });
    }


    /**
     * 加载本地摄像头
     *
     * @param lRender 底层图像地址
     * @return 打开本地预览返回值：0/1/2：没哟相机权限/打开成功/打开相机失败
     */
    public int setLocalVideoCapturer(final long lRender) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (ARMeetKit.this) {
                    int ret = 0;
                    int permission = PermissionChecker.checkSelfPermission(ARMeetEngine.Inst().context(), CAMERA);
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        if (mVideoCapturer == null) {
                            mCameraId = 0;
                            String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                            String frontCameraDeviceName =
                                    CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                            int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                            if (numberOfCameras > 1 && frontCameraDeviceName != null && ARMeetEngine.Inst().getARMeetOption().isDefaultFrontCamera()) {
                                cameraDeviceName = frontCameraDeviceName;
                                mCameraId = 1;
                            }
                            Log.d(TAG, "Opening camera: " + cameraDeviceName);
                            mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                            if (mVideoCapturer == null) {
                                Log.e("sys", "Failed to open camera");
                                LooperExecutor.exchange(result, 2);
                            }
                            nativeSetVideoCapturer(mVideoCapturer, lRender);
                            LooperExecutor.exchange(result, 1);
                        } else {
                            nativeSetVideoCapturer(mVideoCapturer, lRender);
                            LooperExecutor.exchange(result, 1);
                        }
                    } else {
                        LooperExecutor.exchange(result, 0);
                    }
                }
            }
        });
        return LooperExecutor.exchange(result, 0);
    }


    /**
     * 重启本地摄像机
     *
     * @param lRender 底层图像地址
     * @return 打开本地预览返回值：0/1/2：没哟相机权限/打开成功/打开相机失败
     */
    public int restartLocalVideoCapturer(final long lRender) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (ARMeetKit.this) {
                    int ret = 0;
                    int permission = PermissionChecker.checkSelfPermission(ARMeetEngine.Inst().context(), CAMERA);
                    if (mVideoCapturer != null) {
                        try {
                            mVideoCapturer.stopCapture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nativeSetVideoCapturer(null, 0);
                        mVideoCapturer = null;
                    }

                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        if (mVideoCapturer == null) {
                            mCameraId = 0;
                            String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                            String frontCameraDeviceName =
                                    CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                            int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                            if (numberOfCameras > 1 && frontCameraDeviceName != null && ARMeetEngine.Inst().getARMeetOption().isDefaultFrontCamera()) {
                                cameraDeviceName = frontCameraDeviceName;
                                mCameraId = 1;
                            }
                            Log.d(TAG, "Opening camera: " + cameraDeviceName);
                            mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);

                            if (mVideoCapturer == null) {
                                Log.e("sys", "Failed to open camera");
                                LooperExecutor.exchange(result, 2);
                            }
                            nativeSetVideoCapturer(mVideoCapturer, lRender);
                            LooperExecutor.exchange(result, 1);
                        } else {
                            LooperExecutor.exchange(result, 3);
                        }
                    } else {
                        LooperExecutor.exchange(result, 0);
                    }
                }
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    public void setVideoSize(final int width, final int height, final int bitrate) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoSize(width, height, bitrate);
            }
        });
    }


    /**
     * 设置本地视频编码码率
     *
     * @param bitrate
     */
    public void setLocalVideoBitrate(final int bitrate) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoBitrate(bitrate);
            }
        });
    }

    /**
     * 设置本地视频编码帧率
     *
     * @param fps
     */
    public void setLocalVideoFps(final int fps) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoFps(fps);
            }
        });
    }

    /**
     * 停止相机预览
     */
    public void removeVideoCapture() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (ARMeetKit.this) {
                    if (mVideoCapturer != null) {
                        try {
                            mVideoCapturer.stopCapture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nativeSetVideoCapturer(null, 0);
                        mVideoCapturer = null;
                    }
                }
            }
        });
    }

    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(null);
                }
            }
        });
    }
    /**
     * 设置ARCamera视频回调数据
     *
     * @param capturerObserver
     */
    public void setARCameraCaptureObserver(final VideoCapturer.ARCameraCapturerObserver capturerObserver) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setARCameraObserver(capturerObserver);
                }
            }
        });
    }

    /**
     * 设置是否采用ARCamera，默认使用ARCamera， 如果设置为false，必须调用setByteBufferFrameCaptured才能本地显示
     *
     * @param usedARCamera true：使用ARCamera，false：不使用ARCamera采集的数据
     */
    public void setUsedARCamera(final boolean usedARCamera) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setUsedARCamera(usedARCamera);
                }
            }
        });
    }

    /**
     * 设置本地显示的视频数据
     *
     * @param data      相机采集数据
     * @param width     宽
     * @param height    高
     * @param rotation  旋转角度
     * @param timeStamp 时间戳
     */
    public void setByteBufferFrameCaptured(final byte[] data, final int width, final int height, final int rotation, final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setByteBufferFrameCaptured(data, width, height, rotation, timeStamp);
                }
            }
        });
    }

    /**
     * 打开第三方流媒体
     *
     * @param url 流媒体地址
     */
    public int openThirdNetStream(final String url) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                ret = nativeOpenThirdNetStream(url);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 关闭播放第三方流媒体
     *
     * @param thirdMediaType 0：关闭所有辐流；1：关闭屏幕共享；2：关闭第三方流媒体
     */
    public void closeThirdNetStream(final ARVideoCommon.ARMediaType thirdMediaType) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCloseThirdStream(thirdMediaType.type);
            }
        });
    }

    /**
     * 网络流本地显示。打开网络流成功之后再设置。
     *
     * @param render
     */
    public void setThirdNetStreamRender(final HwRender render) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetThirdNetStreamRender(render);
            }
        });
    }

    /**
     * 使用rtsp/rtmp等流媒体地址作为摄像头
     *
     * @param url 流媒体地址
     */
    public int openRtspCap(final String url){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                ret = nativeOpenRtspCap(url);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 关闭流媒体摄像头
     */
    public void closeRtspCap() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCloseRtspCap();
            }
        });
    }

    /**
     * 使用硬解显示视频（仅在定制终端中使用）
     *
     * @param publishId
     * @param hwRender
     */
    public void setRTCHwVideoRender(final String publishId, final HwRender hwRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCHwVideoRender(publishId, hwRender);
            }
        });
    }

    /**
     * 设置共享信息
     *
     * @param type 共享类型（自定义）
     */
    public void openShare(final int type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserShareEnable(type, true);
            }
        });
    }

    /**
     * 关闭共享信息
     *
     * @param type 共享类型（自定义）
     */
    public void closeShare(final int type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserShareEnable(type, false);
            }
        });
    }

    /**
     * 发送共享信息
     *
     * @param shareInfo
     */
    public void setShareInfo(final String shareInfo) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserShareInfo(shareInfo);
            }
        });
    }

    /**
     * 离开会议室
     */
    public void leave() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeLeave();
            }
        });
    }


    public void clean() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
                nativeLeave();
                nativeDestroy();
            }
        });
    }

    /**
     * 自定义视频数据接入时，本地显示
     * @param lRender 底层图像地址
     */
    public void setLocalVideoRender(final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoRender(lRender);
            }
        });
    }

    /**
     * 按照旋转角度显示渲染本地之定义视频数据
     * @param lRender 底层图像地址
     * @param rotation ARVideoRotation 视频的角度
     */
    public void setLocalVideoRotationRender(final long lRender, final ARVideoCommon.ARVideoRotation rotation) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoRotationRender(lRender, rotation.rotation);
            }
        });
    }

    /**
     * 外部视频采集的yuv数据流,
     *
     * @param bEnable
     * @param captureType  ARCaptureType
     */
    public void setExternalCameraCapturer(final boolean bEnable, final ARCaptureType captureType) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetExternalCameraCapturer(bEnable, captureType.type);
            }
        });
    }
    /**
     * 外部nv12数据, 使用此接口时， 不能使用setLocalVideoCapturer接口
     * @param data
     * @param width
     * @param height
     * @param rotation ARVideoRotation 视频的角度
     * @return -1:分辨率或者自定义视频数据类型不正确，0：自定义视频流对接成功。
     */
    public int setVideoNV12Data(byte[] data, int width, int height, ARVideoCommon.ARVideoRotation rotation) {
        return nativeSetNV12Data(data, width, height, rotation.rotation);
    }
    /**
     * 外部nv21数据, 使用此接口时， 不能使用setLocalVideoCapturer接口
     * @param data
     * @param width
     * @param height
     * @param rotation ARVideoRotation 视频的角度
     * @return -1:分辨率或者自定义视频数据类型不正确，0：自定义视频流对接成功。
     */
    public int setVideoNV21Data(byte[] data, int width, int height, ARVideoCommon.ARVideoRotation rotation) {
        return nativeSetNV21Data(data, width, height, rotation.rotation);
    }

    /**
     * 外部yuv数据, 使用此接口时， 不能使用setLocalVideoCapturer接口
     * @param p_yuv
     * @param width
     * @param height
     * @param rotation ARVideoRotation 视频的角度
     * @return -1:分辨率或者自定义视频数据类型不正确，0：自定义视频流对接成功。
     */
    public int setVideoYUV420PData(byte[] p_yuv, int width, int height, ARVideoCommon.ARVideoRotation rotation) {
        return nativeSetYUV420PData(p_yuv, width, height, rotation.rotation);
    }


    private String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 设置监看模式
     * @param bMonitor true：监看模式，false：普通模式
     */
    public void setMonitorMode(final boolean bMonitor) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetMonitorMode(bMonitor);
            }
        });
    }

    /**
     * 加入会议
     *
     * @param roomId
     * @param userId
     * @param userData
     * @return 订阅结果；false/true:入会失败（没有RECORD_AUDIO权限）/入会成功
     */
    public boolean joinRTCByToken(final String token, final String roomId, final String userId, final String userData) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                int permission = PermissionChecker.checkSelfPermission(ARMeetEngine.Inst().context(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    if (null != token && !token.equals("")) {
                        nativeSetUserToken(token);
                    }
                    if (!TextUtils.isEmpty(roomId)) {
                        nativeSetDeviceInfo(ARMeetEngine.Inst().getDeviceInfo());
                        ret = nativeJoin(roomId, ARMeetEngine.Inst().getARMeetOption().isHost(), userId, userData);
                    } else {
                        ret = false;
                    }
                } else {
                    ret = false;
                }

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }


    public boolean sendMessage(final String userName, final String headUrl, final String content) {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(content)) {
            return false;
        }
        if (headUrl.getBytes().length > 512) {
            return false;
        }
        if (content.getBytes().length > 1024) {
            return false;
        }
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeSendUserMsg(userName, headUrl, content);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 显示渲染远端视频
     * @param publishId 远端视频流id
     * @param lRender 底层图像地址
     */
    public void setRemoteVideoRender(final String publishId, final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetRTCVideoRender(publishId, lRender);
                }
            }
        });
    }

    /**
     * 按照旋转角度显示渲染远端视频
     * @param publishId 远端视频流id
     * @param lRender 底层图像地址
     * @param rotation ARVideoRotation 视频的角度
     */
    public void setRemoteVideoRotationRender(final String publishId, final long lRender, final ARVideoCommon.ARVideoRotation rotation) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetRTCVideoRotationRender(publishId, lRender, rotation.rotation);
                }
            }
        });
    }

    public void setBroadCast(final String peerId, final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    if (null != peerId) {
                        nativeSetBroadCast(enable, peerId);
                    }
                }
            }
        });
    }

    /**
     * 设置单聊
     *
     * @param peerId 单聊用户的rtcPeerId
     * @param enable
     */
    public void setTalkOnly(final String peerId, final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetTalkOnly(enable, peerId);
                }
            }
        });
    }

    /**
     * 设置Zoom模式
     *
     * @param mode 0:normal 1:single 2:driver
     */
    public void setZoomMode(final ARMeetZoomMode mode) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetZoomMode(mode.type);
            }
        });
    }

    /**
     * 设置页数
     *
     * @param nPages
     */
    public void setZoomPage(final int nPages) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetZoomPage(nPages);
                }
            }
        });
    }

    /**
     * 设置当前页数id及显示个数
     *
     * @param nIdx
     * @param showNum
     */
    public void setZoomPageIdx(final int nIdx, final int showNum) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetZoomPageIdx(nIdx, showNum);
                }
            }
        });
    }


    /**
     * 打开或关闭前置摄像头镜面
     *
     * @param enable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                isFrontOpenMirrorEnable = enable;
                nativeSetCameraMirror(enable);
            }
        });
    }

    public boolean getFrontCameraMirror() {
        return isFrontOpenMirrorEnable;
    }

    /**
     * 打开或关闭网络状态监测
     *
     * @param enable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetNetworkStatus(enable);
            }
        });
    }

    public boolean networkStatusEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeNetworkStatusEnabled();
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    private ARMeetHelper rtMeetHelper = new ARMeetHelper() {
        @Override
        public void OnRtcJoinMeetOK(String roomId) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCJoinMeetOK(roomId);
            }
        }

        @Override
        public void OnRtcJoinMeetFailed(String roomId, int nCode, String strReason) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCJoinMeetFailed(roomId, nCode, strReason);
            }
        }

        @Override
        public void OnRtcLeaveMeet(int nCode) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCLeaveMeet(nCode);
            }
        }

        @Override
        public void OnRtcConnectionLost() {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCConnectionLost();
            }
        }

        @Override
        public void OnRtcOpenVideoRender(String peerId, String publishId, String userId, String userData) {
            if (arMeetEvent != null) {
                if (publishId.startsWith("X100")) {
                    arMeetEvent.onRTCOpenScreenRender(peerId, publishId, userId, userData);
                } else {
                    arMeetEvent.onRTCOpenRemoteVideoRender(peerId, publishId, userId, userData);
                }
            }
        }

        @Override
        public void OnRtcCloseVideoRender(String peerId, String publishId, String userId) {
            if (arMeetEvent != null) {
                if (publishId.startsWith("X100")) {
                    arMeetEvent.onRTCCloseScreenRender(peerId, publishId, userId);
                } else {
                    arMeetEvent.onRTCCloseRemoteVideoRender(peerId, publishId, userId);
                }
            }
        }

        @Override
        public void OnRtcOpenAudioTrack(String peerId, String userId, String userData) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCOpenRemoteAudioTrack(peerId, userId, userData);
            }
        }

        @Override
        public void OnRtcCloseAudioTrack(String peerId, String userId) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCCloseRemoteAudioTrack(peerId, userId);
            }
        }

        @Override
        public void OnRtcAudioPcmData(String peerId, byte[] data, int len, int sampleHz, int channel) {
            if (arMeetEvent != null) {
                if(peerId.equals("localAudio")) {
                    arMeetEvent.onRTCLocalAudioPcmData(peerId, data, len, sampleHz, channel);
                } else {
                    arMeetEvent.onRTCRemoteAudioPcmData(peerId, data, len, sampleHz, channel);
                }
            }
        }

        @Override
        public void OnRtcUserCome(String peerId, String publishId, String userId, String userData) {
            if (arMeetEvent != null) {
                arMeetEvent.onRtcUserCome(peerId, publishId, userId, userData);
            }
        }

        @Override
        public void OnRtcUserOut(String peerId, String publishId, String userId) {
            if (arMeetEvent != null) {
                arMeetEvent.onRtcUserOut(peerId, publishId, userId);
            }
        }

        @Override
        public void OnRtcAVStatus(String peerId, boolean bAudio, boolean bVideo) {
            if (arMeetEvent != null) {
                if (peerId.equals("RTCMainParticipanter")) {
                    arMeetEvent.onRTCLocalAVStatus(bAudio, bVideo);
                } else {
                    arMeetEvent.onRTCRemoteAVStatus(peerId, bAudio, bVideo);
                }
            }
        }

        @Override
        public void OnRtcAudioActive(String peerId, String userId, int nLevel, int nShowtime) {
            if (null != arMeetEvent) {
                if (peerId.equals("RtcPublisher")) {
                    arMeetEvent.onRTCLocalAudioActive(nLevel, nShowtime);
                } else {
                    arMeetEvent.onRTCRemoteAudioActive(peerId, userId, nLevel, nShowtime);
                }
            }
        }

        @Override
        public void OnRtcNetworkStatus(String peerId, String userId, int nNetSpeed, int nPacketLost) {
            if (arMeetEvent != null) {
                ARNetQuality netQuality = null;
                if (nPacketLost <= 1) {
                    netQuality = ARNetQuality.ARNetQualityExcellent;
                } else if (nPacketLost > 1 && nPacketLost <= 3) {
                    netQuality = ARNetQuality.ARNetQualityGood;
                } else if (nPacketLost > 3 && nPacketLost <= 5) {
                    netQuality = ARNetQuality.ARNetQualityAccepted;
                } else if (nPacketLost > 5 && nPacketLost <= 10) {
                    netQuality = ARNetQuality.ARNetQualityBad;
                } else {
                    netQuality = ARNetQuality.ARNetQualityVBad;
                }
                if (peerId.equals("RtcPublisher")) {
                    arMeetEvent.onRTCLocalNetworkStatus(nNetSpeed, nPacketLost, netQuality);
                } else {
                    arMeetEvent.onRTCRemoteNetworkStatus(peerId, userId, nNetSpeed, nPacketLost, netQuality);
                }
            }
        }

        @Override
        public void OnRtcUserMessage(String peerId, String userName, String headUrl, String content) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCUserMessage(peerId, userName, headUrl, content);
            }
        }

        @Override
        public void OnRtcSetUserShareEnableResult(boolean bSuccess) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCShareEnable(bSuccess);
            }
        }

        @Override
        public void OnRtcUserShareOpen(int type, String shareInfo, String userId, String userData) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCShareOpen(type, shareInfo, userId, userData);
            }
        }

        @Override
        public void OnRtcUserShareClose() {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCShareClose();
            }
        }

        @Override
        public void OnRtcHosterOnline(String peerId, String userId, String userData) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCHosterOnline(peerId, userId, userData);
            }
        }

        @Override
        public void OnRtcHosterOffline(String peerId) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCHosterOffline(peerId);
            }
        }

        @Override
        public void OnRtcTalkOnlyOn(String peerId, String userId, String userData) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCTalkOnlyOn(peerId, userId, userData);
            }
        }

        @Override
        public void OnRtcTalkOnlyOff(String peerId) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCTalkOnlyOff(peerId);
            }
        }

        @Override
        public void OnRtcZoomPageInfo(int nZoomMode, int nAllPages, int nCurPage, int nAllRender, int nScrnBeginIdx, int nNum) {
            if (arMeetEvent != null) {
                arMeetEvent.onRTCZoomPageInfo(ARMeetZoomMode.values()[nZoomMode], nAllPages, nCurPage, nAllRender, nScrnBeginIdx, nNum);
            }
        }
    };

    /**
     * 获取UVC Camera的采集数据
     *
     * @return
     */
    public long getUVCCallabck() {
        return nativeGetAnyrtcUvcCallabck();
    }


    /**
     * UVC相机数据与RTC对接
     *
     * @param usbCamera usb相机
     * @return
     */
    public int setUvcVideoCapturer(final Object usbCamera) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUvcVideoCapturer(usbCamera, "");
                LooperExecutor.exchange(result, 0);
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

//    public int setUvcExVideoCapturer(final Object usbCamera) {
//        final Exchanger<Integer> result = new Exchanger();
//        this.mExecutor.execute(new Runnable() {
//            public void run() {
//               nativeSetUvcVideoCapturer(usbCamera, "");
//              LooperExecutor.exchange(result, 0);
//            }
//        });
//        return LooperExecutor.exchange(result, 1);
//    }


    /**
     * Jni interface
     */
    private native long nativeCreate(Object obj);

    /**
     * 设置会议模式
     *
     * @param nMode 0/1/3：会议模式/主持会议模式/直播模式
     */
    private native void nativeSetMeetMode(int nMode);

    private native void nativeSetUserToken(String userToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeForceSetAecEnable(boolean enable);

    private native void nativeSetAudioNeedPcm(boolean enable);

    private native void nativeSetAudioEnable(boolean enable);

    private native void nativeSetVideoEnable(boolean enable);

    private native boolean nativeGetAudioEnable();

    private native boolean nativeGetVideoEnable();

//    private native void nativeSetLocalPeerAVEnable(String publishId, boolean bIsVideo, boolean videoEnable);

    private native void nativeSetRemotePeerAVEnable(String peerId, boolean audioEnable, boolean videoEnable);

    private native void nativeSetLocalPeerAudioEnable(String publishId, boolean audioEnable);

    private native void nativeSetLocalPeerVideoEnable(String publishId, boolean videoEnable);

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long nativeRenderer);

    private native void nativeSetLocalVideoRender(long nativeRenderer);

    private native void nativeSetLocalVideoRotationRender(long nativeRenderer, int rotation);

    private native void nativeSetExternalCameraCapturer(boolean enable, int type);

    private native int nativeSetNV21Data(byte[] data, int width, int height, int rotation);

    private native int nativeSetNV12Data(byte[] data, int width, int height, int rotation);

    private native int nativeSetYUV420PData(byte[] data, int width, int height, int rotation);

    private native int nativeSetVideoYUV420PData(byte[] y, int stride_y, byte[]  u, int stride_u, byte[]  v, int stride_v, int width, int height, int rotation);

    private native void nativeSetVideoCapturer(byte[] p_rgb, int width, int height);

    private native void nativeSetVideoSize(int nWidth, int nHeight, int nBitrate);

    private native void nativeSetVideoBitrate(int bitrate);

    private native void nativeSetVideoFps(int fps);

    private native void nativeSetVideoModeExcessive(int nVideoMode);

    private native void nativeSetVideoProfileMode(int nVideoMode);

    private native void nativeSetVideoFpsProfile(int nFpsMode);

    private native void nativeSetVideoExProfileMode(int nVideoMode);

    private native void nativeSetVideoExFpsProfile(int nFpsMode);

    private native void nativeSetDriverMode(boolean enable);

    private native void nativeSetMonitorMode(boolean bMonitor);

    private native boolean nativeJoin(String roomId, boolean isHoster, String userId, String userData);

    private native void nativeLeave();

    private static native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);

    private native void nativeSetRTCVideoRender(String publishId, long nativeRenderer);

    private native void nativeSetRTCVideoRotationRender(String publishId, long nativeRenderer, int rotation);

    private native boolean nativeSendUserMsg(String userName, String headUrl, String content);

    private native boolean nativeSetUserShareEnable(int type, boolean enable);

    private native void nativeSetUserShareInfo(String shareInfo);

    private native void nativeSetBroadCast(boolean enable, String strLivePeerId);

    private native void nativeSetTalkOnly(boolean enable, String strLivePeerId);

    private native void nativeSetZoomMode(int nMode/*0:normal 1:single 2:driver*/);

    private native void nativeSetZoomPage(int nPage);

    private native void nativeSetZoomPageIdx(int nIdx, int nShowNum);

    private native void nativeSetCameraMirror(boolean enable);

    private native void nativeSetNetworkStatus(boolean enable);

    private native boolean nativeNetworkStatusEnabled();

    private native void nativeDestroy();

    private native long nativeGetAnyrtcUvcCallabck();

    private native void nativeSetUvcVideoCapturer(Object capturer, String strImg);

    private native int nativeOpenThirdNetStream(String pStrUrl);

    private native void nativeCloseThirdStream(int nIdx);

    private native void nativeSetThirdNetStreamRender(Object hwRenderer);

    private native void nativeSetRTCHwVideoRender(String strRtcPubId, Object hwRenderer);

    private native int nativeOpenRtspCap(String pStrUrl);

    private native void nativeCloseRtspCap();
}
