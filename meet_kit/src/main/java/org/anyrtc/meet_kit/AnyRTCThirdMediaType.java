package org.anyrtc.meet_kit;

@Deprecated
public enum AnyRTCThirdMediaType {
    AnyRTC_Third_Media_All(0),
    AnyRTC_Third_Media_Capturer(1),
    AnyRTC_Third_Media_Stream(2);

    public final int type;

    private AnyRTCThirdMediaType(int type) {
        this.type = type;
    }
}
