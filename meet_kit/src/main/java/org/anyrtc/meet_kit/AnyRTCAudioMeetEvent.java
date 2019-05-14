package org.anyrtc.meet_kit;

import org.anyrtc.common.enums.AnyRTCNetQuality;

/**
 * @author liuxiaozhong
 * @date 2017-10-19
 */
@Deprecated
public abstract class AnyRTCAudioMeetEvent implements RTMeetHelper {

    @Override
    public void OnRtcJoinMeetOK(String strAnyrtcId) {
        onRTCJoinMeetOK(strAnyrtcId);
    }

    @Override
    public void OnRtcJoinMeetFailed(String strAnyrtcId, int nCode, String strReason) {
        onRTCJoinMeetFailed(strAnyrtcId, nCode, strReason);
    }

    @Override
    public void OnRtcLeaveMeet(int nCode) {
        onRTCLeaveMeet(nCode);
    }

    @Override
    public void OnRtcConnectionLost() {
        onRTCConnectionLost();
    }

    @Override
    public void OnRtcOpenVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData) {
    }

    @Override
    public void OnRtcCloseVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId) {

    }

    @Override
    public void OnRtcOpenAudioTrack(String strRTCPeerId, String strUserId, String strUserData) {
        onRTCOpenAudioTrack(strRTCPeerId, strUserId, strUserData);
    }

    @Override
    public void OnRtcCloseAudioTrack(String strRTCPeerId, String strUserId) {
        onRTCCloseAudioTrack(strRTCPeerId, strUserId);
    }

    @Override
    public void OnRtcUserCome(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData) {

    }

    @Override
    public void OnRtcUserOut(String strRTCPeerId, String strRTCPubId, String strUserId) {

    }

    @Override
    public void OnRtcAVStatus(String strRTCPeerId, boolean bAudio, boolean bVideo) {
        onRTCAVStatus(strRTCPeerId, bAudio);
    }

    @Override
    public void OnRtcAudioActive(String strRTCPeerId, String strUserId, int nLevel, int nShowtime) {
        onRTCAudioActive(strRTCPeerId, strUserId, nLevel, 360);
    }

    @Override
    public void OnRtcNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost) {
        AnyRTCNetQuality netQuality = null;
        if(nPacketLost <= 1) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityExcellent;
        } else if(nPacketLost > 1 && nPacketLost <= 3) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityGood;
        } else if(nPacketLost > 3 && nPacketLost <= 5) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityAccepted;
        } else if(nPacketLost > 5 && nPacketLost <= 10) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityBad;
        } else {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityVBad;
        }
        onRTCNetworkStatus(strRTCPeerId, strUserId, nNetSpeed, nPacketLost, netQuality);
    }

    @Override
    public void OnRtcUserMessage(String strUserId, String strUserName, String strUserHeaderUrl, String strContent) {
        onRTCUserMessage(strUserId, strUserName, strUserHeaderUrl, strContent);
    }


    @Override
    public void OnRtcSetUserShareEnableResult(boolean bSuccess) {
        onRTCSetUserShareEnableResult(bSuccess);
    }

    @Override
    public void OnRtcUserShareOpen(int nType, String strShareInfo, String strUserId, String strUserData) {
        onRTCUserShareOpen(nType, strShareInfo, strUserId, strUserData);
    }

    @Override
    public void OnRtcUserShareClose() {
        onRTCUserShareClose();
    }

    @Override
    public void OnRtcHosterOnline(String strRTCPeerId, String strUserId, String strUserData) {
        onRTCHosterOnline(strRTCPeerId, strUserId, strUserData);
    }

    @Override
    public void OnRtcHosterOffline(String strRTCPeerId) {
        onRTCHosterOffline(strRTCPeerId);
    }

    @Override
    public void OnRtcTalkOnlyOn(String strRTCPeerId, String strUserId, String strUserData) {
        onRTCTalkOnlyOn(strRTCPeerId, strUserId, strUserData);
    }

    @Override
    public void OnRtcTalkOnlyOff(String strRTCPeerId) {
        onRTCTalkOnlyOff(strRTCPeerId);
    }


    public abstract void onRTCJoinMeetOK(String strAnyRTCId);

    public abstract void onRTCJoinMeetFailed(String strAnyRTCId, int nCode, String strReason);

    public abstract void onRTCLeaveMeet(int nCode);

    public abstract void onRTCConnectionLost();

    public abstract void onRTCOpenAudioTrack(String strRTCPeerId, String strUserId, String strUserData);

    public abstract void onRTCCloseAudioTrack(String strRTCPeerId, String strUserId);

    public abstract void onRTCAVStatus(String strRTCPeerId, boolean bAudio);

    public abstract void onRTCAudioActive(String strRTCPeerId, String strUserId, int nLevel, int nTime);

    public abstract void onRTCNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost, AnyRTCNetQuality netQuality);

    public abstract void onRTCUserMessage(String strUserId, String strUserName, String strUserHeaderUrl, String strContent);

    public abstract void onRTCSetUserShareEnableResult(boolean bSuccess);

    public abstract void onRTCUserShareOpen(int nType, String strShareInfo, String strUserId, String strUserData);

    public abstract void onRTCUserShareClose();

    public abstract void onRTCHosterOnline(String strRTCPeerId, String strUserId, String strUserData);

    public abstract void onRTCHosterOffline(String strRTCPeerId);

    public abstract void onRTCTalkOnlyOn(String strRTCPeerId, String strUserId, String strUserData);

    public abstract void onRTCTalkOnlyOff(String strRTCPeerId);
}
