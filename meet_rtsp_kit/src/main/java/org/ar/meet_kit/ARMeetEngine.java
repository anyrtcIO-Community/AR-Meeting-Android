package org.ar.meet_kit;

import android.content.Context;
import android.support.v4.BuildConfig;

import org.ar.common.enums.ARLogLevel;
import org.ar.common.utils.DeviceUtils;
import org.ar.common.utils.LooperExecutor;
import org.ar.common.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public class ARMeetEngine {

    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("meet-jni");
        System.loadLibrary("xrtsp");
    }

    private final LooperExecutor executor;
    private final EglBase eglBase;
    private Context context;
    private String developerId, appId, appKey, appToken;
    private ARMeetOption arMeetOption = new ARMeetOption();

    private static class SingletonHolder {
        private static final ARMeetEngine INSTANCE = new ARMeetEngine();
    }

    public static final ARMeetEngine Inst() {
        return ARMeetEngine.SingletonHolder.INSTANCE;
    }

    public Context context() {
        return context;
    }

    public ARMeetOption getARMeetOption() {
        return arMeetOption;
    }


    private ARMeetEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
        executor.requestStart();
    }

    public LooperExecutor Executor() {
        return executor;
    }

    public EglBase Egl() {
        return eglBase;
    }

    public void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    public void disableHWDecode() {
        MediaCodecVideoDecoder.disableVp8HwCodec();
        MediaCodecVideoDecoder.disableVp9HwCodec();
        MediaCodecVideoDecoder.disableH264HwCodec();
    }
    public void initEngineWithARInfo(final Context ctx, final String strDeveloperId, final String strAppId,
                                         final String strAESKey, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                context = ctx;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                nativeInitEngineWithARInfo(strDeveloperId, strAppId, strAESKey, strToken, getPackageName());
            }
        });
    }
    /**
     * 初始化应用信息
     * @param ctx
     * @param strAppId
     * @param strToken
     */
    public void initEngine(final Context ctx, final String strAppId, final String strToken) {
        appId = strAppId;
        appToken = strToken;
        context = ctx;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                nativeInitEngineWithAppInfo(strAppId, strToken, getPackageName());
            }
        });
    }

    public String getPackageName() {
        return context.getPackageName();
    }

    public void configServerForPriCloud(final String strAddr, final int nPort) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeConfigServerForPriCloud(strAddr, nPort);
            }
        });
    }

    private void dispose() {
        executor.requestStop();
    }

    /**
     * 获取sdk版本号
     *
     * @return ARMeet版本号
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取设备信息
     * @return
     */
    protected String getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operatorName", NetworkUtils.getNetworkOperatorName()); //运营商名字
            jsonObject.put("devType", 0);//设备类型： 0/1/2/3/4:android/ios/web/wechat/pc
            jsonObject.put("devName", DeviceUtils.getManufacturer() + "-" +DeviceUtils.getModel());//设备名字：MI9，H8类似这些
            jsonObject.put("networkType", NetworkUtils.getNetworkType().toString().replace("NETWORK_", "")); //网络类型：2G/3G/4G/WIFI
            jsonObject.put("osType", "Android " + DeviceUtils.getSDKVersionName());//系统版本，类似Android 7.0
            jsonObject.put("sdkVer", getSdkVersion());//SDK版本
            jsonObject.put("rtcVer", 60);//服务版本：有了写上，没有了可以不写。
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 是否打开回音消除
     *
     * @param enable
     */
    public void setForceAecEnable(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeForceSetAecEnable(enable);
            }
        });
    }
    /**
     * 设置日志显示级别
     *
     * @param logLevel 日志显示级别
     */
    public void setLogLevel(final ARLogLevel logLevel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLogLevel(logLevel.type);
            }
        });
    }

    /**
     * Jni interface
     */
    private static native void nativeInitCtx(Context ctx, EglBase.Context context);

    private static native void nativeInitEngineWithARInfo(String strDeveloperId, String strAppId,
                                                              String strAESKey, String strToken, String strPackage);

    private static native void nativeInitEngineWithAppInfo(String strAppId, String strToken, String strPackage);

    private static native void nativeConfigServerForPriCloud(String strAddr, int nPort);

    private static native void nativeForceSetAecEnable(boolean enable);

    private static native void nativeSetLogLevel(int logLevel);
}
