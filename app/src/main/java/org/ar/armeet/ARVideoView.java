package org.ar.armeet;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ar.common.utils.ScreenUtils;
import org.ar.meet_kit.HwRender;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.PercentFrameLayout;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;

/**
 * Created by liuxiaozhong on 2019/1/11.
 */
public class ARVideoView implements View.OnTouchListener {

    public RelativeLayout rlVideoGroup;//所有视频的容器布局

    private EglBase eglBase;//底层视频渲染相关对象

    private Context mContext;//上下文对象

    private VideoView LocalVideoRender;//本地视频显示对象

    private VideoView ScreenShareRender;//屏幕共享显示对象

    private LinkedHashMap<String, VideoView> mRemoteRenderList;//远程视频集合

    private static int mScreenWidth;//屏幕宽

    private static int mScreenHeight;//屏幕高

    private boolean isSameSize = false;//是否是平均大小模式
    private boolean is169 = false;//比例是否是16：9
    private int direction = Gravity.CENTER;//1大几小的时候  小像位置
    private int orientation = LinearLayout.HORIZONTAL;//1大几小的时候  小像横向或纵向排列

    private static int SUB_WIDTH = 0;
    private static int SUB_HEIGHT = 0;
    private static int BIG_SUB_WIDTH = 0;
    private static int BIG_SUB_HEIGHT = 0;
    private int bottomHeight;

    public ARVideoView(RelativeLayout rlVideoGroup, EglBase eglBase, Context context, boolean isSameSize) {

        this.rlVideoGroup = rlVideoGroup;
        this.eglBase = eglBase;
        this.mContext = context;
        this.isSameSize = isSameSize;

        mRemoteRenderList = new LinkedHashMap<>();
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        mScreenHeight = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.getStatusHeight(mContext);
        rlVideoGroup.setOnTouchListener(this);
    }


