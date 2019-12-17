package org.anyrtc.armeet;

import android.view.View;

import java.util.Calendar;

public abstract class PerfectClickListener implements View.OnClickListener {

    private int id = -1;
    private long lastClickTime = 0;
    private static final int MIN_CLICK_DELAY_TIME = 1000;

    @Override
    public void onClick(View v) {
        long curClickTime = System.currentTimeMillis();
        if (curClickTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = curClickTime;
            onNoDoubleClick(v);
        }
    }

    protected abstract void onNoDoubleClick(View v);
}
