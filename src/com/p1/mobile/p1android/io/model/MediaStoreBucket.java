package com.p1.mobile.p1android.io.model;

import android.content.Context;

import com.p1.mobile.p1android.R;

/**
 * 
 * @author Viktor Nyblom
 * 
 */
public class MediaStoreBucket {

    private final String mBucketId;
    private final String mBucketName;

    public MediaStoreBucket(String id, String name) {
        mBucketId = id;
        mBucketName = name;
    }

    public String getId() {
        return mBucketId;
    }

    public String getName() {
        return mBucketName;
    }

    @Override
    public String toString() {
        return mBucketName;
    }

    public static MediaStoreBucket getAllPhotosBucket(Context context) {
        return new MediaStoreBucket(null, context.getString(R.string.album_all));
    }

}