    public void setBottomHeight(int bottomHeight) {
       this.bottomHeight= (int) (((float)bottomHeight/mScreenHeight)*100f);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int startX = (int) event.getX();
            int startY = (int) event.getY();
            if (LocalVideoRender.Hited(startX, startY)) {
                return true;
            } else {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
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
            if (LocalVideoRender.Hited(startX, startY)) {
                SwitchViewToFullscreen(LocalVideoRender, GetFullScreen());
                return true;
            } else {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
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

    /**
     * 一个VideoView对象 就是一个视频渲染对象 里面的方法 UI 可以根据需求自定义
     */
    protected static class VideoView {
        public String videoId; //视频ID 保持唯一
        public int index; //视频的下标
        public int x; //装载视频的容器的起始X轴位置  最大100 最左边为0
        public int y; //装载视频的容器的起始Y轴位置  最大100 最上边为0
        public int w; //装载视频的容器的宽  最大100
        public int h; //装载视频的容器的高  最大100
        public PercentFrameLayout mLayout = null;//自定义宽高为百分比的布局控件
        public SurfaceViewRenderer surfaceViewRenderer = null; //显示视频的SurfaceView
        private FrameLayout flLoading; //视频显示前的Loading
        public VideoRenderer videoRenderer = null; //底层视频渲染对象
        public RelativeLayout rl_root;
        public TextView tv_net;

        public VideoView(final String videoId, final Context ctx, EglBase eglBase, int index, int x, int y, int w, int h) {
            this.videoId = videoId;
            this.index = index;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            mLayout = new PercentFrameLayout(ctx);
            mLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            View view = View.inflate(ctx, R.layout.layout_arvideo, null);//这个View可完全自定义 需要显示名字或者其他图标可以在里面加
            flLoading = (FrameLayout) view.findViewById(R.id.fl_video_loading);
            rl_root=view.findViewById(R.id.rl_root);
            tv_net=view.findViewById(R.id.tv_net);
            surfaceViewRenderer = (SurfaceViewRenderer) view.findViewById(R.id.sv_video_render);
            surfaceViewRenderer.init(eglBase.getEglBaseContext(), null);
            surfaceViewRenderer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mLayout.addView(view);//将SurfaceView添加到自定义宽高为百分比的布局控件中
        }

        /**
         * 该视频对象是否全屏显示
         *
         * @return true false
         */
        public Boolean isFullScreen() {
            return w == 100 || h == 100;
        }

        /**
         * 是否点击了该视频对象
         *
         * @param px
         * @param py
         * @return
         */
        public Boolean Hited(int px, int py) {
            if (!isFullScreen()) {
                int left = x * mScreenWidth / 100;
                int right = (x + w) * mScreenWidth / 100;
                int top = y *mLayout.getHeight() / 100;
                int bottom = (y + h ) * mLayout.getHeight() / 100;
                if ((px >= left && px <= right) && (py >= top && py <= bottom)) {
                    return true;
                }
            }
            return false;
        }
//        public  boolean Hited(int px, int py) {
//            Rect frame = new Rect();
//            surfaceViewRenderer.getHitRect(frame);
//            return frame.contains(px,py);
//
//        }

        public void close() {
            mLayout.removeView(surfaceViewRenderer);
            surfaceViewRenderer.release();
            surfaceViewRenderer = null;
            videoRenderer = null;
        }

    }


    /**
     * 仅用于1大几小
     * 1个大像和几个小像的时候设置
     *
     * @param is169       比例是否是16：9  true 16:9  false 4:3
     * @param direction   显示位置 左边 中间  右边
     * @param orientation 排列方式 垂直 横向
     */
    public void setVideoViewLayout(boolean is169, int direction, int orientation) {
        this.is169 = is169;
        this.direction = direction;
        this.orientation = orientation;
        if (!isSameSize) {
            changeSizeWhenRotate(false);
        }
    }


    public void updateRemoteNetStatus(String publishId,String net){
        Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VideoView> entry = iter.next();
            VideoView render = entry.getValue();
            if (render.videoId.equals(publishId)){
                render.tv_net.setText("接收："+net+"Kbps");
            }
        }
    }

    public void updateLocalNetStatus(String net){
        if (LocalVideoRender!=null){
            LocalVideoRender.tv_net.setText("发送："+net+"Kbps");
        }
    }
    /**
     * 仅用于1大几小
     * 旋转屏幕时改变尺寸
     * isFirst 是否是第一次  是的话 是不需要更新视频View的
     */
    public void changeSizeWhenRotate(boolean isFirst) {
        if (is169) {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
                SUB_WIDTH = (int) (((mScreenWidth / 4f) * 1.777777f) / (mScreenHeight / 100f));
                SUB_HEIGHT = (int) ((mScreenWidth / 4f) / (mScreenWidth / 100f));
                BIG_SUB_WIDTH=100;
                BIG_SUB_HEIGHT= (int) ((mScreenWidth*1.7777777f)/(mScreenHeight / 100f));
            } else {
                SUB_HEIGHT = (int) (((mScreenWidth / 4f) * 1.777777f) / (mScreenHeight / 100f));
                SUB_WIDTH = (int) ((mScreenWidth / 4f) / (mScreenWidth / 100f));
                BIG_SUB_WIDTH=100;
                BIG_SUB_HEIGHT= (int) ((mScreenWidth*1.7777777f)/(mScreenHeight / 100f));
            }
        } else {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
                SUB_WIDTH = (int) (((mScreenWidth / 4f) * 1.33333f) / (mScreenHeight / 100f));
                SUB_HEIGHT = (int) ((mScreenWidth / 4f) / (mScreenWidth / 100f));
                BIG_SUB_WIDTH=100;
                BIG_SUB_HEIGHT= (int) ((mScreenWidth*1.33333f)/(mScreenHeight / 100f));
            } else {
                SUB_HEIGHT = (int) (((mScreenWidth / 4f) * 1.33333f) / (mScreenHeight / 100f));
                SUB_WIDTH = (int) ((mScreenWidth / 4f) / (mScreenWidth / 100f));
                BIG_SUB_WIDTH=100;
                BIG_SUB_HEIGHT= (int) ((mScreenWidth*1.33333f)/(mScreenHeight / 100f));
            }
        }
        if (!isFirst) {
            updateVideoView1Big();
        }

    }


