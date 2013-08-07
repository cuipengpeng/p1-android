package com.p1.mobile.p1android.ui.phone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.PictureViewPagerFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;

public class GalleryPicturePagerActivity extends FlurryFragmentActivity
        implements ContextualBackListener {
    public static final String TAG = GalleryPicturePagerActivity.class
            .getSimpleName();

    private int mContentFrame = R.id.gallery_content_frame;
    private String mSelectedPictureId;
    private String mUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gallery_activity);
        setTitle("Gallery");
        Bundle bundle = getIntent().getExtras();
        mUserId = bundle.getString("userId");
        mSelectedPictureId = bundle.getString("pictureId");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        PictureViewPagerFragment picker = PictureViewPagerFragment.newInstance(
                mSelectedPictureId, mUserId);
        ft.replace(mContentFrame, picker);
        ft.commit();

    }

    public void openComment(String pictureId) {
        Intent intent = new Intent(this, FeedItemActivity.class);
        intent.putExtra("pictureId", pictureId);
        startActivity(intent);
    }

    @Override
    public void onContextualBack() {
        this.finish();
    }

}
