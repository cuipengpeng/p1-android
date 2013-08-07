package com.p1.mobile.p1android.util;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.p1.mobile.p1android.ui.listeners.BitmapLoaderListener;

/**
 * 
 * @author Anton
 * 
 *         Loads a bitmap that is scaled to the determined optimal upload size.
 */
public class ThumbnailLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = ThumbnailLoaderTask.class.getSimpleName();
    
    private WeakReference<Context> contextRef;
    private BitmapLoaderListener listener;

    public ThumbnailLoaderTask(Context context, BitmapLoaderListener listener) {
        this.listener = listener;
        contextRef = new WeakReference<Context>(context);
    }

    @Override
    protected Bitmap doInBackground(String... imageUri) {
        try {
            Bitmap oddSizeBitmap = BitmapUtils.getCorrectlyOrientedImage(
                    contextRef.get(), imageUri[0], BitmapUtils.THUMBNAIL_SIZE);
            return BitmapUtils.getBitmapThumbnail(oddSizeBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        listener.onBitmapLoaded(bitmap);
    }
}
