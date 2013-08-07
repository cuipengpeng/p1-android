/**
 * GalleryImagePickerActivity.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.CustomGalleryFragment;
import com.p1.mobile.p1android.ui.fragment.SettingFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;

/**
 * @author Viktor Nyblom
 * 
 */
public class CustomGalleryActivity extends AbstractShareActivity implements
        ContextualBackListener {
    private static final String TAG = CustomGalleryActivity.class
            .getSimpleName();
    private static final int CONTENT_FRAME = R.id.gallery_image_picker_frame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isChangeProfilePicture = false;
        boolean isChangeCoverPicture = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String changeStr = bundle.getString(SettingFragment.CHANGE_KEY);
            if (changeStr.equals(SettingFragment.CHANGE_COVER_PICTURE)) {
                isChangeCoverPicture = true;
            }
            if (changeStr.equals(SettingFragment.CHANGE_PROFILE_PICTURE)) {
                isChangeProfilePicture = true;
            }
        }
        setContentView(R.layout.gallery_image_picker_activity);

        setTitle("Test");

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            CustomGalleryFragment fragment = CustomGalleryFragment.newInstance(
                    isChangeCoverPicture, isChangeProfilePicture);
            ft.replace(CONTENT_FRAME, fragment, TAG);
            ft.commit();
        }
    }

    @Override
    public void onContextualBack() {
        this.finish();
    }

}
