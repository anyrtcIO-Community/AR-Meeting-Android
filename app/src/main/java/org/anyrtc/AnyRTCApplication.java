package org.anyrtc;

import android.app.Application;

import org.anyrtc.meet_kit.AnyRTCMeetEngine;
import org.anyrtc.utils.NameUtils;

/**
 * Created by liuxiaozhong on 2017-10-12.
 */

public class AnyRTCApplication extends Application {

    private static AnyRTCApplication mAnyRTCApplication;
    private static String NickName="";


    @Override
    public void onCreate() {
        super.onCreate();
        mAnyRTCApplication=this;
        NickName= NameUtils.getNickName();
        //初始化会议引擎
        //配置开发者信息 可去anyrtc.io官网注册获取
        AnyRTCMeetEngine.Inst().initEngineWithAnyrtcInfo(getApplicationContext(),"DeveloperID", "APPID", "APPKEY", "APPTOKEN");
        //配置私有云  没有可不填写
//        AnyRTCMeetEngine.Inst().configServerForPriCloud("",0);
    }

    public  static Application App(){
        return mAnyRTCApplication;
    }

    public static String getNickName(){
        return NickName;
    }

    public static String getUserId(){
        return (int)((Math.random()*9+1)*100000)+"";
    }

}
