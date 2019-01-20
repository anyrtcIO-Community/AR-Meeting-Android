package org.anyrtc;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.meet.AdvancedMeetPreActivity;
import org.anyrtc.meet.NormolMeetPreActivity;
import org.anyrtc.meeting.R;
import org.anyrtc.utils.PermissionsCheckUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    public final static int REQUECT_CODE_CAMARE = 1;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

        public class Student{
            int index;

            public Student(int index) {
                this.index = index;
            }
        }
    @Override
    public void initView(Bundle savedInstanceState) {
        LinkedHashMap<String, Student> map = new LinkedHashMap<>();
        map.put("zhangsan",  new Student(1));
        map.put("lisi", new Student(2));
        map.put("wangwu", new Student(3));
        map.put("dsdd", new Student(4));
        map.put("ds", new Student(5));


        map.remove("dsdd");
        List<Map.Entry<String, Student>> list = new ArrayList<Map.Entry<String, Student>>(map.entrySet());
        for (int i=0;i<list.size();i++){
            list.get(i).getValue().index=i+1;
        }
        for(Map.Entry<String, Student> t:list){
           Log.d("=============",t.getKey()+":"+t.getValue().index);
        }

        map.put("dsdd", new Student(map.size()));
        List<Map.Entry<String, Student>> list1 = new ArrayList<Map.Entry<String, Student>>(map.entrySet());
        for (int i=0;i<list1.size();i++){
            list1.get(i).getValue().index=i+1;
        }
        for(Map.Entry<String, Student> t:list1){
            Log.d("=============",t.getKey()+":"+t.getValue().index);
        }
    }


    @OnClick({R.id.tv_normol_room, R.id.tv_advanced_room, R.id.tv_call})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_normol_room:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AndPermission.with(this)
                            .requestCode(REQUECT_CODE_CAMARE)
                            .permission(Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO)
                            .callback(new PermissionListener() {
                                @Override
                                public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                    startAnimActivity(NormolMeetPreActivity.class);
                                }

                                @Override
                                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                    if (deniedPermissions.size() == 2) {
                                        PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音和相机权限");
                                        return;
                                    }
                                    for (int i = 0; i < deniedPermissions.size(); i++) {
                                        if (deniedPermissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音权限");
                                        } else {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启相机权限");
                                        }
                                    }
                                }
                            }).start();
                } else {
                    startAnimActivity(NormolMeetPreActivity.class);
                }

                break;
            case R.id.tv_advanced_room:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AndPermission.with(this)
                            .requestCode(REQUECT_CODE_CAMARE)
                            .permission(Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO)
                            .callback(new PermissionListener() {
                                @Override
                                public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                    startAnimActivity(AdvancedMeetPreActivity.class);
                                }

                                @Override
                                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                    if (deniedPermissions.size() == 2) {
                                        PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音和相机权限");
                                        return;
                                    }
                                    for (int i = 0; i < deniedPermissions.size(); i++) {
                                        if (deniedPermissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音权限");
                                        } else {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启相机权限");
                                        }
                                    }
                                }
                            }).start();
                } else {
                    startAnimActivity(AdvancedMeetPreActivity.class);
                }

                break;
            case R.id.tv_call:
                Uri uri = Uri.parse("tel:021-65650071");
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                System.exit(0);
                finishAnimActivity();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
