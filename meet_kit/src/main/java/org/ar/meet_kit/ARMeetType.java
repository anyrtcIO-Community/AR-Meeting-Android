package org.ar.meet_kit;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */

public enum ARMeetType {
    Normal(0),
    Host(1),
    ZOOM(3);

    public final int type;

    private ARMeetType(int type) {
        this.type = type;
    }
}
