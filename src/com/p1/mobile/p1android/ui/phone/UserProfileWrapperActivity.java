package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.UserProfileFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;

public class UserProfileWrapperActivity extends FlurryFragmentActivity
        implements ContextualBackListener {

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        Bundle bundle = getIntent().getExtras();
        mUserId = bundle.getString(UserProfileFragment.USER_ID_KEY);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_user_profile_activity_content,
                UserProfileFragment.newInstance(mUserId));
        ft.commit();
    }

    @Override
    public void onContextualBack() {
        this.finish();
    }

}
