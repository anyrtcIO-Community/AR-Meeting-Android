package org.ar.common.enums;

/**
 *
 */
public enum ARCaptureType {
    /**
     * YUV
     */
    YUV420P(0),
    /**
     * RGB
     */
    RGB565(1),
    /**
     * NV12
     */
    NV12(0),
    /**
     * NV21
     */
    NV21(0);

    public final int type;

    private ARCaptureType(int type) {
        this.type = type;
    }
}
