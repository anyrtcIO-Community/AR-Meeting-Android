package org.ar.meet_kit;

/**
 * Created by Eric on 2016/11/8.
 */

public interface ARMeetHelper {
    /**
     * Join meet OK
     *
     * @param roomId
     */
    public void OnRtcJoinMeetOK(String roomId);

    /**
     * Join meet Failed
     *
     * @param roomId
     * @param nCode
     * @param strReason
     */
    public void OnRtcJoinMeetFailed(String roomId, int nCode, String strReason);

    /**
     * Leave meet
     *
     * @param nCode
     */
    public void OnRtcLeaveMeet(int nCode);

    /**
     * 当底层支持重连时，网络断开使用这个回调
     */
    public void OnRtcConnectionLost();

    /**
     * OnRTCOpenVideoRender
     *
     * @param strUserId
     */
    public void OnRtcOpenVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData);

    /**
     * OnRTCCloseVideoRender
     *
     * @param strRTCPeerId
     */
    public void OnRtcCloseVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId);

    public void OnRtcOpenAudioTrack(String strRTCPeerId, String strUserId, String strUserData);

    public void OnRtcCloseAudioTrack(String strRTCPeerId, String strUserId);

    public void OnRtcAudioPcmData(String strRTCPeerId, byte[] data, int nLen, int nSampleHz, int nChannel);

    /**
     * OnRtcUserCome
     * @param strRTCPeerId
     * @param strRTCPubId
     * @param strUserId
     * @param strUserData
     */
    public void OnRtcUserCome(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData);

    /**
     * OnRtcUserOut
     * @param strRTCPeerId
     * @param strRTCPubId
     * @param strUserId
     */
    public void OnRtcUserOut(String strRTCPeerId, String strRTCPubId, String strUserId);

    /**
     * OnRTCAVStatus
     *
     * @param strRTCPeerId
     * @param bAudio
     * @param bVideo
     */
    public void OnRtcAVStatus(String strRTCPeerId, boolean bAudio, boolean bVideo);

    public void OnRtcAudioActive(String strRTCPeerId, String strUserId, int nLevel, int showtime);

    public void OnRtcNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost);

    public void OnRtcUserMessage(String strUserId, String strUserName, String strUserHeaderUrl, String strContent);

    public void OnRtcSetUserShareEnableResult(boolean bSuccess);

    public void OnRtcUserShareOpen(int nType, String strShareInfo, String strUserId, String strUserData);

    public void OnRtcUserShareClose();

    public void OnRtcHosterOnline(String strRTCPeerId, String strUserId, String strUserData);

    public void OnRtcHosterOffline(String strRTCPeerId);

    public void OnRtcTalkOnlyOn(String strRTCPeerId, String strUserId, String strUserData);

    public void OnRtcTalkOnlyOff(String strRTCPeerId);

    public void OnRtcZoomPageInfo(int nZoomMode, int nAllPages, int nCurPage, int nAllRender, int nScrnBeginIdx, int nNum);
}
