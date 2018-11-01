package org.anyrtc.meet;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.anyrtc.AnyRTCApplication;
import org.anyrtc.BaseActivity;
import org.anyrtc.meeting.R;
import org.anyrtc.utils.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class AdvancedMeetPreActivity extends BaseActivity {


    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.et_id)
    EditText etId;
    @BindView(R.id.view_space)
    View viewSpace;

    @Override
    public int getLayoutId() {
        return R.layout.activity_advanced_meet_pre;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tvName.setText(AnyRTCApplication.getNickName());

    }


    @OnClick(R.id.iv_back)
    public void onClick() {
        finishAnimActivity();
    }

    @OnClick({R.id.rb_join_meet})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rb_join_meet:
                ToastUtil.show("敬请期待！");
                break;
        }
    }
}
