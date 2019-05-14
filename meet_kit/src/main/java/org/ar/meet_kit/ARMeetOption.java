package org.ar.meet_kit;

import org.ar.common.enums.ARVideoCommon;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */

public class ARMeetOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean isDefaultFrontCamera = true;
    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private ARVideoCommon.ARVideoOrientation mScreenOriention = ARVideoCommon.ARVideoOrientation.Portrait;
    /**
     * anyRTC视频清晰标准；默认：标清（AnyRTC_Video_SD）
     */
    private ARVideoCommon.ARVideoProfile videoProfile = ARVideoCommon.ARVideoProfile.ARVideoProfile360x640;
    /**
     * anyRTC视频帧率；默认：15帧（ARVideoFrameRateFps15）
     */
    private ARVideoCommon.ARVideoFrameRate videoFps = ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps15;

    /**
     * 会议媒体类型
     */
    private ARVideoCommon.ARMediaType mediaType = ARVideoCommon.ARMediaType.Video;

    /**
     * 会议类型
     */
    private ARMeetType meetType=ARMeetType.Normal;

    /**
     * 是否是主持人
     */
    private boolean isHost = false;

    public void setOptionParams(boolean isDefaultFrontCamera, ARVideoCommon.ARVideoOrientation mScreenOriention, ARVideoCommon.ARVideoProfile videoProfile, ARVideoCommon.ARVideoFrameRate videoFps, ARVideoCommon.ARMediaType mediaType, ARMeetType meetType, boolean isHost) {
        this.isDefaultFrontCamera = isDefaultFrontCamera;
        this.mScreenOriention = mScreenOriention;
        this.videoProfile = videoProfile;
        this.videoFps = videoFps;
        this.mediaType = mediaType;
        this.meetType = meetType;
        this.isHost = isHost;
    }

    public ARMeetOption() {
    }

    protected boolean isDefaultFrontCamera() {
        return isDefaultFrontCamera;
    }

    public void setDefaultFrontCamera(boolean defaultFrontCamera) {
        isDefaultFrontCamera = defaultFrontCamera;
    }

    protected ARVideoCommon.ARVideoOrientation getScreenOriention() {
        return mScreenOriention;
    }

    public void setScreenOriention(ARVideoCommon.ARVideoOrientation mScreenOriention) {
        this.mScreenOriention = mScreenOriention;
    }

    protected ARVideoCommon.ARVideoProfile getVideoProfile() {
        return videoProfile;
    }

    public void setVideoProfile(ARVideoCommon.ARVideoProfile videoProfile) {
        this.videoProfile = videoProfile;
    }

    protected ARVideoCommon.ARVideoFrameRate getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(ARVideoCommon.ARVideoFrameRate videoFps) {
        this.videoFps = videoFps;
    }

    protected ARVideoCommon.ARMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(ARVideoCommon.ARMediaType mediaType) {
        this.mediaType = mediaType;
    }

    protected ARMeetType getMeetType() {
        return meetType;
    }

    public void setMeetType(ARMeetType meetType) {
        this.meetType = meetType;
    }

    protected boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}
