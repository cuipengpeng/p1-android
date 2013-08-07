package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.EditProfileFragment;

public class EditProfileWrapperActivity extends FlurryFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_edit_profile_activity_content,
                EditProfileFragment.newInstance());
        ft.commit();
    }
}
