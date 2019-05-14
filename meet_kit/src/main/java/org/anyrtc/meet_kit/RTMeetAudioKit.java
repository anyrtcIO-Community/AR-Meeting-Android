package org.anyrtc.meet_kit;

/**
 * @author liuxiaozhong
 * @date 2017-10-19
 */
@Deprecated
public class RTMeetAudioKit {

    private static final String TAG = "RTMeetAudioKit";

    private RTMeetKit meetKit;

    public RTMeetAudioKit(AnyRTCAudioMeetEvent mHelper) {
        AnyRTCMeetEngine.Inst().setAuidoModel(true, true);
        meetKit = new RTMeetKit(mHelper, null);
    }

    public boolean setUserToken(final String strUserToken) {
        return meetKit.setUserToken(strUserToken);
    }

    //* Common function
    public void setAudioEnable(final boolean bEnable) {
        meetKit.setAudioEnable(bEnable);

    }

    public void setAudioActiveCheck(final boolean bEnable) {
        AnyRTCMeetEngine.Inst().setAuidoModel(true, bEnable);
    }

    public void setUserShareEnable(int nType, boolean bEnable) {
        meetKit.setUserShareEnable(nType, bEnable);
    }

    public void setUserShareInfo(String strShareInfo) {
        meetKit.setUserShareInfo(strShareInfo);
    }

    public void leave() {
        meetKit.leaveAudio();
    }

    /**
     * RTC function for meet
     */

    /**
     * @param strAnyrtcId
     * @param strUserId
     * @param strUserData
     * @return
     */
    public boolean joinRTC(final String strAnyrtcId, final String strUserId, final String strUserData) {
        boolean result = meetKit.joinRTC("a_" + strAnyrtcId, false, strUserId, strUserData);
        return result;
    }

    public boolean joinRTC(final String strAnyrtcId, boolean bIsHoster, final String strUserId, final String strUserData) {
        boolean result = meetKit.joinRTC("a_" + strAnyrtcId, bIsHoster, strUserId, strUserData);
        return result;
    }

    public boolean sendUserMessage(final String strUserName, final String strUserHeaderUrl, final String strContent) {
        boolean result = meetKit.sendUserMessage(strUserName, strUserHeaderUrl, strContent);
        return result;
    }

    public void setBroadCast(final String strRTCPeerId, final boolean bEnable) {
        meetKit.setBroadCast(strRTCPeerId, bEnable);
    }

    public void setTalkOnly(final String strRTCPeerId, final boolean bEnable) {
        meetKit.setTalkOnly(strRTCPeerId, bEnable);
    }
}
