package org.anyrtc.weight;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.anyrtc.common.enums.AnyRTCVideoLayout;
import org.anyrtc.common.utils.ScreenUtils;
import org.anyrtc.meet_kit.AnyRTCMeetEngine;
import org.anyrtc.meeting.R;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.PercentFrameLayout;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.view.View.VISIBLE;

/**
 * Created by Eric on 2016/7/26.
 * <p>
 * Update by Ming on 2017/03/20
 */
public class RTCVideoView implements RTCViewHelper, View.OnTouchListener {
    private static final String TAG = "RTCVideoView";
    private static Context mContext;

    private static int SUB_X = 72;
    private static int SUB_Y = 2;
    private static int SUB_WIDTH = 24;
    private static int SUB_HEIGHT = 20;

    private static int mScreenWidth;
    private static int mScreenHeight;

    private HashMap<String, Boolean> mAudioSetting = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> mVideoSetting = new HashMap<String, Boolean>();

    private boolean isHost;

    public interface ViewClickEvent {
        void CloseVideoRender(View view, String strPeerId);

        void OnSwitchCamera(View view);

        void onVideoTouch(String strPeerId);
    }


    protected static class VideoView {
        public String strUserId;
        public int index;
        public int x;
        public int y;
        public int w;
        public int h;
        public PercentFrameLayout mLayout = null;
        public SurfaceViewRenderer mView = null;
        private FrameLayout tvLoading;
        public VideoRenderer mRenderer = null;


        private AnyRTCVideoLayout mRTCVideoLayout;
        private int width = mScreenWidth * SUB_WIDTH / (100 * 3);
        private int height = mScreenHeight * SUB_HEIGHT / (100 * 3);

        public VideoView(String strUserId, Context ctx, EglBase eglBase, int index, int x, int y, int w, int h, AnyRTCVideoLayout videoLayout) {
            this.strUserId = strUserId;
            this.index = index;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.mRTCVideoLayout = videoLayout;

            mLayout = new PercentFrameLayout(ctx);
            mLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            //这里的view也可以自定义
            View view = View.inflate(ctx, R.layout.layout_video, null);
            tvLoading = (FrameLayout) view.findViewById(R.id.tv_loading);
            mView = (SurfaceViewRenderer) view.findViewById(R.id.suface_view);
            mView.init(eglBase.getEglBaseContext(), null);
            mView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mLayout.addView(view);
        }

        public Boolean Fullscreen() {
            return w == 100 || h == 100;
        }

        public Boolean Hited(int px, int py) {
            if (!Fullscreen()) {
                int left = x * mScreenWidth / 100;
                int top = y * mScreenHeight / 100;
                int right = (x + w) * mScreenWidth / 100;
                int bottom = (y + h) * mScreenHeight / 100;
                if ((px >= left && px <= right) && (py >= top && py <= bottom)) {
                    return true;
                }
            }
            return false;
        }

        public void close() {
            mLayout.removeView(mView);
            mView.release();
            mView = null;
            mRenderer = null;
        }

    }

    private boolean mAutoLayout;
    private EglBase mRootEglBase;
    private static RelativeLayout mVideoView;
    private VideoView mLocalRender;
    private HashMap<String, VideoView> mRemoteRenders;
    private AnyRTCVideoLayout mRTCVideoLayout;

    public RTCVideoView(RelativeLayout videoView, Context ctx, EglBase eglBase) {
        mAutoLayout = false;
        mContext = ctx;
        mVideoView = videoView;
        mRootEglBase = eglBase;
        mLocalRender = null;
        mRemoteRenders = new HashMap<>();
        mRTCVideoLayout = AnyRTCMeetEngine.Inst().getAnyRTCMeetOption().getmVideoLayout();
        if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_3X3_auto) {
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        mScreenHeight = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.getStatusHeight(mContext);
    }

