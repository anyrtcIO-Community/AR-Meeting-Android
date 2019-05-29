package org.anyrtc.meet_kit;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;

import org.anyrtc.common.enums.AnyRTCMeetMode;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoLayout;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
import org.anyrtc.common.utils.AnyRTCUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

/**
 * @author Eric
 * @date 2016/11/8
 */
@Deprecated
public class RTMeetKit {
    private static final String TAG = "RTMeetKit";

    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private Activity mActivity;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;

    private boolean bFront = true;
    private AnyRTCVideoQualityMode anyRTCVideoMode = AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium1;
    private AnyRTCScreenOrientation anyRTCScreenOrientation = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;
    private AnyRTCVideoLayout mVideoLayout = AnyRTCVideoLayout.AnyRTC_V_1X3;


    public RTMeetKit(final RTMeetHelper helper, AnyRTCMeetOption option) {
        AnyRTCUtils.assertIsTrue(helper != null);
        AnyRTCMeetEngine.Inst().setAnyRTCMeetOption(option);
        if (null != option) {
            bFront = option.ismBFront();
            anyRTCVideoMode = option.getmVideoMode();
            anyRTCScreenOrientation = option.getmScreenOriention();
            mVideoLayout = option.getmVideoLayout();
        }
        mExecutor = AnyRTCMeetEngine.Inst().Executor();
        mEglBase = AnyRTCMeetEngine.Inst().Egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(helper);
            }
        });
    }

    /**
     * 是否打开音频实时检测
     * @param bOpen
     */
    public void setAudioActiveCheck(final boolean bOpen) {
        AnyRTCMeetEngine.Inst().setAuidoModel(false, bOpen);
    }

    /**
     * 设置验证token
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if(null == strUserToken || strUserToken.equals("")) {
                    ret = false;
                } else {
                    nativeSetUserToken(strUserToken);
                    ret = true;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 设置会议模式
     * @param mode
     */
    public void setMeetMode(final AnyRTCMeetMode mode) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetMeetMode(mode.type);
            }
        });
    }

    /**
     * 是否打开回音消除
     * @param bEnable
     */
    public void setForceAecEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeForceSetAecEnable(bEnable);
            }
        });
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
     * 设置本地视频是否可用
     * @param bEnable
     */
    public void setAudioEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(bEnable);
            }
        });
    }

    /**
     * 设置本地音频是否可用
     * @param bEnable
     */
    public void setVideoEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(bEnable);
            }
        });
    }

    public boolean getAudioEnabled() {
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

    public boolean getVideoEnabled() {
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
     * 设置远端用户的音视频状态
     * @param strPeerId
     * @param bAudioEnable
     * @param bVideoEnable
     */
    @Deprecated
    public void setPeerAVEnable(final String strPeerId, final boolean bAudioEnable, final boolean bVideoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRemotePeerAVEnable(strPeerId, bAudioEnable, bVideoEnable);
            }
        });
    }

    /**
     * 设置远端用户的音视频状态
     * @param strPeerId
     * @param bAudioEnable
     * @param bVideoEnable
     */
    public void setRemotePeerAVEnable(final String strPeerId, final boolean bAudioEnable, final boolean bVideoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRemotePeerAVEnable(strPeerId, bAudioEnable, bVideoEnable);
            }
        });
    }

    /**
     * 不接收某路的音视频
     * @param strPubId
     * @param bIsVideo 是否是视频，如果是视频，则关闭音频和视频，如果是音视频仅仅关闭音频
     * @param bVideoEnable
     */
    private void setLocalPeerAVEnable(final String strPubId, final boolean bIsVideo, final boolean bVideoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
//                nativeSetLocalPeerAVEnable(strPubId, bIsVideo, bVideoEnable);
            }
        });
    }

    /**
     * 不接收某路的音频
     * @param strPubId
     * @param bAudioEnable true:接收远端音频， false，不接收远端音频
     */
    public void muteRemoteAudioStream(final String strPubId, final boolean bAudioEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerAudioEnable(strPubId, bAudioEnable);
            }
        });
    }
    /**
     * 不接收某路的视频
     * @param strPubId
     * @param bVideoEnable true:接收远端视频， false，不接收远端视频
     */
    public void muteRemoteVideoStream(final String strPubId, final boolean bVideoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerVideoEnable(strPubId, bVideoEnable);
            }
        });
    }

    /**
     * 设置驾驶模式
     * @param bEnable true:打开驾驶模式；false:关闭驾驶模式
     */
    public void setDriverMode(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetDriverMode(bEnable);
            }
        });
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
                synchronized (RTMeetKit.this) {
                    int ret = 0;
                    int permission = PermissionChecker.checkSelfPermission(AnyRTCMeetEngine.Inst().context(), CAMERA);
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        if (mVideoCapturer == null) {
                            mCameraId = 0;
                            String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                            String frontCameraDeviceName =
                                    CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                            int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                            if (numberOfCameras > 1 && frontCameraDeviceName != null && bFront) {
                                cameraDeviceName = frontCameraDeviceName;
                                mCameraId = 1;
                            }
                            Log.d(TAG, "Opening camera: " + cameraDeviceName);
                            mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                            if (mVideoCapturer == null) {
                                Log.e("sys", "Failed to open camera");
                                LooperExecutor.exchange(result, 2);
                            }
                        } else {
                            LooperExecutor.exchange(result, 0);
                        }
                        AnyRTCMeetEngine.Inst().setAuidoModel(false, false);
                        if (anyRTCScreenOrientation == AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait) {
                            nativeSetScreenToPortrait();
                        } else {
                            nativeSetScreenToLandscape();
                        }
                        nativeSetVideoModeExcessive(anyRTCVideoMode.level);
                        nativeSetVideoCapturer(mVideoCapturer, lRender);
                        ret = 1;
                    } else {
                        ret = 0;
                    }
                    LooperExecutor.exchange(result, ret);
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
                synchronized (RTMeetKit.this) {
                    int ret = 0;
                    int permission = PermissionChecker.checkSelfPermission(AnyRTCMeetEngine.Inst().context(), CAMERA);
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
                            if (numberOfCameras > 1 && frontCameraDeviceName != null && bFront) {
                                cameraDeviceName = frontCameraDeviceName;
                                mCameraId = 1;
                            }
                            Log.d(TAG, "Opening camera: " + cameraDeviceName);
                            mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);

                            if (mVideoCapturer == null) {
                                Log.e("sys", "Failed to open camera");
                                LooperExecutor.exchange(result, 2);
                            }
                        } else {
                            LooperExecutor.exchange(result, 0);
                        }
                        AnyRTCMeetEngine.Inst().setAuidoModel(false, false);
                        if (anyRTCScreenOrientation == AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait) {
                            nativeSetScreenToPortrait();
                        } else {
                            nativeSetScreenToLandscape();
                        }
                        nativeSetVideoModeExcessive(anyRTCVideoMode.level);
                        nativeSetVideoCapturer(mVideoCapturer, lRender);
                        ret = 1;
                    } else {
                        ret = 0;
                    }
                    LooperExecutor.exchange(result, ret);
                }
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 停止相机预览
     */
    public void removeVideoCapture() {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (RTMeetKit.this) {
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
     * 设置共享信息
     *
     * @param nType   共享类型（自定义）
     * @param bEnable 打开开关（true/false: 打开/关闭）
     */
    public void setUserShareEnable(final int nType, final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserShareEnable(nType, bEnable);
            }
        });
    }

    /**
     * 发送共享信息
     *
     * @param strShareInfo
     */
    public void setUserShareInfo(final String strShareInfo) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserShareInfo(strShareInfo);
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

    protected void leaveAudio() {
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

    public void clear() {
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
     * 加入会议
     *
     * @param strAnyrtcId
     * @param strUserId
     * @param strUserData
     * @return 订阅结果；false/true:入会失败（没有RECORD_AUDIO权限）/入会成功
     */
    public boolean joinRTC(final String strAnyrtcId, final String strUserId, final String strUserData) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCMeetEngine.Inst().context(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    if (!TextUtils.isEmpty(strAnyrtcId)) {
                        nativeSetDeviceInfo(AnyRTCMeetEngine.Inst().getDeviceInfo());
                        ret = nativeJoin(strAnyrtcId, false, strUserId, strUserData);
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

    /**
     * 加入会议
     *
     * @param strAnyrtcId
     * @param bIsHoster
     * @param strUserId
     * @param strUserData
     * @return 订阅结果；false/true:入会失败（没有RECORD_AUDIO权限）/入会成功
     */
    public boolean joinRTC(final String strAnyrtcId, final boolean bIsHoster, final String strUserId, final String strUserData) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCMeetEngine.Inst().context(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    if (!TextUtils.isEmpty(strAnyrtcId)) {
                        ret = nativeJoin(strAnyrtcId, bIsHoster, strUserId, strUserData);
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

    public boolean sendUserMessage(final String strUserName, final String strUserHeaderUrl, final String strContent) {
        if (TextUtils.isEmpty(strUserName) || TextUtils.isEmpty(strContent)) {
            return false;
        }
        if (strUserHeaderUrl.getBytes().length > 512) {
            return false;
        }
        if (strContent.getBytes().length > 1024) {
            return false;
        }
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeSendUserMsg(strUserName, strUserHeaderUrl, strContent);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    public void setRTCVideoRender(final String strRTCPeerId, final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetRTCVideoRender(strRTCPeerId, lRender);
                }
            }
        });
    }

    public void setBroadCast(final String strRTCPeerId, final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    if(null != strRTCPeerId) {
                        nativeSetBroadCast(bEnable, strRTCPeerId);
                    }
                }
            }
        });
    }

    /**
     * 设置单聊
     * @param strRTCPeerId 单聊用户的rtcPeerId
     * @param bEnable
     */
    public void setTalkOnly(final String strRTCPeerId, final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetTalkOnly(bEnable, strRTCPeerId);
                }
            }
        });
    }

    /**
     * 设置Zoom模式
     * @param mode 0:normal 1:single 2:driver
     */
    public void setZoomMode(final AnyRTCMeetZoomMode mode) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetZoomMode(mode.type);
            }
        });
    }

    /**
     * 设置页数
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
     * @param nIdx
     * @param nShowNum
     */
    public void setZoomPageIdx(final int nIdx, final int nShowNum) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != mVideoCapturer) {
                    nativeSetZoomPageIdx(nIdx, nShowNum);
                }
            }
        });
    }

    /**
     * 获取UVC Camera的采集数据
     * @return
     */
    public long getAnyrtcUvcCallabck() {
        return nativeGetAnyrtcUvcCallabck();
    }


    /**
     * UVC相机数据与RTC对接
     * @param usbCamera usb相机
     * @return
     */
    public int setUvcVideoCapturer(final Object usbCamera){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer == null) {
                    mCameraId = 0;
                    nativeSetUvcVideoCapturer(usbCamera, "");
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }


    /**
     * UVC相机数据与RTC对接
     * @param usbCamera usb相机
     * @return
     */
    public int setUvcExVideoCapturer(final Object usbCamera){
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

    /**
     * 切换USB相机或者本机相机
     * @param isUsb 是否是usb相机（当为true时，为采用usb相机，当为false时，为采用本机相机）
     * @param usbCamera usb相机
     * @param renderPointer 本机相机的底层图像地址
     * @param isFront 本机相机是否是前置摄像头
     * @param eventsHandler 相机打开事件回调
     * @return 0：摄像头打开成功， 1：摄像头打开失败
     */
    public int selectCamera(final boolean isUsb, final Object usbCamera, final long renderPointer,
                            final boolean isFront, final CameraVideoCapturer.CameraEventsHandler eventsHandler) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(isUsb) {
                    //本地相机置空
                    if(mVideoCapturer != null) {
                        try {
                            mVideoCapturer.stopCapture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nativeSetVideoCapturer(null, 0);
                        mVideoCapturer = null;
                    }
                    if(mVideoCapturer == null) {
                        mCameraId = 0;
                        nativeSetUvcVideoCapturer(usbCamera, "");
                        LooperExecutor.exchange(result, 0);
                    }
                } else {
                    //USB相机置空
                    if(mVideoCapturer != null) {
                        nativeSetUvcVideoCapturer(null, "");
                        mVideoCapturer = null;
                    }

                    //加载本地相机
                    if(mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && isFront) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, eventsHandler);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 1);
                        }
                        nativeSetVideoCapturer(mVideoCapturer, renderPointer);
                        LooperExecutor.exchange(result, 0);
                    }
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    /**
     * 打开第三方流媒体
     * @param strUrl 流媒体地址
     * @return 0/2：未进会成功/打开成功，
     */
    public int openThirdNetStream(final String strUrl){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                ret = nativeOpenThirdNetStream(strUrl);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 关闭播放第三方流媒体
     * @param thirdMediaType 0：关闭所有辐流；1：关闭屏幕共享；2：关闭第三方流媒体
     */
    public void closeThirdNetStream(final AnyRTCThirdMediaType thirdMediaType) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCloseThirdStream(thirdMediaType.type);
            }
        });
    }

    /**
     * 网络流本地显示。打开网络流成功之后再设置。
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
     * @param strUrl
     * @return 0:打开成功
     */
    public int openRtspCap(final String strUrl){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                ret = nativeOpenRtspCap(strUrl);
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
     * @param strRtcPubId
     * @param hwRender
     */
    public void setRTCHwVideoRender(final String strRtcPubId, final HwRender hwRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCHwVideoRender(strRtcPubId, hwRender);
            }
        });
    }

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

    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeForceSetAecEnable(boolean bEnable);

    private native void nativeSetAudioEnable(boolean bEnable);

    private native void nativeSetVideoEnable(boolean bEnable);

    private native boolean nativeGetAudioEnable();

    private native boolean nativeGetVideoEnable();

