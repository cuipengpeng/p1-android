package com.p1.mobile.p1android.ui.phone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.PictureEditFragment;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;

public class PictureEditActivity extends AbstractShareActivity implements
        ContextualBackListener {
    private static final String TAG = PictureEditActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = "editfragment";

    private static final String TYPE_IMAGE = "image/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.picture_edit_activity);

        Intent intent = getIntent();

        String uriString = intent.getExtras().getString("uri");
        boolean bitmapExists = intent.getExtras().getBoolean("bitmapExists");

        Log.d(TAG, "Type " + intent.getType());
        Log.d(TAG, "Action " + intent.getAction());

        if (Intent.ACTION_SEND.equals(intent.getAction())
                && (intent.getType() != null)) {
            if (intent.getType().startsWith(TYPE_IMAGE)) {
                Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                uriString = uri.toString();
            }
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG);
        Log.d(TAG, "Fragment found with tag " + (fragment == null));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = PictureEditFragment.newInstance(uriString, bitmapExists);
        }
        ft.replace(R.id.editPictureFragment, fragment, FRAGMENT_TAG);
        ft.commit();
    }

    @Override
    public void onContextualBack() {
        cleanUp();
        this.finish();
    }
}
