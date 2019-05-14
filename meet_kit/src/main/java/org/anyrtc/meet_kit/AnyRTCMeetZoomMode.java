package org.anyrtc.meet_kit;

@Deprecated
public enum AnyRTCMeetZoomMode {
    /**
     * 多屏模式
     */
    normal(0),
    /**
     * 语音激励模式
     */
    single(1),
    /**
     * 驾驶模式
     */
    drive(2);

    public final int type;

    private AnyRTCMeetZoomMode(int type) {
        this.type = type;
    }
}
