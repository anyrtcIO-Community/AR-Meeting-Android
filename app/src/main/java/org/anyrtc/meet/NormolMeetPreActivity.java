package org.anyrtc.meet;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.anyrtc.AnyRTCApplication;
import org.anyrtc.BaseActivity;
import org.anyrtc.meeting.R;

import butterknife.BindView;
import butterknife.OnClick;

public class NormolMeetPreActivity extends BaseActivity {


    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.rg_type)
    RadioGroup rgType;
    @BindView(R.id.view_space)
    View viewSpace;
    Bundle bundle;


    private static Object sLockObj=new Object();
    @Override
    public int getLayoutId() {
        return R.layout.activity_normol_meet_pre;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tvName.setText(AnyRTCApplication.getNickName());
        bundle=new Bundle();

    }

    static class WaitThread extends Thread{
        @Override
        public void run() {
            try{
                synchronized (sLockObj){
                   Thread.sleep(3000);
                   sLockObj.notifyAll();
                }
            }catch (Exception e){

            }
        }
    }


    @OnClick({R.id.iv_back, R.id.rb_4_360, R.id.rb_4_720, R.id.rb_4_1080, R.id.rb_9_0, R.id.rb_9_1, R.id.rb_9_2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finishAnimActivity();
                break;
            case R.id.rb_4_360:
                bundle.putString("id","anymeeting10000");
                bundle.putInt("mode",360);
                bundle.putInt("MaxJoiner",4);
                startAnimActivity(MeetingActivity.class,bundle);
                break;
            case R.id.rb_4_720:
                bundle.putString("id","anymeeting10001");
                bundle.putInt("mode",720);
                bundle.putInt("MaxJoiner",4);
                startAnimActivity(MeetingActivity.class,bundle);
                break;
            case R.id.rb_4_1080:
                bundle.putString("id","anymeeting10002");
                bundle.putInt("mode",1080);
                bundle.putInt("MaxJoiner",4);
                startAnimActivity(MeetingActivity.class,bundle);
                break;
            case R.id.rb_9_0:
                bundle.putString("id","anymeeting10003");
                bundle.putInt("mode",9);
                startAnimActivity(MeetingActivity.class,bundle);
                break;
            case R.id.rb_9_1:
                bundle.putString("id","anymeeting10004");
                bundle.putInt("mode",1);
                startAnimActivity(AudioMeetingActivity.class,bundle);
                break;
            case R.id.rb_9_2:
                bundle.putString("id","anymeeting10005");
                bundle.putInt("mode",2);
                startAnimActivity(AudioMeetingActivity.class,bundle);
                break;
        }
    }




}
