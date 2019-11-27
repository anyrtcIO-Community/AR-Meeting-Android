package org.anyrtc.armeet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ar.common.utils.SharePrefUtil;

public class InputDevInfoActivity extends BaseActivity {

    private EditText et_appid, et_token, et_ip;
    private Button btnOk;

    @Override
    public int getLayoutId() {
        return R.layout.activity_input_dev_info;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

        et_appid = findViewById(R.id.et_appid);
        et_token = findViewById(R.id.et_apptoken);
        et_ip = findViewById(R.id.et_ip);
        btnOk=findViewById(R.id.btn_ok);

        String appid = SharePrefUtil.getString("appid");
        String apptoken = SharePrefUtil.getString("apptoken");
        String ip = SharePrefUtil.getString("ip");

        if (!TextUtils.isEmpty(appid)) {
            et_appid.setText(appid);
        }
        if (!TextUtils.isEmpty(apptoken)) {
            et_token.setText(apptoken);
        }
        if (!TextUtils.isEmpty(ip)) {
            et_ip.setText(ip);
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_appid.getText().toString().trim().isEmpty() || et_token.getText().toString().trim().isEmpty()) {
                    Toast.makeText(InputDevInfoActivity.this, "请输入完整", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharePrefUtil.putString("appid", et_appid.getText().toString().trim());
                SharePrefUtil.putString("apptoken", et_token.getText().toString().trim());

                if (!et_ip.getText().toString().trim().isEmpty()) {
                    SharePrefUtil.putString("ip", et_ip.getText().toString().trim());
                }
                SharePrefUtil.putBoolean("isDevMode", true);
                Toast.makeText(InputDevInfoActivity.this, "请重启应用", Toast.LENGTH_SHORT).show();
                System.exit(0);

            }
        });
    }
}
