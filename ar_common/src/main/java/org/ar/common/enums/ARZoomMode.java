package org.ar.common.enums;

public enum ARZoomMode {
    /**
     * 多屏模式
     */
    AR_ZOOM_NORMAL(0),
    /**
     * 语音激励模式
     */
    AR_ZOOM_SINGNAL(1),
    /**
     * 驾驶模式
     */
    AR_ZOOM_DRIVE(2);

    public final int type;

    private ARZoomMode(int type) {
        this.type = type;
    }
}
