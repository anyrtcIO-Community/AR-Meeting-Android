package org.anyrtc.meet;

import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.anyrtc.AnyRTCApplication;
import org.anyrtc.BaseActivity;
import org.anyrtc.common.enums.AnyRTCNetQuality;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.meet_kit.AnyRTCMeetEngine;
import org.anyrtc.meet_kit.AnyRTCMeetOption;
import org.anyrtc.meet_kit.AnyRTCVideoMeetEvent;
import org.anyrtc.meet_kit.RTMeetKit;
import org.anyrtc.meeting.R;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.weight.ARVideoView;
import org.anyrtc.weight.ScreenVideoView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoRenderer;

import butterknife.BindView;
import butterknife.OnClick;

public class MeetingActivity extends BaseActivity {


    @BindView(R.id.rl_video)
    RelativeLayout rlVideo;
    @BindView(R.id.view_space)
    View viewSpace;
    @BindView(R.id.tv_meet_mode)
    TextView tvMeetMode;
    @BindView(R.id.btn_camare)
    ImageButton btnCamare;
    @BindView(R.id.ib_audio)
    ImageButton ibAudio;
    @BindView(R.id.ib_leave)
    ImageButton ibLeave;
    @BindView(R.id.ib_video)
    ImageButton ibVideo;
    @BindView(R.id.tv_status)
    TextView tvStatus;
    @BindView(R.id.rl_screen_share)
    RelativeLayout rlScreenShare;
    @BindView(R.id.rl_video_parent)
    RelativeLayout rlVideoParent;
    @BindView(R.id.ib_screen)
    ImageButton ib_screen;
    private RTMeetKit mMeetKit;
    private ARVideoView mVideoView;
    boolean isScreenShare = false;
    boolean hadAddScreenVideo=false;
    private String shareScreenInfo = "";

    String roomId="";
    private int   MaxJoiner;
    private AnyRTCAudioManager mRTCAudioManager;
    ScreenVideoView screenVideoView;
    @Override
    public int getLayoutId() {
        return R.layout.activity_meeting;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mRTCAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
            // This method will be called each time the audio state (number
            // and
            // type of devices) has been changed.
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        mRTCAudioManager.init();
        Bundle bundle=getIntent().getExtras();
        roomId = bundle.getString("id");
        MaxJoiner=bundle.getInt("MaxJoiner");
        int mode = bundle.getInt("mode");
        //获取配置类
        AnyRTCMeetOption anyRTCMeetOption= AnyRTCMeetEngine.Inst().getAnyRTCMeetOption();
        //设置默认为前置摄像头
        anyRTCMeetOption.setFrontCamera(true);
        switch (mode) {
            case 360:
                //设置视频质量
                anyRTCMeetOption.setVideoMode(AnyRTCVideoQualityMode.AnyRTCVideoQuality_Low2);
                tvMeetMode.setText("四人会议室 - 360P");
                break;
            case 720:
                anyRTCMeetOption.setVideoMode(AnyRTCVideoQualityMode.AnyRTCVideoQuality_Height1);
                tvMeetMode.setText("四人会议室 - 720P");
                break;
            case 1080:
                anyRTCMeetOption.setVideoMode(AnyRTCVideoQualityMode.AnyRTCVideoQuality_Height2);
                tvMeetMode.setText("四人会议室 - 1080P");
                break;
            case 9://9人的不需要设置  默认
                tvMeetMode.setText("九人会议室");
                break;
        }
        //实例化视频会议对象
        mMeetKit = new RTMeetKit(VideoMeetEvent,anyRTCMeetOption);
        //实例化视频窗口管理对象 （这个开发者可自行修改RTCVideoView类）
        mVideoView = new ARVideoView(rlVideo,  AnyRTCMeetEngine.Inst().Egl(),this,false);
        mVideoView.setVideoViewLayout(false, Gravity.CENTER, LinearLayout.HORIZONTAL);
        //设置可以点击切换
        mVideoView.setVideoSwitchEnable(true);
        //获取视频渲染对象
        VideoRenderer render = mVideoView.openLocalVideoRender();
        //设置本地视频采集
        mMeetKit.setLocalVideoCapturer(render.GetRenderPointer());
        //加入RTC服务
        mMeetKit.joinRTC("v_"+roomId, AnyRTCApplication.getUserId(), getUserInfo());

    }
    public String getUserInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("MaxJoiner",MaxJoiner);
            jsonObject.put("userId", AnyRTCApplication.getUserId());
            jsonObject.put("nickName", "android" + AnyRTCApplication.getUserId());
            jsonObject.put("headUrl", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }


