package com.p1.mobile.p1android.ui.listeners;

import android.graphics.Bitmap;

public interface CameraFragmentListener {

    /**
     * Called when a non-recoverable camera error has happened
     */
    public void onCameraError();

    public void onPictureTaken(Bitmap bitmap);

    public void onShowGalleries();
}
