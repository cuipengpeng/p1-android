package com.p1.mobile.p1android.ui.phone;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import android.widget.Toast;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.CameraPreviewFragment;
import com.p1.mobile.p1android.ui.fragment.CustomGalleryFragment;
import com.p1.mobile.p1android.ui.listeners.CameraFragmentListener;
import com.p1.mobile.p1android.ui.listeners.CancelShareListener;

public class CameraActivity extends FlurryFragmentActivity implements
        CameraFragmentListener, CancelShareListener {
    static final String TAG = CameraActivity.class.getSimpleName();

    public static final String CHANGE_PROFILE_PICTURE_KEY = "change_profile_picture";
    public static final String CHANGE_COVER_PICTURE_KEY = "change_cover_picture";

    private final int CONTENT_FRAME = R.id.camera_frame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide actionbar and status bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.camera_activity_layout);

        Fragment fragment = CameraPreviewFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.camera_frame, fragment);

        ft.commit();
    }

    @Override
    public void onCameraError() {
        Toast.makeText(this, "Camera error", Toast.LENGTH_LONG).show();
        this.finish();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onPictureTaken(Bitmap bitmap) {
        P1Application.tempCameraImage = bitmap;
        startEditActivity();
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @Override
    public void onShowGalleries() {
        Fragment fragment = CustomGalleryFragment.newInstance(false, false);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(CONTENT_FRAME, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onCancelShare() {
        this.finish();
    }

    private void startEditActivity() {
        Intent intent = new Intent(this, PictureEditActivity.class);
        intent.putExtra("bitmapExists", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        this.finish();
    }
}
