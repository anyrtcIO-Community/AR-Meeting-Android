package org.anyrtc.armeet;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

public class MainActivity extends BaseActivity {

    EditText et_meet_id;
    TextView tvVersion;
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mImmersionBar.with(this).statusBarDarkFont(true,0.2f).keyboardEnable(true).init();
        et_meet_id=findViewById(R.id.et_meet_id);
        tvVersion=findViewById(R.id.tv_version);
        tvVersion.setText("v "+BuildConfig.VERSION_NAME);
        findViewById(R.id.tv_join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_meet_id.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"房间ID不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (AndPermission.hasPermissions(MainActivity.this,Permission.RECORD_AUDIO)){
                    startAnimActivity(MeetingActivity.class,"meet_id",et_meet_id.getText().toString());
                }else {

                    AndPermission.with(MainActivity.this).runtime().permission(Permission.RECORD_AUDIO, Permission.CAMERA).onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Toast.makeText(MainActivity.this, "请打开音视频权限", Toast.LENGTH_SHORT).show();
                        }
                    }).onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            startAnimActivity(MeetingActivity.class,"meet_id",et_meet_id.getText().toString());
                        }
                    }).start();
                }

            }
        });

    }
}