    /**
     * 设置1x3模式下点击小图像切换至全屏
     *
     * @param enable
     */
    public void setVideoSwitchEnable(boolean enable) {
        if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_1X3) {
            mVideoView.setOnTouchListener(this);
        }
    }

    /**
     * 获取视频窗口的个数
     *
     * @return
     */
    public int GetVideoRenderSize() {
        int size = mRemoteRenders.size();
        if (mLocalRender != null) {
            size += 1;
        }
        return size;
    }


    private void SwitchViewToFullscreen(VideoView view1, VideoView fullscrnView) {
        int index, x, y, w, h;

        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;

        view1.index = fullscrnView.index;
        view1.x = fullscrnView.x;
        view1.y = fullscrnView.y;
        view1.w = fullscrnView.w;
        view1.h = fullscrnView.h;

        fullscrnView.index = index;
        fullscrnView.x = x;
        fullscrnView.y = y;
        fullscrnView.w = w;
        fullscrnView.h = h;

        fullscrnView.mLayout.setPosition(fullscrnView.x, fullscrnView.y, fullscrnView.w, fullscrnView.h);
        view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);

        updateVideoLayout(view1, fullscrnView);
    }

    private void SwitchViewPosition(VideoView view1, VideoView view2) {
        int index, x, y, w, h;
//        PercentFrameLayout layout;
        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;
//        layout = view1.mLayout;

        view1.index = view2.index;
        view1.x = view2.x;
        view1.y = view2.y;
        view1.w = view2.w;
        view1.h = view2.h;
//        view1.mLayout = view2.mLayout;

        view2.index = index;
        view2.x = x;
        view2.y = y;
        view2.w = w;
        view2.h = h;
//        view2.mLayout = layout;

        view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);
        view2.mLayout.setPosition(view2.x, view2.y, view2.w, view2.h);
        updateVideoLayout(view1, view2);
    }

    /**
     * 视频切换后更新视频的布局
     *
     * @param view1
     * @param view2
     */
    private void updateVideoLayout(VideoView view1, VideoView view2) {
        if (view1.Fullscreen()) {
            view1.mView.setZOrderMediaOverlay(false);
            view2.mView.setZOrderMediaOverlay(true);
            view1.mLayout.requestLayout();
            view2.mLayout.requestLayout();
            mVideoView.removeView(view1.mLayout);
            mVideoView.removeView(view2.mLayout);
            mVideoView.addView(view1.mLayout, -1);
            mVideoView.addView(view2.mLayout, 0);
        } else if (view2.Fullscreen()) {
            view1.mView.setZOrderMediaOverlay(true);
            view2.mView.setZOrderMediaOverlay(false);
            view2.mLayout.requestLayout();
            view1.mLayout.requestLayout();
            mVideoView.removeView(view1.mLayout);
            mVideoView.removeView(view2.mLayout);
            mVideoView.addView(view1.mLayout, 0);
            mVideoView.addView(view2.mLayout, -1);
        } else {
            view1.mLayout.requestLayout();
            view2.mLayout.requestLayout();
            mVideoView.removeView(view1.mLayout);
            mVideoView.removeView(view2.mLayout);
            mVideoView.addView(view1.mLayout, 0);
            mVideoView.addView(view2.mLayout, 0);
        }
    }

    /**
     * 切换第一个视频为全屏
     *
     * @param fullscrnView
     */
    private void SwitchIndex1ToFullscreen(VideoView fullscrnView) {
        VideoView view1 = null;
        if (mLocalRender != null && mLocalRender.index == 1) {
            view1 = mLocalRender;
        } else {
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                if (render.index == 1) {
                    view1 = render;
                    break;
                }
            }
        }
        if (view1 != null) {
            SwitchViewPosition(view1, fullscrnView);
        }
    }

    public void BubbleSortSubView(VideoView view) {
        if (mLocalRender != null && view.index + 1 == mLocalRender.index) {
            SwitchViewPosition(mLocalRender, view);
        } else {
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                if (view.index + 1 == render.index) {
                    SwitchViewPosition(render, view);
                    break;
                }
            }
        }
        if (view.index < mRemoteRenders.size()) {
            BubbleSortSubView(view);
        }
    }

    /**
     * 屏幕发生变化时变换图像的大小
     */
