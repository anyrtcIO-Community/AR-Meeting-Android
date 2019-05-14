package org.ar.meet_kit;

public enum ARMeetZoomMode {
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

    private ARMeetZoomMode(int type) {
        this.type = type;
    }
}
