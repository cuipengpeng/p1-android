package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.squareup.picasso.Callback;

public class PictureViewerAdapter extends PagerAdapter {
    public static final String TAG = PictureViewerAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private List<String> mPicturesIdList = new ArrayList<String>();
    private List<ViewPicWrapper> mActiveRequesters = new ArrayList<ViewPicWrapper>();
    private OnClickListener mTapListener;
    private ImageFormat mImageFormat;

    private String mOriginId;

    public PictureViewerAdapter(Activity activity, String originId,
            OnClickListener tapListener, ImageFormat imageFormat) {
        mInflater = activity.getLayoutInflater();
        mTapListener = tapListener;
        mOriginId = originId;
        mImageFormat = imageFormat;
    }

    boolean mOriginLoaded = false;

    private class ViewPicWrapper implements IContentRequester {
        ImageView imageView;
        ProgressBar progress;

        String pictureId;
        int picPosition = POSITION_NONE;

        @Override
        public void contentChanged(Content content) {
            Log.d(TAG, "Wrapper ContentChanged");
            if (content instanceof Picture && content != null
                    && imageView.getDrawable() == null) {
                final Picture picture = (Picture) content;
                PictureIOSession io = picture.getIOSession();
                try {
                    pictureId = io.getId();
                    picPosition = mPicturesIdList.indexOf(pictureId);
                    loadImage(io.getImageUrl(mImageFormat), this,
                            pictureId.equals(mOriginId));

                } finally {
                    io.close();
                }

            }
        }

    }

    @Override
    public Object instantiateItem(View view, int position) {
        final View parentLayout = mInflater.inflate(
                R.layout.gallery_fullscreen_imageview, null);
        final ImageView imageView = (ImageView) parentLayout
                .findViewById(R.id.galleryFullscreenImage);
        final ProgressBar progress = (ProgressBar) parentLayout
                .findViewById(R.id.galleryFullscreenProgress);
        Log.d(TAG, "instantiateItem " + position);
        ViewPicWrapper wrapper = new ViewPicWrapper();
        wrapper.imageView = imageView;
        wrapper.progress = progress;
        wrapper.pictureId = mPicturesIdList.get(position);
        wrapper.progress.setVisibility(View.VISIBLE);
        wrapper.picPosition = position;

        Picture pic = ReadPicture.requestPicture(wrapper.pictureId, wrapper);

        PictureIOSession io = pic.getIOSession();
        try {
            Log.d(TAG, "Is valid " + io.isValid());
            if (io.getImageUrl(mImageFormat) != null) {

                loadImage(io.getImageUrl(mImageFormat), wrapper, pic.getId()
                        .equals(mOriginId));
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {

            io.close();
        }

        imageView.setOnClickListener(mTapListener);
        parentLayout.setTag(wrapper);
        mActiveRequesters.add(wrapper);

        ((ViewPager) view).addView(parentLayout, 0);

        return wrapper;
    }

    @Override
    public int getItemPosition(Object object) {
        ViewPicWrapper wrapper = ((ViewPicWrapper) object);

        Log.w(TAG, "getItemPosition " + wrapper.picPosition);
        if (wrapper.picPosition == POSITION_NONE) {

            return POSITION_NONE;
        } else {

            return wrapper.picPosition;
        }
    }

    public int getPicturePosition(String id) {
        return mPicturesIdList.indexOf(id);
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ViewPicWrapper wrapper = (ViewPicWrapper) object;
        ImageView imageView = (wrapper).imageView;
        View parent = (View) imageView.getParent();

        mActiveRequesters.remove(wrapper);
        ContentHandler.getInstance().removeRequester(wrapper);
        ((ViewPager) container).removeView(parent);
    }

    @Override
    public int getCount() {
        Log.d(TAG, "size " + mPicturesIdList.size());
        return mPicturesIdList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewPicWrapper wrapper = ((ViewPicWrapper) object);

        return view.equals(wrapper.imageView.getParent());
    }

    private Queue<LoadTask> mLoadTasks = new LinkedList<LoadTask>();

    private class LoadTask implements Callback {

        private String url;
        private ViewPicWrapper wrapper;
        private boolean reload;

        public LoadTask(String url, ViewPicWrapper wrapper, boolean reload) {
            this.url = url;
            this.wrapper = wrapper;
            this.reload = reload;
        }

        public void load() {
            P1Application.picasso.load(Uri.parse(url)).placeholder(null)
                    .into(wrapper.imageView, this);
        }

        @Override
        public void onError() {
            Log.d(TAG, "Picasso error ");
            if (reload) {
                mOriginLoaded = true;
                while (!mLoadTasks.isEmpty())
                    mLoadTasks.poll().load();
            }
            wrapper.imageView.setImageDrawable(null);
        }

        @Override
        public void onSuccess() {
            if (reload) {
                mOriginLoaded = true;
                while (!mLoadTasks.isEmpty())
                    mLoadTasks.poll().load();
            }
            wrapper.progress.setVisibility(View.GONE);
        }

    }

    private void loadImage(String url, ViewPicWrapper wrapper,
            final boolean isOrigin) {
        LoadTask task = new LoadTask(url, wrapper, !mOriginLoaded && isOrigin);
        if (mOriginLoaded || isOrigin) {
            task.load();
        } else {
            mLoadTasks.add(task);
        }

    }

    public Picture getPicture(int position, IContentRequester requester) {

        String pictureId = mPicturesIdList.get(position);
        Log.d(TAG, "getPicture " + position + " " + pictureId);
        return ReadPicture.requestPicture(pictureId, requester);
    }

    public void setPicturesList(List<String> pictureIdList) {
        Log.d(TAG, "Old list " + mPicturesIdList.size());
        mPicturesIdList.clear();
        mPicturesIdList.addAll(pictureIdList);
        Log.d(TAG, "New List " + pictureIdList.size());
        notifyDataSetChanged();

    }

    public String getItem(int index) {
        return mPicturesIdList.get(index);
    }

    public void onDestroy() {
        for (ViewPicWrapper requester : mActiveRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
            requester.imageView.setOnClickListener(null);
        }
    }
}
