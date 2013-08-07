package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;

public class DeletePicture {
    public static final String TAG = DeletePicture.class.getSimpleName();

    public static void deletePicture(Picture picture) {
        PictureIOSession io = picture.getIOSession();
        try {
            io.setValid(false);
        } finally {
            io.close();
        }
        picture.notifyListeners();
        Log.w(TAG, "Delete is not yet fully implemented");
    }

    public static void reportPicture(Picture picture) {
        Log.w(TAG, "Reporting is not yet fully implemented");
    }

}
