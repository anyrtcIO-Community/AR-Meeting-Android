package org.anyrtc.meet_kit;

import android.content.Context;

import org.ar.common.BuildConfig;
import org.anyrtc.common.utils.DeviceUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.anyrtc.common.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.concurrent.Exchanger;

/**
 * @author Eric
 * @date 2017/10/17
 */
@Deprecated
public class AnyRTCMeetEngine {

    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("meet-jni");
    }

    private final LooperExecutor executor;
    private final EglBase eglBase;
    private Context context;
    private String developerId, appId, appKey, appToken;
    private String strSvrAddr = "cloud.anyrtc.io";
    private AnyRTCMeetOption anyRTCMeetOption = new AnyRTCMeetOption();

    private static class SingletonHolder {
        private static final AnyRTCMeetEngine INSTANCE = new AnyRTCMeetEngine();
    }

    public static final AnyRTCMeetEngine Inst() {
        return SingletonHolder.INSTANCE;
    }

    public Context context() {
        return context;
    }

    public AnyRTCMeetOption getAnyRTCMeetOption() {
        return anyRTCMeetOption;
    }

    public void setAnyRTCMeetOption(AnyRTCMeetOption anyRTCMeetOption) {
        this.anyRTCMeetOption = anyRTCMeetOption;
    }

    private AnyRTCMeetEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
//        disableHWEncode();
//        disableHWDecode();
        executor.requestStart();
    }

    public LooperExecutor Executor() {
        return executor;
    }

    public EglBase Egl() {
        return eglBase;
    }

    public static void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    public static void disableHWDecode() {
        MediaCodecVideoDecoder.disableVp8HwCodec();
        MediaCodecVideoDecoder.disableVp9HwCodec();
        MediaCodecVideoDecoder.disableH264HwCodec();
    }

    /**
     * 配置开发者信息
     *
     * @param ctx            应用上下文环境
     * @param strDeveloperId anyRTC开发者id
     * @param strAppId       anyRTC平台应用的appId
     * @param strAESKey         anyRTC平台应用的appKey
     * @param strToken       anyRTC平台应用的appToken
     */
    public void initEngineWithAnyrtcInfo(final Context ctx, final String strDeveloperId, final String strAppId,
                                         final String strAESKey, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                developerId = strDeveloperId;
                appId = strAppId;
                appKey = strAESKey;
                appToken = strToken;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                context = ctx;
                nativeInitEngineWithAnyrtcInfo(strDeveloperId, strAppId, strAESKey, strToken);
            }
        });
    }

    /**
     * 配置开发者信息
     *
     * @param ctx            应用上下文环境
     * @param strAppId       anyRTC平台应用的appId
     * @param strToken       anyRTC平台应用的appToken
     */
    public void initEngineWithAppInfo(final Context ctx, final String strAppId, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
//                developerId = strDeveloperId;
                appId = strAppId;
//                appKey = strAESKey;
                appToken = strToken;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                context = ctx;
                nativeInitEngineWithAppInfo(strAppId, strToken);
            }
        });
    }

    public String getPackageName() {
        return context.getPackageName();
    }

    public void configServerForPriCloud(final String strAddr, final int nPort) {
        strSvrAddr = strAddr;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeConfigServerForPriCloud(strAddr, nPort);
            }
        });
    }

    protected void setAuidoModel(final boolean enabled, final boolean audioDetect) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAuidoModel(enabled, audioDetect);
            }
        });
    }

    /**
     * 打开或关闭前置摄像头镜面
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean bEnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetCameraMirror(bEnable);
            }
        });
    }

    /**
     * 打开或关闭网络状态监测
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean bEnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetNetworkStatus(bEnable);
            }
        });
    }

    public boolean networkStatusEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeNetworkStatusEnabled();
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    public void dispose() {
        executor.requestStop();
    }

    /**
     * 获取sdk版本号
     *
     * @return RTMPC版本号
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
            jsonObject.put("operatorName", NetworkUtils.getNetworkOperatorName());
            jsonObject.put("devType", DeviceUtils.getModel());
            jsonObject.put("networkType", NetworkUtils.getNetworkType().toString().replace("NETWORK_", ""));
            jsonObject.put("osType", "Android");
            jsonObject.put("sdkVer", getSdkVersion());
            jsonObject.put("rtcVer", 60);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * Jni interface
     */
    private static native void nativeInitCtx(Context ctx, EglBase.Context context);

    private static native void nativeInitEngineWithAnyrtcInfo(String strDeveloperId, String strAppId,
                                                              String strAESKey, String strToken);

    private static native void nativeInitEngineWithAppInfo(String strAppId, String strToken);

    private static native void nativeConfigServerForPriCloud(String strAddr, int nPort);

    private static native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);

    private native void nativeSetCameraMirror(boolean bEnable);

    private native void nativeSetNetworkStatus(boolean bEnable);

    private native boolean nativeNetworkStatusEnabled();
}