    /**
     * 获取视频窗口的个数
     *
     * @return
     */
    public int getVideoRenderSize() {
        int size = mRemoteRenderList.size();
        if (LocalVideoRender != null) {
            size += 1;
        }
        return size;
    }


    /**
     * 打开本地摄像头渲染对象
     *
     * @return
     */
    public VideoRenderer openLocalVideoRender() {
        int size = getVideoRenderSize();
        if (size == 0) {
            LocalVideoRender = new VideoView("localRender", rlVideoGroup.getContext(), eglBase, 0, 0,  (100-BIG_SUB_HEIGHT)/2, BIG_SUB_WIDTH, BIG_SUB_HEIGHT);
            LocalVideoRender.surfaceViewRenderer.setZOrderMediaOverlay(false);
        } else {
            LocalVideoRender = new VideoView("localRender", rlVideoGroup.getContext(), eglBase, size, 0,  (100-BIG_SUB_HEIGHT)/2, BIG_SUB_WIDTH, BIG_SUB_HEIGHT);
            LocalVideoRender.surfaceViewRenderer.setZOrderMediaOverlay(false);
        }
        rlVideoGroup.addView(LocalVideoRender.mLayout, -1);
        LocalVideoRender.mLayout.setPosition(
                LocalVideoRender.x, LocalVideoRender.y, LocalVideoRender.w, LocalVideoRender.h);
        LocalVideoRender.surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        LocalVideoRender.flLoading.setVisibility(VISIBLE);
        LocalVideoRender.surfaceViewRenderer.addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap frame) {
                Log.d("surfaceView", frame.toString());
                LocalVideoRender.surfaceViewRenderer.post(new Runnable() {
                    @Override
                    public void run() {
                        LocalVideoRender.flLoading.setVisibility(View.GONE);
                    }
                });

            }
        }, 1f);
        if (isSameSize) {
            updateVideoViewSameSize();
        } else {
            updateVideoView1Big();
        }
        LocalVideoRender.videoRenderer = new VideoRenderer(LocalVideoRender.surfaceViewRenderer);
        return LocalVideoRender.videoRenderer;
    }

    /**
     * 移除本地视频渲染对象
     */
    public void removeLocalVideoRender() {
        if (LocalVideoRender != null) {
            LocalVideoRender.close();
            LocalVideoRender.videoRenderer = null;
            rlVideoGroup.removeView(LocalVideoRender.mLayout);
            LocalVideoRender = null;
            if (isSameSize) {
                updateVideoViewSameSize();
            } else {
                updateVideoView1Big();
            }
        }
    }

    public void saveLocalPicture() {
        LocalVideoRender.surfaceViewRenderer.addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                saveImageToGallery(mContext,bitmap);
            }
        }, 1f);
    }

    public void saveRemotePicture(final String videoId) {
        VideoView remoteVideoRender = mRemoteRenderList.get(videoId);
        remoteVideoRender.surfaceViewRenderer.addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                saveImageToGallery(mContext,bitmap);
            }
        }, 1f);
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap
     * @return
     */
    public static boolean saveImageToGallery(Context context, Bitmap bitmap) {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/ARP2P通话/");
        if (!appDir.exists()) {
            // 目录不存在 则创建
            appDir.mkdirs();
        }
        String fileName = "a"+System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // 保存bitmap至本地
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
            MediaScannerConnection.scanFile(context, new String[] {file.getAbsolutePath()}, null, null);
            if (!bitmap.isRecycled()) {
                // bitmap.recycle(); 当存储大图片时，为避免出现OOM ，及时回收Bitmap
                System.gc(); // 通知系统回收
            }
        }
        return true;
    }

    /**
     * 打开远程视频渲染对象
     *
     * @param videoId 视频ID
     * @return
     */
    public VideoRenderer openRemoteVideoRender(final String videoId) {
        VideoView remoteVideoRender = mRemoteRenderList.get(videoId);
        if (remoteVideoRender == null) {
            int size = getVideoRenderSize();
            if (size == 0) {
                remoteVideoRender = new VideoView(videoId, rlVideoGroup.getContext(), eglBase, 0, 0, 0, 100, 100);
            } else {
                remoteVideoRender = new VideoView(videoId, rlVideoGroup.getContext(), eglBase, size, 0, 0, 0, 0);
                remoteVideoRender.surfaceViewRenderer.setZOrderMediaOverlay(true);
            }
            rlVideoGroup.addView(remoteVideoRender.mLayout, -1);
            remoteVideoRender.mLayout.setPosition(
                    remoteVideoRender.x, remoteVideoRender.y, remoteVideoRender.w, remoteVideoRender.h);
            remoteVideoRender.surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            remoteVideoRender.flLoading.setVisibility(VISIBLE);
            final VideoView finalRemoteVideoRender = remoteVideoRender;
            remoteVideoRender.surfaceViewRenderer.addFrameListener(new EglRenderer.FrameListener() {
                @Override
                public void onFrame(Bitmap frame) {
                    finalRemoteVideoRender.surfaceViewRenderer.post(new Runnable() {
                        @Override
                        public void run() {
                            finalRemoteVideoRender.flLoading.setVisibility(View.GONE);
                        }
                    });
                }
            }, 1f);
            remoteVideoRender.videoRenderer = new VideoRenderer(remoteVideoRender.surfaceViewRenderer);
            mRemoteRenderList.put(videoId, remoteVideoRender);
            if (isSameSize) {
                updateVideoViewSameSize();
            } else {
                if (!LocalVideoRender.isFullScreen()){
                    SwitchViewToFullscreen(LocalVideoRender,GetFullScreen());
                }
                updateVideoView1Big();
            }

        }
        return remoteVideoRender.videoRenderer;
    }

    /**
     * 移除远程像
     *
     * @param videoId
     */
    public void removeRemoteRender(String videoId) {
        VideoView remoteVideoRender = mRemoteRenderList.get(videoId);
        if (remoteVideoRender != null) {
            remoteVideoRender.close();
            rlVideoGroup.removeView(remoteVideoRender.mLayout);
            mRemoteRenderList.remove(videoId);
            sortVideoRenderIndex();
            if (isSameSize) {
                updateVideoViewSameSize();
            } else {
                updateVideoView1Big();
            }
        }
    }
    public void removeAllRemoteRender() {
        Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VideoView> entry = iter.next();
            VideoView render = entry.getValue();
            render.surfaceViewRenderer.release();
            rlVideoGroup.removeView(render.mLayout);
        }
        mRemoteRenderList.clear();
    }

    public void sortVideoRenderIndex() {
        List<Map.Entry<String, VideoView>> list = new ArrayList<Map.Entry<String, VideoView>>(mRemoteRenderList.entrySet());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).getValue().index = i + 1;
        }
    }


    //第一种 1个大 多个小 小像从中间位置开始 最多5个

    /**
     * 1个大像 5个小像示例
     * 小像横排/竖排排列
     * 小像从左边 中间 右边开始排列
     */
    private void updateVideoView1Big() {
        int size = mRemoteRenderList.size();
        if (size == 0) {
            if (LocalVideoRender != null) {
                LocalVideoRender.x = 0;
                LocalVideoRender.y =  (100-BIG_SUB_HEIGHT)/2;
                LocalVideoRender.w = BIG_SUB_WIDTH;
                LocalVideoRender.h = BIG_SUB_HEIGHT;
                LocalVideoRender.surfaceViewRenderer.setZOrderMediaOverlay(false);
                LocalVideoRender.mLayout.setPosition(LocalVideoRender.x, LocalVideoRender.y, LocalVideoRender.w, LocalVideoRender.h);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
            }
        } else {
            int startX = 0;
            int startY = 100-bottomHeight-SUB_HEIGHT;
            if (orientation == LinearLayout.HORIZONTAL) {
                if (direction == Gravity.CENTER) {
                    startX = (100 - (SUB_WIDTH * size)) / 2;//小像起始位置
                } else if (direction == Gravity.LEFT) {
                    startX = 0;
                } else if (direction == Gravity.RIGHT) {
                    startX = 100 - SUB_WIDTH;
                } else {
                    startX = (100 - (SUB_WIDTH * size)) / 2;
                }
            } else {
                if (direction == Gravity.CENTER) {
                    startX = (100 - SUB_WIDTH) / 2;//小像起始位置
                } else if (direction == Gravity.LEFT) {
                    startX = 0;
                } else if (direction == Gravity.RIGHT) {
                    startX = 100 - SUB_WIDTH;
                } else {
                    startX = (100 - SUB_WIDTH) / 2;
                }
            }

            if (LocalVideoRender != null) {
                LocalVideoRender.x = 0;
                LocalVideoRender.y = (100-BIG_SUB_HEIGHT)/2;
                LocalVideoRender.w = BIG_SUB_WIDTH;
                LocalVideoRender.h = BIG_SUB_HEIGHT;
                LocalVideoRender.surfaceViewRenderer.setZOrderMediaOverlay(false);
                LocalVideoRender.mLayout.setPosition(LocalVideoRender.x, LocalVideoRender.y, LocalVideoRender.w, LocalVideoRender.h);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
            }
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();

                if (orientation == LinearLayout.HORIZONTAL) {
                    if (direction == Gravity.CENTER) {
                        render.x = startX + (render.index - 1) * SUB_WIDTH;
                    } else if (direction == Gravity.LEFT) {
                        render.x = startX + (render.index - 1) * SUB_WIDTH;
                    } else if (direction == Gravity.RIGHT) {
                        render.x = startX - (render.index - 1) * SUB_WIDTH;
                    } else {
                        render.x = startX + (render.index - 1) * SUB_WIDTH;
                    }
                    render.y = startY;
                } else {
                    render.x = startX;
                    render.y = startY - (render.index - 1) * SUB_HEIGHT;
                }
                render.w = SUB_WIDTH;
                render.h = SUB_HEIGHT;
                render.surfaceViewRenderer.setZOrderMediaOverlay(true);
                render.mLayout.setPosition(render.x, render.y, render.w, render.h);
                render.surfaceViewRenderer.requestLayout();
            }
        }
    }

    /**
     * 适合横屏
     * 平均大小模式示例
     * 1个全屏 2个上下或左右个1  3个品字形状  4个田字形状 5个上2下3  6个上3下3
     */
    public void updateVideoViewSameSize() {
        int HEIGHT, WIDTH;
        //平均大小模式
        int size = mRemoteRenderList.size();
        if (size == 0) {
            LocalVideoRender.mLayout.setPosition(0, 0, 100, 100);
            LocalVideoRender.surfaceViewRenderer.requestLayout();
        } else if (size == 1) {
            if (!is169) {
                HEIGHT = (int) (((mScreenWidth / 2f) / 1.33333f) / (mScreenHeight / 100));
                WIDTH = (int) ((mScreenWidth / 2f) / (mScreenWidth / 100));
            } else {
                HEIGHT = (int) (((mScreenWidth / 2f) / 1.77777f) / (mScreenHeight / 100));
                WIDTH = (int) ((mScreenWidth / 2f) / (mScreenWidth / 100));
            }
            int Y = (100 - HEIGHT) / 2;
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                LocalVideoRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
                if (render.index == 1) {
                    render.mLayout.setPosition(WIDTH, Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                }
            }
        } else if (size == 2) {
            if (!is169) {
                WIDTH = (int) (((mScreenHeight / 2f) * 1.33333f) / (mScreenWidth / 100));
                HEIGHT = (int) ((mScreenHeight / 2f) / (mScreenHeight / 100));
            } else {
                WIDTH = (int) (((mScreenHeight / 2f) * 1.77777f) / (mScreenWidth / 100));
                HEIGHT = (int) ((mScreenHeight / 2f) / (mScreenHeight / 100));
            }
            int X = 0;
            int Y = 0;
//            int WIDTH = 100 / 2;
//            int HEIGHT = 50;

            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();

                VideoView render = entry.getValue();
                LocalVideoRender.mLayout.setPosition((100 - WIDTH) / 2, Y, WIDTH, HEIGHT);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
                if (render.index == 1) {
                    render.mLayout.setPosition((100 - 2 * WIDTH) / 2, Y + HEIGHT, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 2) {
                    render.mLayout.setPosition((100 - 2 * WIDTH) / 2 + WIDTH, Y + HEIGHT, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                }
            }
        } else if (size == 3) {
            if (!is169) {
                WIDTH = (int) (((mScreenHeight / 2f) * 1.33333f) / (mScreenWidth / 100));
                HEIGHT = (int) ((mScreenHeight / 2f) / (mScreenHeight / 100));
            } else {
                WIDTH = (int) (((mScreenHeight / 2f) * 1.77777f) / (mScreenWidth / 100));
                HEIGHT = (int) ((mScreenHeight / 2f) / (mScreenHeight / 100));
            }
            int X = 0;
            int Y = 0;
//            int WIDTH = 50;
//            int HEIGHT = 50;
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                LocalVideoRender.mLayout.setPosition((100 - WIDTH * 2) / 2, Y, WIDTH, HEIGHT);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
                if (render.index == 1) {
                    render.mLayout.setPosition((100 - 2 * WIDTH) / 2 + WIDTH, Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 2) {
                    render.mLayout.setPosition((100 - 2 * WIDTH) / 2, Y + HEIGHT, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 3) {
                    render.mLayout.setPosition((100 - 2 * WIDTH) / 2 + WIDTH, Y + HEIGHT, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                }
            }
        } else if (size == 4) {
            if (!is169) {
                WIDTH = (int) (((mScreenHeight / 2f) * 1.33333f) / (mScreenWidth / 100));
                HEIGHT = (int) ((mScreenHeight / 2f) / (mScreenHeight / 100));
            } else {
                HEIGHT = (int) (((mScreenWidth / 3f) / 1.77777f) / (mScreenHeight / 100));
                WIDTH = (int) ((mScreenWidth / 3f) / (mScreenWidth / 100));
            }
            int X = (100 - WIDTH * 3) / 2;
            int Y = (100 - HEIGHT * 2) / 2;
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                LocalVideoRender.mLayout.setPosition((100 - WIDTH * 2) / 2, Y, WIDTH, HEIGHT);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
                if (render.index == 1) {
                    render.mLayout.setPosition((100 - WIDTH * 2) / 2 + WIDTH, Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else {
                    if (render.index % 3 == 0) {
                        render.mLayout.setPosition(X, Y + HEIGHT, WIDTH, HEIGHT);
                        render.surfaceViewRenderer.requestLayout();
                    } else {
                        render.mLayout.setPosition(X + (render.index % 3 * WIDTH), Y + HEIGHT, WIDTH, HEIGHT);
                        render.surfaceViewRenderer.requestLayout();
                    }

                }

            }
        } else {
            if (!is169) {
                WIDTH = (int) (((mScreenHeight / 2f) * 1.33333f) / (mScreenWidth / 100));
                HEIGHT = (int) ((mScreenHeight / 2f) / (mScreenHeight / 100));
            } else {
                HEIGHT = (int) (((mScreenWidth / 3f) / 1.77777f) / (mScreenHeight / 100));
                WIDTH = (int) ((mScreenWidth / 3f) / (mScreenWidth / 100));
            }
            int X = (100 - WIDTH * 3) / 2;
            int Y = (100 - HEIGHT * 2) / 2;
//            int WIDTH = 100 / 3;
//            int HEIGHT = 30;
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                LocalVideoRender.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                LocalVideoRender.surfaceViewRenderer.requestLayout();
                if (render.index == 1) {
                    render.mLayout.setPosition(X + WIDTH, Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 2) {
                    render.mLayout.setPosition(X + (WIDTH * 2), Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 3) {
                    render.mLayout.setPosition(X, HEIGHT + Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 4) {
                    render.mLayout.setPosition(X + WIDTH, HEIGHT + Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                } else if (render.index == 5) {
                    render.mLayout.setPosition(X + (WIDTH * 2), HEIGHT + Y, WIDTH, HEIGHT);
                    render.surfaceViewRenderer.requestLayout();
                }
            }
        }
    }


    private void SwitchViewToFullscreen(VideoView view1, VideoView fullscrnView) {
        if (view1 == null || fullscrnView == null) {
            return;
        }
        if (view1.videoId.equals("ScreenShare")){
            int index, x, y, w, h;

            index = view1.index;
            x = view1.x;
            y = view1.y;
            w = view1.w;
            h = view1.h;

            view1.index = fullscrnView.index;
            view1.w = 100;
            view1.h = 27;
            view1.x = 0;
            view1.y = (int)(100-view1.h)/2;

            fullscrnView.index = index;
            fullscrnView.x = x;
            fullscrnView.y = y;
            fullscrnView.w = w;
            fullscrnView.h = h;
            fullscrnView.mLayout.setPosition(fullscrnView.x, fullscrnView.y, fullscrnView.w, fullscrnView.h);
            view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);

            updateVideoLayout(view1, fullscrnView);
        }else {
            int index, x, y, w, h;

            index = view1.index;
            x = view1.x;
            y = view1.y;
            w = view1.w;
            h = view1.h;

//            view1.index = fullscrnView.index;
//            view1.x = fullscrnView.x;
//            view1.y = fullscrnView.y;
//            view1.w = fullscrnView.w;
//            view1.h = fullscrnView.h;
            if (fullscrnView.videoId.equals("ScreenShare")){
                view1.index = fullscrnView.index;
                view1.x =0;
                view1.y = (100-BIG_SUB_HEIGHT)/2;
                view1.w = BIG_SUB_WIDTH;
                view1.h =  BIG_SUB_HEIGHT;
            }else {
                view1.index = fullscrnView.index;
                view1.x = fullscrnView.x;
                view1.y = fullscrnView.y;
                view1.w = fullscrnView.w;
                view1.h = fullscrnView.h;
            }
            fullscrnView.index = index;
            fullscrnView.x = x;
            fullscrnView.y = y;
            fullscrnView.w = w;
            fullscrnView.h = h;
            fullscrnView.mLayout.setPosition(fullscrnView.x, fullscrnView.y, fullscrnView.w, fullscrnView.h);
            view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);

            updateVideoLayout(view1, fullscrnView);
        }

    }

    /**
     * 视频切换后更新视频的布局
     *
     * @param view1
     * @param view2
     */
    private void updateVideoLayout(VideoView view1, VideoView view2) {
        if (view1.isFullScreen()) {
            view1.surfaceViewRenderer.setZOrderMediaOverlay(false);
            view2.surfaceViewRenderer.setZOrderMediaOverlay(true);
            view1.mLayout.requestLayout();
            view2.mLayout.requestLayout();
            rlVideoGroup.removeView(view1.mLayout);
            rlVideoGroup.removeView(view2.mLayout);
            rlVideoGroup.addView(view1.mLayout);
            rlVideoGroup.addView(view2.mLayout);
        } else if (view2.isFullScreen()) {
            view1.surfaceViewRenderer.setZOrderMediaOverlay(true);
            view2.surfaceViewRenderer.setZOrderMediaOverlay(false);
            view2.mLayout.requestLayout();
            view1.mLayout.requestLayout();
            rlVideoGroup.removeView(view1.mLayout);
            rlVideoGroup.removeView(view2.mLayout);
            rlVideoGroup.addView(view1.mLayout);
            rlVideoGroup.addView(view2.mLayout);
        } else {
            view1.mLayout.requestLayout();
            view2.mLayout.requestLayout();
            rlVideoGroup.removeView(view1.mLayout);
            rlVideoGroup.removeView(view2.mLayout);
            rlVideoGroup.addView(view1.mLayout);
            rlVideoGroup.addView(view2.mLayout);
        }
    }

    /**
     * 获取全屏的界面
     *
     * @return
     */
    private VideoView GetFullScreen() {
        if (LocalVideoRender.isFullScreen()) {
            return LocalVideoRender;
        }
        Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenderList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VideoView> entry = iter.next();
            String peerId = entry.getKey();
            VideoView render = entry.getValue();
            if (render.isFullScreen())
                return render;
        }
        return null;
    }
}
