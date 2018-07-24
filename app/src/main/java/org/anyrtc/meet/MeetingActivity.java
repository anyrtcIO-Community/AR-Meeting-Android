package org.anyrtc.meet;

import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.anyrtc.AnyRTCApplication;
import org.anyrtc.BaseActivity;
import org.anyrtc.common.enums.AnyRTCVideoLayout;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.meet_kit.AnyRTCMeetEngine;
import org.anyrtc.meet_kit.AnyRTCMeetOption;
import org.anyrtc.meet_kit.AnyRTCVideoMeetEvent;
import org.anyrtc.meet_kit.RTMeetKit;
import org.anyrtc.meeting.R;
import org.anyrtc.weight.RTCVideoView;
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
    private RTMeetKit mMeetKit;
    private RTCVideoView mVideoView;
    boolean isOpenBoard=false;
    String id="";
    private AnyRTCAudioManager mRTCAudioManager;
    @Override
    public int getLayoutId() {
        return R.layout.activity_meeting;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mImmersionBar.titleBar(viewSpace).init();
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
        id=bundle.getString("id");
        int mode=bundle.getInt("mode");
        //获取配置类
        AnyRTCMeetOption anyRTCMeetOption= AnyRTCMeetEngine.Inst().getAnyRTCMeetOption();
        //设置默认为前置摄像头
        anyRTCMeetOption.setFrontCamera(true);
        switch (mode) {
            case 360:
                //设置视频图像排列方式
                anyRTCMeetOption.setVideoLayout(AnyRTCVideoLayout.AnyRTC_V_1X3);
                //设置视频质量
                anyRTCMeetOption.setVideoMode(AnyRTCVideoQualityMode.AnyRTCVideoQuality_Low2);
                tvMeetMode.setText("四人会议室 - 360P");
                break;
            case 720:
                anyRTCMeetOption.setVideoLayout(AnyRTCVideoLayout.AnyRTC_V_1X3);
                anyRTCMeetOption.setVideoMode(AnyRTCVideoQualityMode.AnyRTCVideoQuality_Height1);
                tvMeetMode.setText("四人会议室 - 720P");
                break;
            case 1080:
                anyRTCMeetOption.setVideoLayout(AnyRTCVideoLayout.AnyRTC_V_1X3);
                anyRTCMeetOption.setVideoMode(AnyRTCVideoQualityMode.AnyRTCVideoQuality_Height2);
                tvMeetMode.setText("四人会议室 - 1080P");
                break;
            case 9://9人的不需要设置  默认
                anyRTCMeetOption.setVideoLayout(AnyRTCVideoLayout.AnyRTC_V_3X3_auto);
                tvMeetMode.setText("九人会议室");
                break;
        }
        //实例化视频会议对象
        mMeetKit = new RTMeetKit(VideoMeetEvent,anyRTCMeetOption);
        //实例化视频窗口管理对象
        mVideoView = new RTCVideoView(rlVideo, this, AnyRTCMeetEngine.Inst().Egl());
        //设置可以点击切换 （这个开发者可自行修改RTCVideoView类）
        mVideoView.setVideoSwitchEnable(true);
        //获取视频渲染对象
        VideoRenderer render = mVideoView.OnRtcOpenLocalRender();
        //设置本地视频采集
        mMeetKit.setLocalVideoCapturer(render.GetRenderPointer());
        //加入RTC服务
        mMeetKit.joinRTC(id, AnyRTCApplication.getUserId(), AnyRTCApplication.getNickName());

//        mVideoView.setBtnClickEvent(new RTCVideoView.ViewClickEvent() {
//            @Override
//            public void CloseVideoRender(View view, String strPeerId) {
//
//            }
//
//            @Override
//            public void OnSwitchCamera(View view) {
//
//            }
//
//            @Override
//            public void onVideoTouch(String strPeerId) {
//
//            }
//        });
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }


    @OnClick({R.id.btn_camare, R.id.ib_audio, R.id.ib_leave, R.id.ib_video})
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
                        tvStatus.setText("加入会议室(" + id + ")成功");
                    }
                }
            });
        }

        /**
         * 加入RTC服务失败 （入会失败）
         * @param strAnyRTCId 会议ID
         * @param nCode 状态码
         */
        @Override
        public void onRTCJoinMeetFailed(final String strAnyRTCId, final int nCode) {
            MeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCJoinMeetFailed strAnyRTCId=" + strAnyRTCId + "Code=" + nCode);
                    if (tvStatus!=null) {
                        tvStatus.setText("加入会议室(" + id + ")失败，错误：" + nCode);
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
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strPublishId);
                    if (null != render) {
                        mMeetKit.setRTCVideoRender(strPublishId, render.GetRenderPointer());
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
                    if (mMeetKit!=null&&mVideoView!=null) {
                        mVideoView.OnRtcRemoveRemoteRender(strPublishId);
                        mMeetKit.setRTCVideoRender(strPublishId, 0);
                    }

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
        public void onRTCNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost) {

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
        public void onRTCUserShareOpen(int nType, String strWBInfo, String strCustomID, String strUserData) {

        }

        /**
         * 其他用户关闭分享
         */
        @Override
        public void onRTCUserShareClose() {

        }

        @Override
        public void onRTCHosterOnline(String strRTCPeerId, String strUserId, String strUserData) {

        }

        @Override
        public void onRTCHosterOffline(String strRTCPeerId) {

        }

        @Override
        public void onRTCTalkOnlyOn(String strRTCPeerId, String strUserId, String strUserData) {

        }

        @Override
        public void onRTCTalkOnlyOff(String strRTCPeerId) {

        }

        @Override
        public void onRTCCheckConnectionRlt(boolean bOK) {

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

}