    @OnClick({R.id.btn_camare, R.id.ib_audio, R.id.ib_leave, R.id.ib_video,R.id.ib_screen})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camare:
                mMeetKit.switchCamera();//翻转摄像头
                if (btnCamare.isSelected()) {
                    btnCamare.setSelected(false);
                } else {
                    btnCamare.setSelected(true);
                }
                break;
            case R.id.ib_audio:
                if (ibAudio.isSelected()) {
                    ibAudio.setSelected(false);
                    mMeetKit.setAudioEnable(true);//允许本地音频传输
                } else {
                    ibAudio.setSelected(true);
                    mMeetKit.setAudioEnable(false);//禁止本地音频传输
                }
                break;
            case R.id.ib_leave:
                if (mMeetKit != null) {
                    mMeetKit.leave();//离开会议
                }
                finishAnimActivity();
                break;
            case R.id.ib_video:
                if (ibVideo.isSelected()) {
                    mMeetKit.setVideoEnable(true);//允许本地视频传输
                    ibVideo.setSelected(false);
                } else {
                    ibVideo.setSelected(true);
                    mMeetKit.setVideoEnable(false);//禁止本地视频传输
                }
            case R.id.ib_screen:
                if (!ib_screen.isSelected()){
                    ToastUtil.show("暂无人共享屏幕");
                    return;
                }
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    rlScreenShare.removeAllViews();
                    toggleVideoLayout();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    },400);

                }else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toggleVideoLayout();
                            if (!hadAddScreenVideo){//如果没打开过屏幕共享  则new一个视频渲染对像  添加到rlScreenShare中
                                rlScreenShare.removeAllViews();
                                rlScreenShare.addView(screenVideoView.getVideoRender().mLayout);
                                hadAddScreenVideo=true;
                            }else {
                                rlScreenShare.addView(screenVideoView.getVideoRender().mLayout);
                            }

                        }
                    },1000);

                }

                break;
        }
    }


    private AnyRTCVideoMeetEvent VideoMeetEvent =new  AnyRTCVideoMeetEvent(){

        /**
         * 加入RTC服务成功（入会成功）
         * @param strAnyRTCId 会议ID
         */
        @Override
        public void onRTCJoinMeetOK(final String strAnyRTCId) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("MEETING", "onRTCJoinMeetOK strAnyRTCId=" + strAnyRTCId);
                    if (tvStatus!=null) {
                        tvStatus.setText("加入会议室(" + roomId + ")成功");
                    }
                }
            });
        }

        @Override
        public void onRTCJoinMeetFailed(final String strAnyRTCId, final int nCode, String strReason) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCJoinMeetFailed strAnyRTCId=" + strAnyRTCId + "Code=" + nCode);
                    if (tvStatus!=null) {
                        tvStatus.setText("加入会议室(" + roomId + ")失败，错误：" + nCode);
                    }
                }
            });
        }


        /**
         * 离开会议
         * @param nCode 状态码
         */
        @Override
        public void onRTCLeaveMeet(final int nCode) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCLeaveMeet Code=" + nCode);
                }
            });
        }

        @Override
        public void onRTCConnectionLost() {

        }


        /**
         * 其他人视频即将显示  比如你在会议中有人进来了 则会回调该方法 再次设置其他人视频窗口即可
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param strUserId 用户ID
         * @param strPublishId 媒体通道ID
         * @param strUserData 用户自定义数据
         */
        @Override
        public void onRTCOpenVideoRender(final String strRTCPeerId, final String strPublishId, final String strUserId, final String strUserData) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCOpenVideoRender strPublishId="+strPublishId+"strRTCPeerId=" + strRTCPeerId + "strUserId=" + strUserId + "strUserData=" +strUserData);
                    if (!strPublishId.equals(shareScreenInfo)) {
                        final VideoRenderer render = mVideoView.openRemoteVideoRender(strUserId);
                        if (null != render) {
                            mMeetKit.setRTCVideoRender(strPublishId, render.GetRenderPointer());
                        }
                    }
                }
            });
        }
        /**
         * 其他人视频关闭  比如你在会议中有人离开了 则会回调该方法 将其他人视频窗口移除即可
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param strUserId 用户ID
         */
        @Override
        public void onRTCCloseVideoRender(final String strRTCPeerId, final String strPublishId, final String strUserId) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCCloseVideoRender strRTCPeerId=" + strRTCPeerId + "strUserId=" + strUserId);
                    if (strPublishId.equals(shareScreenInfo)){
                        shareScreenInfo="";
                        return;
                    }
                    if (mMeetKit!=null&&mVideoView!=null) {
                        mVideoView.removeRemoteRender(strUserId);
                        mMeetKit.setRTCVideoRender(strPublishId, 0);
                    }

                }
            });
        }

        @Override
        public void onRTCOpenScreenRender(String strRTCPeerId, String strPublishId, String strUserId, String strUserData) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isScreenShare = true;
                    shareScreenInfo = strPublishId;
                    ib_screen.setSelected(true);
                    screenVideoView=new ScreenVideoView(AnyRTCMeetEngine.Inst().Egl(),MeetingActivity.this);
                    VideoRenderer videoRenderer = screenVideoView.openVideoRender(strPublishId);
                    mMeetKit.setRTCVideoRender(strPublishId,videoRenderer.GetRenderPointer());
                }
            });
        }

        @Override
        public void onRTCCloseScreenRender(String strRTCPeerId, String strPublishId, String strUserId) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isScreenShare = false;
                    ib_screen.setSelected(false);
                    rlScreenShare.removeAllViews();
                    if (MeetingActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                        toggleVideoLayout();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            }
                        },400);

                    }
                    hadAddScreenVideo=false;
                }
            });
        }

        /**
         * 其他人对音视频操作的监听 其他人打开关闭音视频都会走该方法
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param bAudio true 打开了音频 false 关闭了
         * @param bVideo true 打开了视频 false 关闭了
         */
        @Override
        public void onRTCAVStatus(final String strRTCPeerId, final boolean bAudio, final boolean bVideo) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCAVStatus strRTCPeerId=" + strRTCPeerId + "bAudio=" + bAudio + "bVideo=" + bVideo);
                }
            });
        }

        @Override
        public void onRTCAVStatusForMe(String strRTCPeerId, boolean bAudio, boolean bVideo) {

        }

        /**
         * 声音检测
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param nTime 360毫秒
         */
        @Override
        public void onRTCAudioActive(final String strRTCPeerId, String strUserId, int nLevel, final int nTime) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCAudioActive strRTCPeerId=" + strRTCPeerId + "nTime=" + nTime);
                }
            });
        }

        @Override
        public void onRTCNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost, AnyRTCNetQuality netQuality) {

        }

        /**
         * 收到消息
         * @param strCustomID 用户ID
         * @param strCustomName 用户昵称
         * @param strCustomHeader 用户头像
         * @param strMessage 消息内容
         */
        @Override
        public void onRTCUserMessage(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strMessage) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCUserMessage strCustomID=" + strCustomID +"strCustomName:"+strCustomName+"strCustomHeader:"+strCustomHeader+"strMessage:"+strMessage);
                }
            });
        }

        /**
         * 设置用户分享信息结果
         * @param bSuccess
         */

        @Override
        public void onRTCSetUserShareEnableResult(final boolean bSuccess) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCSetWhiteBoardEnableResult bSuccess=" + bSuccess);
                }
            });
        }

        /**
         * 其他用户打开分享
         * @param nType 自定义分享类型
         * @param strWBInfo 自定义分享数据
         * @param strCustomID 自定义用户ID
         * @param strUserData 自定义用户信息
         */
        @Override
        public void onRTCUserShareOpen(final int nType, final String strWBInfo, String strCustomID, String strUserData) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * 其他用户关闭分享
         */
        @Override
        public void onRTCUserShareClose() {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onRTCHosterOnline(String strRTCPeerId, String strUserId, String strUserData) {

        }

        @Override
        public void onRTCHosterOffline(String strRTCPeerId) {

        }

        @Override
        public void onRtcUserCome(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData) {

        }

        @Override
        public void onRtcUserOut(String strRTCPeerId, String strRTCPubId, String strUserId) {

        }

        @Override
        public void onRTCTalkOnlyOn(String strRTCPeerId, String strUserId, String strUserData) {

        }

        @Override
        public void onRTCTalkOnlyOff(String strRTCPeerId) {

        }

        @Override
        public void onRTCZoomPageInfo(int nZoomMode, int nAllPages, int nCurPage, int nAllRender, int nScrnBeginIdx, int nNum) {

        }
    };



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mMeetKit != null) {
                mMeetKit.leave();
            }
            finishAnimActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMeetKit != null) {
            mMeetKit.clear();
        }
        if (mRTCAudioManager != null) {
            mRTCAudioManager.close();
            mRTCAudioManager = null;
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            if (mMeetKit!=null){
                mMeetKit.setScreenToLandscape();
            }
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mMeetKit!=null){
                mMeetKit.setScreenToPortrait();
            }
        }
    }

    public void toggleVideoLayout() {
        if (rlVideoParent.getTranslationX() == 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(rlVideoParent, "translationX", rlVideoParent.getTranslationX(), -rlVideoParent.getWidth());
            animator.setDuration(400);
            animator.start();
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(rlVideoParent, "translationX", rlVideoParent.getTranslationX(), 0);
            animator.setDuration(400);
            animator.start();
        }
    }

}
