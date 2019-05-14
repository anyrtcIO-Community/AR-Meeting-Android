package org.ar.meet_kit;

import org.ar.common.enums.ARNetQuality;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public abstract class ARMeetEvent  {


    public abstract void onRTCJoinMeetOK(String anyrtcId);

    public abstract void onRTCJoinMeetFailed(String anyrtcId, int code, String reason);

    public abstract void onRTCLeaveMeet(int code);

    public abstract void onRTCOpenRemoteVideoRender(String peerId, String publishId, String userId, String userData);

    public abstract void onRTCCloseRemoteVideoRender(String peerId, String publishId, String userId);

    public abstract void onRTCOpenScreenRender(String peerId, String publishId, String userId, String userData);

    public abstract void onRTCCloseScreenRender(String peerId, String publishId, String userId);

    public abstract void onRTCOpenRemoteAudioTrack(String peerId, String userId, String userData);

    public abstract void onRTCCloseRemoteAudioTrack(String peerId, String userId);

    public abstract void onRTCRemoteAVStatus(String peerId, boolean bAudio, boolean bVideo);

    public abstract void onRTCLocalAVStatus(boolean bAudio, boolean bVideo);

    public abstract void onRTCRemoteAudioActive(String peerId, String userId,int nLevel, int nTime);

    public abstract void onRTCLocalAudioActive( int nLevel, int nTime);

    public abstract void onRTCRemoteNetworkStatus(String peerId, String userId,int nNetSpeed, int nPacketLost, ARNetQuality netQuality);

    public abstract void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality);

    public abstract void onRTCConnectionLost();

    public abstract void onRTCUserMessage(String userId, String userName, String headUrl, String message);

    public abstract void onRTCShareEnable(boolean success);

    public abstract void onRTCShareOpen(int type, String shareInfo, String userId, String userData);

    public abstract void onRTCShareClose();

    public abstract void onRTCHosterOnline(String peerId, String userId, String userData);

    public abstract void onRTCHosterOffline(String peerId);

    public abstract void onRTCTalkOnlyOn(String peerId, String userId, String userData);

    public abstract void onRTCTalkOnlyOff(String peerId);

    public abstract void onRtcUserCome(String peerId, String publishId, String userId, String userData);

    public abstract void onRtcUserOut(String peerId, String publishId, String userId);

    public abstract void onRTCZoomPageInfo(ARMeetZoomMode zoomMode, int allPages, int curPage, int allRender, int screenIndex, int num);



}
