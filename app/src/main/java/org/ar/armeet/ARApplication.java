package org.ar.armeet;

import android.app.Application;

import org.ar.meet_kit.ARMeetEngine;


/**
 * Created by liuxiaozhong on 2019/3/12.
 */
public class ARApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ARMeetEngine.Inst().initEngine(getApplicationContext(), DeveloperInfo.APPID,DeveloperInfo.APPTOKEN);
    }
}
