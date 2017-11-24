package org.anyrtc.meet;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.anyrtc.AnyRTCApplication;
import org.anyrtc.BaseActivity;
import org.anyrtc.meet_kit.AnyRTCAudioMeetEvent;
import org.anyrtc.meet_kit.RTMeetAudioKit;
import org.anyrtc.meeting.R;
import org.anyrtc.model.MemberBean;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;


public class AudioMeetingActivity extends BaseActivity {


    @BindView(R.id.rl_video)
    RelativeLayout rlVideo;
    @BindView(R.id.view_space)
    View viewSpace;
    @BindView(R.id.tv_meet_mode)
    TextView tvMeetMode;
    @BindView(R.id.btn_close)
    ImageButton btnClose;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.rv_people)
    RecyclerView rvPeople;
    @BindView(R.id.ib_audio)
    ImageButton ibAudio;
    AudioMemberAdapter memberAdapter;
    @BindView(R.id.iv_audio)
    ImageView ivAudio;
    RTMeetAudioKit rtMeetAudioKit;
    @BindView(R.id.tv_status)
    TextView tvStatus;
    String id;

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_meeting;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tvName.setText(AnyRTCApplication.getNickName());
        mImmersionBar.titleBar(viewSpace).init();
        rvPeople.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        memberAdapter = new AudioMemberAdapter();
        rvPeople.setItemAnimator(null);
        rvPeople.setAdapter(memberAdapter);
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        int mode = bundle.getInt("mode");
        tvMeetMode.setText(mode == 1 ? "语音会议室 01" : "语音会议室 02");
        //实例化音频会议对象
        rtMeetAudioKit = new RTMeetAudioKit(AudioEvent);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nickName", AnyRTCApplication.getNickName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //加入RTC服务（入会）
        rtMeetAudioKit.joinRTC(id, AnyRTCApplication.getUserId(), jsonObject.toString());

    }

    @OnClick({R.id.btn_close, R.id.ib_audio})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                if (rtMeetAudioKit != null) {
                    rtMeetAudioKit.leave();
                }
                finishAnimActivity();
                break;
            case R.id.ib_audio:
                if (ibAudio.isSelected()) {
                    ibAudio.setSelected(false);
                    rtMeetAudioKit.setAudioEnable(true);
                } else {
                    ibAudio.setSelected(true);
                    rtMeetAudioKit.setAudioEnable(false);
                }
                break;
        }
    }

    //开始自己说话的动画
    public void showSelfAnim() {
        ivAudio.setVisibility(View.VISIBLE);
        Animation animation = new AlphaAnimation(1f, 1f);
        animation.setDuration(360);
        ivAudio.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivAudio.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public AnyRTCAudioMeetEvent AudioEvent =new  AnyRTCAudioMeetEvent() {

        /**
         * 加入RTC服务成功（入会成功）
         * @param strAnyRTCId 会议ID
         */
        @Override
        public void onRTCJoinMeetOK(final String strAnyRTCId) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCJoinMeetOK strAnyRTCId=" + strAnyRTCId);
                    if (tvStatus != null) {
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
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCJoinMeetFailed strAnyRTCId=" + strAnyRTCId + "Code=" + nCode);
                    if (tvStatus != null) {
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
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCLeaveMeet Code=" + nCode);
                }
            });
        }
        /**
         * 其他人加入了
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param strUserId 用户ID
         * @param strUserData 用户自定义数据
         */
        @Override
        public void onRTCOpenAudioTrack(final String strRTCPeerId, final String strUserId, final String strUserData) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCOpenAudioTrack strRTCPeerId:" + strRTCPeerId + "strUserId:" + strUserId + "strUserData:" + strUserData);

                    try {
                        JSONObject jsonObject = new JSONObject(strUserData);
                        memberAdapter.addData(new MemberBean(strRTCPeerId, jsonObject.getString("nickName")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        memberAdapter.addData(new MemberBean(strRTCPeerId, "游客"));
                    }

                }
            });
        }

        /**
         * 其他人离开
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param strUserId 用户ID
         */
        @Override
        public void onRTCCloseAudioTrack(final String strRTCPeerId, final String strUserId) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCCloseAudioTrack strRTCPeerId:" + strRTCPeerId + "strUserId:" + strUserId);
                    int index = 100;
                    for (int i = 0; i < memberAdapter.getData().size(); i++) {
                        if (memberAdapter.getItem(i).peerId.equals(strRTCPeerId)) {
                            index = i;
                        }
                    }
                    if (index != 100 && index <= memberAdapter.getData().size()) {
                        memberAdapter.remove(index);
                    }
                }
            });
        }

        /**
         * 其他人对音频操作的监听 其他人打开关闭音频都会走该方法
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param bAudio true 打开了音频 false 关闭了
         */
        @Override
        public void onRTCAVStatus(final String strRTCPeerId, final boolean bAudio) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCAVStatus strRTCPeerId=" + strRTCPeerId + "bAudio=" + bAudio);
                }
            });
        }
        /**
         * 声音检测  谁在说话可以在这判断
         * @param strRTCPeerId RTC服务生成的用来标识该用户的ID
         * @param nTime 360毫秒
         */
        @Override
        public void onRTCAudioActive(final String strRTCPeerId, final int nTime) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCAudioActive strRTCPeerId=" + strRTCPeerId + "nTime=" + nTime);
                    for (int i = 0; i < memberAdapter.getData().size(); i++) {
                        if (strRTCPeerId.equals(memberAdapter.getData().get(i).peerId)) {
                            memberAdapter.getItem(i).setVisiable(true);
                            memberAdapter.notifyItemChanged(i);
                        }
                    }
                }
            });
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
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCUserMessage strCustomID=" + strCustomID + "strCustomName:" + strCustomName + "strCustomHeader:" + strCustomHeader + "strMessage:" + strMessage);

                }
            });
        }
        /**
         * 打开白板结果
         * @param bSuccess true 成功 false 失败
         */
        @Override
        public void onRTCSetWhiteBoardEnableResult(final boolean bSuccess) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCSetWhiteBoardEnableResult bSuccess=" + bSuccess);
                }
            });
        }
        /**
         * 会议中其他人打开了白板
         * @param strWBInfo 白板信息
         * @param strCustomID 用户ID
         * @param strUserData 用户数据
         */
        @Override
        public void onRTCWhiteBoardOpen(final String strWBInfo, final String strCustomID, final String strUserData) {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCWhiteBoardOpen strWBInfo=" + strWBInfo + "strCustomID:" + strCustomID + "strUserData:" + strUserData);


                }
            });
        }
        /**
         * 会议中 其他人关闭了白板
         */
        @Override
        public void onRTCWhiteBoardClose() {
            AudioMeetingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("callback", "onRTCWhiteBoardClose ");
                }
            });
        }
    };



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (rtMeetAudioKit != null) {
                rtMeetAudioKit.leave();
                finishAnimActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
