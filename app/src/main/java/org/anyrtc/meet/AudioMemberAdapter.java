package org.anyrtc.meet;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.anyrtc.meeting.R;
import org.anyrtc.model.MemberBean;

/**
 * Created by liuxiaozhong on 2017-10-13.
 */

public class AudioMemberAdapter extends BaseQuickAdapter<MemberBean,BaseViewHolder> {
    public AudioMemberAdapter() {
        super(R.layout.item_audio_member);
    }

    @Override
    protected void convert(BaseViewHolder helper, final MemberBean item) {
        helper.setText(R.id.tv_name,item.name);
        final ImageView iv_audio=helper.getView(R.id.iv_audio);
        if (item.isVisiable()){
            iv_audio.setVisibility(View.VISIBLE);
            Animation animation = new AlphaAnimation(1f,1f);
            animation.setDuration(360);
            iv_audio.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    item.setVisiable(false);
                    iv_audio.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }else {
            iv_audio.setVisibility(View.GONE);
        }

    }
}
