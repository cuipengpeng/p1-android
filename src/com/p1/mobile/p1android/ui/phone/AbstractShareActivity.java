package com.p1.mobile.p1android.ui.phone;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.logic.ReadShare;
import com.p1.mobile.p1android.content.logic.WriteShare;
import com.p1.mobile.p1android.ui.dialog.ShareDialog;
import com.p1.mobile.p1android.ui.dialog.ShareDialog.ShareDialogListener;
import com.p1.mobile.p1android.ui.fragment.PictureEditFragment;

/**
 * 
 * @author Viktor Nyblom
 * 
 */
public class AbstractShareActivity extends FlurryFragmentActivity implements
        ShareDialogListener {
    public static final String SHARE_ID_KEY = "shareId";
    public static final int LOCATION_REQUEST_CODE = 1;
    private static final String TAG = AbstractShareActivity.class
            .getSimpleName();

    private Share mShare;
    private ShareDialog mDialog;
    private PictureProvider mPictureProvider;

    public interface PictureProvider {
        public List<String> getSelectedPictures();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null
                && getIntent().getExtras().containsKey(SHARE_ID_KEY)) {
            Bundle extras = getIntent().getExtras();
            mShare = ReadShare.requestShare(extras.getString(SHARE_ID_KEY),
                    null);
        }

        if (mShare == null) {
            mShare = WriteShare.initNewShare();
        }
    }

    public void setPictureProvider(PictureProvider provider) {
        mPictureProvider = provider;
    }

    public void showShareDialog() {
        FragmentManager fm = getSupportFragmentManager();
        mDialog = ShareDialog.newInstance(null);
        mDialog.show(fm, "fragment_edit_name");
    }

    @Override
    public void onLocationRequested() {
        ShareIOSession io = mShare.getIOSession();
        String shareId;
        try {
            shareId = io.getId();
        } finally {
            io.close();
        }
        Intent intent = new Intent(Actions.LOCATION);
        intent.putExtra(SHARE_ID_KEY, shareId);
        startActivityForResult(intent, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onPostRequested(String caption, String venueId) {

        WriteShare.addVenue(mShare, venueId);
        WriteShare.addCaption(mShare, caption);
        mDialog.dismiss();
        mDialog = null;
        if (mPictureProvider instanceof PictureEditFragment) {
            Log.d(TAG, "Call PictureEditFragment to save picture");
            ((PictureEditFragment) mPictureProvider).sharePicture(mShare);

        }

        finishShareProcess();
    }

    private void finishShareProcess() {
        Log.d(TAG, "finishShareProcess");
        List<String> imageUriStrings = getImageUriStrings();
        if (!imageUriStrings.isEmpty()) {
            WriteShare.addPictures(mShare, imageUriStrings);

            WriteShare.send(mShare);
        }
        cleanUp();
        Intent intent = new Intent(Actions.DEFAULT_START);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private List<String> getImageUriStrings() {
        if (mPictureProvider == null) {
            return new ArrayList<String>();
        }

        return mPictureProvider.getSelectedPictures();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivitieResult");
        if (requestCode != LOCATION_REQUEST_CODE
                || resultCode == RESULT_CANCELED) {
            return;
        }

        if (resultCode == RESULT_OK) {
            if (mDialog != null && mDialog.isVisible()) {
                mDialog.setVenueId(data.getStringExtra("venueId"));
            }
        }
    }

    public void cleanUp() {
        if (P1Application.tempCameraImage != null) {
            P1Application.tempCameraImage.recycle();
            P1Application.tempCameraImage = null;
        }
    }

}
