package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.fragment.SettingFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;

public class SettingActivity extends FlurryFragmentActivity implements
        ContextualBackListener {

    private String mLoggedInUserId;
    private SettingFragment mSettingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        mLoggedInUserId = NetworkUtilities.getLoggedInUserId();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_setting_activity_content,
                SettingFragment.newInstance(null));
        ft.commit();
    }

    @Override
    public void onContextualBack() {
        this.finish();
    }
}
