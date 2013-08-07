package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;

public class DeleteShare {
    public static final String TAG = DeleteShare.class.getSimpleName();

    public static void deleteShare(Share share) {
        ShareIOSession io = share.getIOSession();
        try {
            io.setValid(false);
        } finally {
            io.close();
        }
        share.notifyListeners();
        Log.w(TAG, "Delete is not yet fully implemented");
    }

    public static void reportShare(Share share) {
        Log.w(TAG, "Reporting is not yet fully implemented");
    }

}
