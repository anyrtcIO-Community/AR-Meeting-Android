package org.anyrtc.meet_kit;

import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoLayout;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;

/**
 * @author Skyline
 * @date 2017/11/13
 */
@Deprecated
public class AnyRTCMeetOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean mBFront = true;
    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private AnyRTCScreenOrientation mScreenOriention = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;
    /**
     * anyRTC视频清晰标准；默认：标清（AnyRTC_Video_SD）
     */
    private AnyRTCVideoQualityMode mVideoMode = AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium1;
    /**
     * anyRTC视频通讯模板：默认：1x3模板
     */
    private AnyRTCVideoLayout mVideoLayout = AnyRTCVideoLayout.AnyRTC_V_1X3;

    public AnyRTCMeetOption(boolean mBFront, AnyRTCScreenOrientation mScreenOriention, AnyRTCVideoQualityMode mVideoMode, AnyRTCVideoLayout mVideoLayout) {
        this.mBFront = mBFront;
        this.mScreenOriention = mScreenOriention;
        this.mVideoMode = mVideoMode;
        this.mVideoLayout = mVideoLayout;
    }

    public AnyRTCMeetOption() {
    }

    protected boolean ismBFront() {
        return mBFront;
    }

    public void setFrontCamera(boolean mBFront) {
        this.mBFront = mBFront;
    }

    protected AnyRTCScreenOrientation getmScreenOriention() {
        return mScreenOriention;
    }

    public void setVideoOriention(AnyRTCScreenOrientation mScreenOriention) {
        this.mScreenOriention = mScreenOriention;
    }

    protected AnyRTCVideoQualityMode getmVideoMode() {
        return mVideoMode;
    }

    public void setVideoMode(AnyRTCVideoQualityMode mVideoMode) {
        this.mVideoMode = mVideoMode;
    }

    public AnyRTCVideoLayout getmVideoLayout() {
        return mVideoLayout;
    }

    public void setVideoLayout(AnyRTCVideoLayout mVideoLayout) {
        this.mVideoLayout = mVideoLayout;
    }

}
