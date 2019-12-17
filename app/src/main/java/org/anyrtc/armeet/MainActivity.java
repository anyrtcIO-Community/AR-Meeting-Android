package org.anyrtc.armeet;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.ar.meet_kit.ARMeetEngine;

import java.util.List;

public class MainActivity extends BaseActivity {

    EditText et_meet_id;
    TextView tvVersion;

    private int mSecretNumber = 0;
    private static final long MIN_CLICK_INTERVAL = 600;
    private long mLastClickTime;
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
        findViewById(R.id.tv_join).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                        if (et_meet_id.getText().toString().isEmpty()){
                            Toast.makeText(MainActivity.this,"房间ID不能为空",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (AndPermission.hasPermissions(MainActivity.this,Permission.RECORD_AUDIO,Permission.CAMERA,Permission.WRITE_EXTERNAL_STORAGE,Permission.READ_EXTERNAL_STORAGE)){
                            startAnimActivity(MeetingActivity.class,"meet_id",et_meet_id.getText().toString());
                        }else {

                            AndPermission.with(MainActivity.this).runtime().permission(Permission.RECORD_AUDIO,Permission.CAMERA,Permission.WRITE_EXTERNAL_STORAGE,Permission.READ_EXTERNAL_STORAGE).onDenied(new Action<List<String>>() {
                                @Override
                                public void onAction(List<String> data) {
                                    Toast.makeText(MainActivity.this, "请打开音视频,文件读写权限", Toast.LENGTH_SHORT).show();
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

        findViewById(R.id.tv_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentClickTime = SystemClock.uptimeMillis();
                long elapsedTime = currentClickTime - mLastClickTime;
                mLastClickTime = currentClickTime;

                if (elapsedTime < MIN_CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (9 == mSecretNumber) {
                        try {
                            Toast.makeText(MainActivity.this,"进入开发者模式",Toast.LENGTH_SHORT).show();
                            startAnimActivity(InputDevInfoActivity.class);
                        } catch (Exception e) {
                        }
                        mSecretNumber=0;
                    }
                } else {
                    mSecretNumber = 0;
                }
            }
        });

    }
}
