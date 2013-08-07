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
public class BitmapLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = BitmapLoaderTask.class.getSimpleName();
    
    private WeakReference<Context> contextRef;
    private BitmapLoaderListener listener;
    private boolean returnExactSize;

    /**
     * 
     * @param context
     * @param listener
     * @param returnExactSize
     *            if true, a bitmap of optimal size is returned. If false, the
     *            bitmap may be bigger.
     */
    public BitmapLoaderTask(Context context, BitmapLoaderListener listener,
            boolean returnExactSize) {
        this.listener = listener;
        this.returnExactSize = returnExactSize;
        contextRef = new WeakReference<Context>(context);
    }

    public BitmapLoaderTask(Context context, BitmapLoaderListener listener) {
        this(context, listener, true);
    }

    @Override
    protected Bitmap doInBackground(String... imageUri) {
        int measureId = PerformanceMeasure.startMeasure();
        try {
            Bitmap oddSizeBitmap = BitmapUtils.getCorrectlyOrientedImage(
                    contextRef.get(), imageUri[0],
                    BitmapUtils.TARGET_IMAGE_SAVE_SIZE);
            if (returnExactSize) {
                return BitmapUtils.getDefaultSizeBitmap(oddSizeBitmap);
            } else {
                return oddSizeBitmap;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            PerformanceMeasure.endMeasure(measureId,
                    "Bitmap load from disk time");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        listener.onBitmapLoaded(bitmap);
    }
}
