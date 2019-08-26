package org.ar.meet_kit;

import org.ar.common.enums.ARNetQuality;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public abstract class ARMeetEvent  {


    public  void onRTCJoinMeetOK(String roomId){}

    public  void onRTCJoinMeetFailed(String roomId, int code, String reason){}

    public  void onRTCLeaveMeet(int code){}

    public  void onRTCOpenRemoteVideoRender(String peerId, String publishId, String userId, String userData){}

    public  void onRTCCloseRemoteVideoRender(String peerId, String publishId, String userId){}

    public  void onRTCOpenScreenRender(String peerId, String publishId, String userId, String userData){}

    public  void onRTCCloseScreenRender(String peerId, String publishId, String userId){}

    public  void onRTCOpenRemoteAudioTrack(String peerId, String userId, String userData){}

    public  void onRTCCloseRemoteAudioTrack(String peerId, String userId){}

    public  void onRTCLocalAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel){}

    public  void onRTCRemoteAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel){}

    public  void onRTCRemoteAVStatus(String peerId, boolean bAudio, boolean bVideo){}

    public  void onRTCLocalAVStatus(boolean bAudio, boolean bVideo){}

    public  void onRTCRemoteAudioActive(String peerId, String userId,int nLevel, int nTime){}

    public  void onRTCLocalAudioActive( int nLevel, int nTime){}

    public  void onRTCRemoteNetworkStatus(String peerId, String userId,int nNetSpeed, int nPacketLost, ARNetQuality netQuality){}

    public  void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality){}

    public  void onRTCConnectionLost(){}

    public  void onRTCUserMessage(String userId, String userName, String headUrl, String message){}

    public  void onRTCShareEnable(boolean success){}

    public  void onRTCShareOpen(int type, String shareInfo, String userId, String userData){}

    public  void onRTCShareClose(){}

    public  void onRTCHosterOnline(String peerId, String userId, String userData){}

    public  void onRTCHosterOffline(String peerId){}

    public  void onRTCTalkOnlyOn(String peerId, String userId, String userData){}

    public  void onRTCTalkOnlyOff(String peerId){}

    public  void onRtcUserCome(String peerId, String publishId, String userId, String userData){}

    public  void onRtcUserOut(String peerId, String publishId, String userId){}

    public  void onRTCZoomPageInfo(ARMeetZoomMode zoomMode, int allPages, int curPage, int allRender, int screenIndex, int num){}
}
