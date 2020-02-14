package org.anyrtc.armeet;

import android.app.Application;
import android.text.TextUtils;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import org.ar.common.utils.SharePrefUtil;
import org.ar.meet_kit.ARMeetEngine;


/**
 * Created by liuxiaozhong on 2019/3/12.
 */
public class ARApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharePrefUtil.init(this);
        boolean isDevMode = SharePrefUtil.getBoolean("isDevMode");
        if (!isDevMode) {
            ARMeetEngine.Inst().initEngine(getApplicationContext(), DeveloperInfo.APPID,DeveloperInfo.APPTOKEN);
            ARMeetEngine.Inst().configServerForPriCloud("192.168.1.21",9080);
        }else {
            String appid = SharePrefUtil.getString("appid");
            String apptoken = SharePrefUtil.getString("apptoken");
            String ip = SharePrefUtil.getString("ip");
            ARMeetEngine.Inst().initEngine(getApplicationContext(), appid, apptoken);
            if (!TextUtils.isEmpty(ip)) {
                ARMeetEngine.Inst().configServerForPriCloud(ip, 9080);
            }
        }

        Bugly.init(getApplicationContext(), "d2e9e6fd44", false);
    }


}