//    private void screenChange() {
//        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            SUB_Y = 2;
//            SUB_WIDTH = 20;
//            SUB_HEIGHT = 24;
//        } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            SUB_Y = 2;
//            SUB_WIDTH = 24;
//            SUB_HEIGHT = 20;
//        }
//    }

    /**
     * 根据模板更新视频界面的布局
     */
    private void updateVideoView() {
        if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_1X3) {
            int startPosition = (100 - SUB_WIDTH * mRemoteRenders.size()) / 2;
            int remotePosition;
            int index;
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();

                VideoView render = entry.getValue();
                if (render.Fullscreen()) {
                    render = mLocalRender;
                    index = mLocalRender.index;
                } else {
                    index = render.index;
                }

                render.y = (100 - 2 * (SUB_HEIGHT + SUB_Y));

                remotePosition = startPosition + (index - 1) * SUB_WIDTH;
                render.x = remotePosition;

                if (!render.Fullscreen()) {
                    render.x = remotePosition;
                } else {
                    mLocalRender.x = remotePosition;
                }

                render.mLayout.setPosition(remotePosition, render.y, SUB_WIDTH, SUB_HEIGHT);
                render.mView.requestLayout();
            }
        } else if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_3X3_auto) {
            int size = mRemoteRenders.size();
            if (size == 0) {
                mLocalRender.mLayout.setPosition(0, 0, 100, 100);
                mLocalRender.mView.requestLayout();
            } else if (size == 1) {
                int X = 50;
                int Y = 30;
                int WIDTH = 50;
                int HEIGHT = 30;
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();

                    VideoView render = entry.getValue();
                    mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                    mLocalRender.mView.requestLayout();
                    if (render.index == 1) {
                        render.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    }
                }
            } else if (size == 2) {
                int X = 50;
                int Y = 0;
                int WIDTH = 50;
                int HEIGHT = 30;
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();

                    VideoView render = entry.getValue();
                    mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                    mLocalRender.mView.requestLayout();
                    if (render.index == 1) {
                        render.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    } else if (render.index == 2) {
                        render.mLayout.setPosition(X / 2, Y + HEIGHT, WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    }
                }
            } else if (size == 3) {
                int X = 50;
                int Y = 0;
                int WIDTH = 50;
                int HEIGHT = 30;
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();

                    VideoView render = entry.getValue();
                    mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                    mLocalRender.mView.requestLayout();
                    if (render.index == 1) {
                        render.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    } else if (render.index == 2) {
                        render.mLayout.setPosition(0, Y + HEIGHT, WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    } else if (render.index == 3) {
                        render.mLayout.setPosition(X, Y + HEIGHT, WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    }
                }
            } else if (size >= 4) {
                int X = 100 / 3;
                int Y = 0;
                int WIDTH = 100 / 3;
                int HEIGHT = 20;
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();

                    VideoView render = entry.getValue();
                    mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                    mLocalRender.mView.requestLayout();
                    if (render.index % 3 == 2) {
                        render.mLayout.setPosition(X * (render.index % 3), Y + (HEIGHT * (render.index / 3)), WIDTH + 1, HEIGHT);
                        render.mView.requestLayout();
                    } else {
                        render.mLayout.setPosition(X * (render.index % 3), Y + (HEIGHT * (render.index / 3)), WIDTH, HEIGHT);
                        render.mView.requestLayout();
                    }
                }
            }
        }
    }


    /**
     * 获取全屏的界面
     *
     * @return
     */
    private VideoView GetFullScreen() {
        if (mLocalRender.Fullscreen()) {
            return mLocalRender;
        }
        Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VideoView> entry = iter.next();
            String peerId = entry.getKey();
            VideoView render = entry.getValue();
            if (render.Fullscreen())
                return render;
        }
        return null;
    }

    /**
     * 横竖屏切换
     */
    public void onScreenChanged() {
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        mScreenHeight = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.getStatusHeight(mContext);

        if (mScreenHeight > mScreenWidth) {
            SUB_Y = 2;
            SUB_WIDTH = 24;
            SUB_HEIGHT = 20;
        } else {
            SUB_Y = 2;
            SUB_WIDTH = 20;
            SUB_HEIGHT = 24;
        }
        updateVideoView();
    }

    /**
     * Implements for AnyRTCViewEvents.
     */
    @Override
    public VideoRenderer OnRtcOpenLocalRender() {
        int size = GetVideoRenderSize();
        if (size == 0) {
            if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_1X3) {
                mLocalRender = new VideoView("localRender", mVideoView.getContext(), mRootEglBase, 0, 0, 0, 100, 100, mRTCVideoLayout);
            } else {
                mLocalRender = new VideoView("localRender", mVideoView.getContext(), mRootEglBase, 0, 0, 0, 100, 100, mRTCVideoLayout);
            }
        } else {
            mLocalRender = new VideoView("localRender", mVideoView.getContext(), mRootEglBase, size, SUB_X, (100 - size * (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT, mRTCVideoLayout);
        }
        if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_1X3) {
            mVideoView.addView(mLocalRender.mLayout, -1);
        } else {
            mVideoView.addView(mLocalRender.mLayout, 0);
        }


        mLocalRender.mLayout.setPosition(
                mLocalRender.x, mLocalRender.y, mLocalRender.w, mLocalRender.h);
        mLocalRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mLocalRender.tvLoading.setVisibility(VISIBLE);
        mLocalRender.mView.addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap frame) {
                Log.d("surfaceView", frame.toString());
                mLocalRender.mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mLocalRender.tvLoading.setVisibility(View.GONE);
                    }
                });

            }
        }, 1f);
        mLocalRender.mRenderer = new VideoRenderer(mLocalRender.mView);
        return mLocalRender.mRenderer;
    }

    @Override
    public void OnRtcRemoveLocalRender() {
        if (mLocalRender != null) {
            mLocalRender.close();
            mLocalRender.mRenderer = null;
            mVideoView.removeView(mLocalRender.mLayout);
            mLocalRender = null;
        }
    }

    @Override
    public VideoRenderer OnRtcOpenRemoteRender(final String strUserId) {
        VideoView remoteRender = mRemoteRenders.get(strUserId);
        if (remoteRender == null) {
            int size = GetVideoRenderSize();
            if (size == 0) {
                remoteRender = new VideoView(strUserId, mVideoView.getContext(), mRootEglBase, 0, 0, 0, 100, 100, mRTCVideoLayout);
            } else {
                remoteRender = new VideoView(strUserId, mVideoView.getContext(), mRootEglBase, size, SUB_X, (100 - size * (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT, mRTCVideoLayout);
                remoteRender.mView.setZOrderMediaOverlay(true);
            }

            mVideoView.addView(remoteRender.mLayout, 0);
            remoteRender.mLayout.setPosition(
                    remoteRender.x, remoteRender.y, remoteRender.w, remoteRender.h);
            remoteRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            remoteRender.tvLoading.setVisibility(VISIBLE);
            final VideoView finalRemoteRender = remoteRender;
            remoteRender.mView.addFrameListener(new EglRenderer.FrameListener() {
                @Override
                public void onFrame(Bitmap frame) {
                    Log.d("surfaceView", frame.toString());
                    finalRemoteRender.mView.post(new Runnable() {
                        @Override
                        public void run() {
                            finalRemoteRender.tvLoading.setVisibility(View.GONE);
                        }
                    });

                }
            }, 1f);
            remoteRender.mRenderer = new VideoRenderer(remoteRender.mView);
            mRemoteRenders.put(strUserId, remoteRender);
            updateVideoView();
            if (mRemoteRenders.size() == 1 && mLocalRender != null) {
                if (mRTCVideoLayout == AnyRTCVideoLayout.AnyRTC_V_1X3) {
                    SwitchViewToFullscreen(remoteRender, mLocalRender);
                }
            }
        }
        return remoteRender.mRenderer;
    }

    @Override
    public void OnRtcRemoveRemoteRender(String peerId) {
        VideoView remoteRender = mRemoteRenders.get(peerId);
        if (remoteRender != null) {
            if (remoteRender.Fullscreen()) {
                SwitchIndex1ToFullscreen(remoteRender);
            }

            if (mRemoteRenders.size() > 1 && remoteRender.index <= mRemoteRenders.size()) {
                BubbleSortSubView(remoteRender);
            }
            remoteRender.close();
            mVideoView.removeView(remoteRender.mLayout);
            mRemoteRenders.remove(peerId);
            updateVideoView();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int startX = (int) event.getX();
            int startY = (int) event.getY();
            if (mLocalRender.Hited(startX, startY)) {
                return true;
            } else {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();
                    String peerId = entry.getKey();
                    VideoView render = entry.getValue();
                    if (render.Hited(startX, startY)) {
                        return true;
                    }
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            int startX = (int) event.getX();
            int startY = (int) event.getY();
            if (mLocalRender.Hited(startX, startY)) {
                SwitchViewToFullscreen(mLocalRender, GetFullScreen());
                return true;
            } else {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();
                    String peerId = entry.getKey();
                    VideoView render = entry.getValue();
                    if (render.Hited(startX, startY)) {
                        SwitchViewToFullscreen(render, GetFullScreen());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //获取视频流渲染对象

    public VideoView openScreenShare(final String strRtcPeerId) {
        VideoView remoteRender = null;
        if (remoteRender == null) {
            remoteRender = new VideoView(strRtcPeerId, mVideoView.getContext(), mRootEglBase, 0, 0, 0, 100, 100, mRTCVideoLayout);
            remoteRender.mLayout.setPosition(
                    remoteRender.x, remoteRender.y, remoteRender.w, remoteRender.h);
            remoteRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            remoteRender.tvLoading.setVisibility(VISIBLE);
            final VideoView finalRemoteRender = remoteRender;
            remoteRender.mView.addFrameListener(new EglRenderer.FrameListener() {
                @Override
                public void onFrame(Bitmap frame) {
                    Log.d("surfaceView", frame.toString());
                    finalRemoteRender.mView.post(new Runnable() {
                        @Override
                        public void run() {
                            finalRemoteRender.tvLoading.setVisibility(View.GONE);
                        }
                    });

                }
            }, 1f);
            remoteRender.mView.setZOrderMediaOverlay(true);
            remoteRender.mRenderer = new VideoRenderer(remoteRender.mView);
        }
        return remoteRender;
    }
}
