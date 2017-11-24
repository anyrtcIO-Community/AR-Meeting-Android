package org.anyrtc.model;

/**
 * Created by liuxiaozhong on 2017/9/24.
 */

public class MemberBean {
    public static final int ACTIVITY = 2;
    public String peerId;
    public String name;
    public boolean visiable;
    public MemberBean(String peerId, String name) {
        this.peerId = peerId;
        this.name = name;
    }

    public boolean isVisiable() {
        return visiable;
    }

    public void setVisiable(boolean visiable) {
        this.visiable = visiable;
    }
}