//    private native void nativeSetLocalPeerAVEnable(String strPubId, boolean bIsVideo, boolean bVideoEnable);

    private native void nativeSetRemotePeerAVEnable(String strPeerId, boolean bAudioEnable, boolean bVideoEnable);

    private native void nativeSetLocalPeerAudioEnable(String strPubId, boolean bAudioEnable);

    private native void nativeSetLocalPeerVideoEnable(String strPubId, boolean bVideoEnable);

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long nativeRenderer);

    private native void nativeSetVideoExCapturer(Boolean enable, int type);

    private native void nativeSetVideoYUV420PData(char y, int stride_y, char u, int stride_u, char v, int stride_v, int width, int height);

    private native void nativeSetVideoCapturer(char p_rgb, int width, int height);

    private native void nativeSetVideoSize(int nWidth, int nHeight);

    private native void nativeSetVideoSize(int nWidth, int nHeight, int nBitrate);

    private native void nativeSetVideoBitrate(int bitrate);

    private native void nativeSetVideoFps(int fps);

    private native void nativeSetVideoModeExcessive(int nVideoMode);

    private native void nativeSetVideoProfileMode(int nVideoMode);

    private native void nativeSetVideoFpsProfile(int nFpsMode);

    private native void nativeSetDriverMode(boolean bEnable);

    private native boolean nativeJoin(String strAnyrtcId, boolean isHoster, String strUserId, String strUserData);

    private native void nativeLeave();

    private native void nativeSetRTCVideoRender(String strLivePeerId, long nativeRenderer);

    private native boolean nativeSendUserMsg(String strUserName, String strUserHeaderUrl, String strContent);

    private native boolean nativeSetUserShareEnable(int nType, boolean bEnable);

    private native void nativeSetUserShareInfo(String strShareInfo);

    private native void nativeDestroy();

    private native void nativeSetBroadCast(boolean bEnable, String strLivePeerId);

    private native void nativeSetTalkOnly(boolean bEnable, String strLivePeerId);

    private native void nativeSetZoomMode(int nMode/*0:normal 1:single 2:driver*/);

    private native void nativeSetZoomPage(int nPage);

    private native void nativeSetZoomPageIdx(int nIdx, int nShowNum);

    private native long nativeGetAnyrtcUvcCallabck();

    private native void nativeSetUvcVideoCapturer(Object capturer, String strImg);

    private native int nativeOpenThirdNetStream(String pStrUrl);

    private native void nativeCloseThirdStream(int nIdx);

    private native void nativeSetThirdNetStreamRender(Object hwRenderer);

    private native void nativeSetRTCHwVideoRender(String strRtcPubId, Object hwRenderer);

    private native int nativeOpenRtspCap(String pStrUrl);

    private native void nativeCloseRtspCap();
}
