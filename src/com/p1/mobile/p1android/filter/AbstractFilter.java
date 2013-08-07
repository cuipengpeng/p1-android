package com.p1.mobile.p1android.filter;

import java.lang.ref.WeakReference;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.p1.mobile.p1android.util.Utils;

/**
 * Abstract filter class using an AsyncTask to initiate the filter
 * asynchronously.
 * 
 * Subclasses must implement the initFilter method providing the GPUImageFilter
 * associated with the filter and the getFilterType method.
 * 
 * @author Viktor Nyblom
 * 
 */
public abstract class AbstractFilter implements Filter {
    private static final String TAG = AbstractFilter.class.getSimpleName();

    private GPUImageFilter mFilter;
    private String mName = "AbstractFilter";

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public GPUImageFilter getGPUFilter() {
        return mFilter;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void applyFilter(Context context, ImageView imageView) {
        Log.d(TAG, getName() + " applyFilter");
        if (mFilter == null) {
            Log.d(TAG, getName() + " new FilterTask");
            Log.d(TAG, getName() + " context is null " + (context == null));
            Log.d(TAG, getName() + " imageview is null " + (imageView == null));
            FilterTask task = new FilterTask(imageView, context);
            if (Utils.hasHoneycomb()) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        (Void[]) null);
            } else {
                task.execute();
            }
        } else {
            Log.d(TAG, getName() + " just set " + mFilter.toString());
            setFilter(context, imageView);
        }
    }

    @Override
    public void applyFilter(Context context, GPUImageView imageView) {
        Log.d(TAG, getName() + " applyFilter");
        imageView.setFilter(getGPUFilter());

    }

    private void setFilter(Context context, ImageView imageView) {
        Log.d(TAG, getName() + " setFilter");
        Bitmap bitmap = getBitmapWithFilterApplied(
                ((BitmapDrawable) imageView.getDrawable()).getBitmap(), context);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * Method initiating the filter on its own thread. Implement this method to
     * return the filter or filter group desired.
     * 
     * @param context
     * @return
     */
    protected abstract GPUImageFilter initFilter(Context context);

    private class FilterTask extends AsyncTask<Void, Void, GPUImageFilter> {
        private WeakReference<ImageView> imageView;
        private WeakReference<Context> contextRef;

        public FilterTask(ImageView imageView, Context context) {
            Log.d(TAG, "FilterTask ");
            this.imageView = new WeakReference<ImageView>(imageView);
            this.contextRef = new WeakReference<Context>(context);
        }

        @Override
        protected GPUImageFilter doInBackground(Void... params) {
            Log.d(TAG, "Is context available "
                    + ((contextRef != null) && (contextRef.get() != null)));
            return initFilter(contextRef.get());
        }

        @Override
        protected void onPostExecute(GPUImageFilter filter) {
            Log.d(TAG, "FilterTask postExcecute setFilter " + (mFilter != null));
            mFilter = filter;
            setFilter(contextRef.get(), imageView.get());
            imageView = null;
            contextRef = null;
        }

    }

    public Bitmap getBitmapWithFilterApplied(Bitmap originalBitmap,
            Context context) {
        GPUImage gpuImage = new GPUImage(context);
        gpuImage.setFilter(mFilter);
        return gpuImage.getBitmapWithFilterApplied(originalBitmap);

    }
}
