package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.FeedItemViewFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;

public class FeedItemActivity extends FlurryFragmentActivity implements
        ContextualBackListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_feed_item_holder);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle extras = getIntent().getExtras();
        String pictureId = "";
        String shareId = "";
        if (extras != null) {
            pictureId = extras.getString("pictureId");
            shareId = extras.getString("shareId");
        }
        ft.replace(R.id.feed_item_holder,
                FeedItemViewFragment.newInstance(shareId, pictureId));
        ft.commit();
    }

    @Override
    public void onContextualBack() {
        finish();
    }
}
