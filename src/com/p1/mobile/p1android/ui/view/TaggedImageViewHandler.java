package com.p1.mobile.p1android.ui.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.io.model.Item;
import com.p1.mobile.p1android.io.model.tags.PictureTag;
import com.p1.mobile.p1android.io.model.tags.SimpleTag;
import com.p1.mobile.p1android.io.model.tags.TagEntity;
import com.p1.mobile.p1android.ui.listeners.OnViewMeasuredListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.util.BitmapUtils;

/**
 * 
 * @author Anton
 * 
 *         Handles an image view used for creating and displaying tag objects.
 *         The same instance of the TaggedImageViewHandler should be kept even
 *         when views are lost.
 */
public class TaggedImageViewHandler implements OnViewMeasuredListener {
    public static final String TAG = TaggedImageViewHandler.class
            .getSimpleName();
    private static final int TAG_TEXT_VERTICAL_PADDING = 0; // specified in dp
    private static final int TAG_TEXT_HORIZONTAL_PADDING = 15; // specified in
                                                               // dp
    private static final boolean MAX_ONE_TAG = true;

    private TagEventListener mTagEventListener;
    private RelativeLayout mRootView;
    private OptimizedImageView mImageView;
    private Bitmap mBitmap;
    private float mBitmapAspect;

    private List<SimpleTag> mTags = new ArrayList<SimpleTag>();
    private List<View> mTagViews = new ArrayList<View>();

    /**
     * 
     * @param tagEventListener
     * @param pictureUri
     * @param disposableActivity
     *            A currently valid activity that will not be stored.
     */
    public TaggedImageViewHandler(TagEventListener tagEventListener,
            String pictureUri, Activity disposableActivity) {
        Log.d(TAG, "Created");

        Log.d(TAG, "PictureUri" + pictureUri);

        try {
            mBitmap = BitmapUtils.getCorrectlyOrientedImage(disposableActivity,
                    pictureUri, 0);
            mBitmapAspect = (float) mBitmap.getWidth() / mBitmap.getHeight();
            Log.d(TAG, "Bitmap aspect: " + mBitmapAspect);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Must be called each time the view is lost.
     * 
     * @param rootView
     *            based on xml layout "tagged_image_view_layout.xml"
     */
    public void setView(RelativeLayout rootView) {
        mTagViews.clear();
        mRootView = rootView;
        mImageView = (OptimizedImageView) rootView
                .findViewById(R.id.taggedImage);

        mImageView.setImageBitmap(mBitmap);
        mImageView.addMeasureListener(this);

        for (SimpleTag tag : mTags) {
            createTagView(tag);
        }
        mImageView.setOnTouchListener(new TagDetector(rootView.getContext()));

    }

    /**
     * Creates a new tag and displays it.
     * 
     * @param tagEntity
     *            May be null
     * @param locationX
     *            Percentage position
     * @param locationY
     *            Percentage position
     */
    public SimpleTag createTag(TagEntity tagEntity, float locationX,
            float locationY) {

        if ((MAX_ONE_TAG && mTags.isEmpty()) || !MAX_ONE_TAG) {
            if (locationX >= 0 && locationX <= 1 && locationY >= 0
                    && locationY <= 1) {
                SimpleTag newTag = new PictureTag(tagEntity, locationX,
                        locationY);
                mTags.add(newTag);
                createTagView(newTag);
                if (mTagEventListener != null) {
                    mTagEventListener.tagAdded(mTagViews.get(0)); // TODO fetch
                                                                  // correct one
                }
                return newTag;
            } else {
                Log.w(TAG, "Disallowed tag creation outside drawable");
                return null;
            }
        } else {
            Log.w(TAG, "Disallowed creating a tag due to tag limit of one");
            return null;
        }
    }

    private void createTagView(SimpleTag tag) {
        if (mImageView != null) {

            Activity activity = (Activity) mImageView.getContext();
            TextView tagView = new P1TextView(activity);
            tagView.setBackgroundResource(R.drawable.pillshape_tag);
            tagView.setText(tag.getTitle());
            tagView.setTextColor(activity.getResources()
                    .getColor(R.color.white));
            tagView.setTextSize(activity.getResources().getDimension(
                    R.dimen.small_text_size));
            tagView.setShadowLayer(1f, 0f, -1f, R.color.black);
            tagView.setGravity(Gravity.CENTER);
            float scale = activity.getResources().getDisplayMetrics().density;
            tagView.setPadding((int) (TAG_TEXT_HORIZONTAL_PADDING * scale),
                    (int) (TAG_TEXT_VERTICAL_PADDING * scale),
                    (int) (TAG_TEXT_HORIZONTAL_PADDING * scale),
                    (int) ((TAG_TEXT_VERTICAL_PADDING + 2) * scale)); // +2 for
                                                                      // some
                                                                      // extra
                                                                      // bottom
                                                                      // padding

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(getViewX(tag)
                    - (int) (TAG_TEXT_HORIZONTAL_PADDING * scale * 2),
                    getViewY(tag), 0, 0);

            Log.d(TAG, "Creating a tag at " + getViewX(tag) + 'x'
                    + getViewY(tag));

            mRootView.addView(tagView, params);
            tagView.setTag(tag);
            tagView.setMaxLines(1);
            mTagViews.add(tagView);
        }
    }

    @Override
    public void onViewMeasured() {
        Log.d(TAG,
                "Root view measured - mImageView measurements: "
                        + mImageView.getMeasuredWidth() + 'x'
                        + mImageView.getMeasuredHeight());
        for (View tagView : mTagViews) {
            SimpleTag tagInfo = ((SimpleTag) tagView.getTag());
            ((RelativeLayout.LayoutParams) tagView.getLayoutParams())
                    .setMargins(getViewX(tagInfo) - tagView.getWidth() / 2,
                            getViewY(tagInfo), 0, 0);
        }
    }

    /**
     * Translates the tag location (percentage of image) into pixel coordinates
     * for the view
     * 
     * @param tag
     * @return
     */
    private int getViewX(SimpleTag tag) { // TODO Take into account that the
                                          // Drawable does not cover the entire
                                          // ImageView.
        return (int) (mImageView.getMeasuredWidth() * tag.getLocationX());
    }

    public float getXPercent(float xPosPixels) {
        return xPosPixels / mImageView.getMeasuredWidth(); // Relies on that
                                                           // entire width is
                                                           // filled
    }

    /**
     * Translates the tag location (percentage of image) into pixel coordinates
     * for the view Assumes that drawable fills width
     * 
     * @param tag
     * @return
     */
    private int getViewY(SimpleTag tag) {
        float viewAspect = (float) mImageView.getWidth()
                / mImageView.getHeight();
        float drawableHeight = (1 / mBitmapAspect) / (1 / viewAspect); // Drawable
                                                                       // height
                                                                       // as
                                                                       // percent
                                                                       // of
                                                                       // view
                                                                       // height.
                                                                       // Assumes
                                                                       // that
                                                                       // drawable
                                                                       // fills
                                                                       // width
        float borderHeight = (1 - drawableHeight) / 2; // Darkness above
                                                       // drawable

        return (int) ((tag.getLocationY() * drawableHeight + borderHeight) * mImageView
                .getMeasuredHeight());
    }

    /**
     * Assumes that drawable fills width
     * 
     * @param yPosPixels
     *            Pixel position of view
     * @return percentage position on the drawable
     */
    public float getYPercent(float yPosPixels) {
        float viewAspect = (float) mImageView.getWidth()
                / mImageView.getHeight();
        float drawableHeight = (1 / mBitmapAspect) / (1 / viewAspect); // Drawable
                                                                       // height
                                                                       // as
                                                                       // percent
                                                                       // of
                                                                       // view
                                                                       // height.
        float borderHeight = (1 - drawableHeight) / 2; // Darkness above
                                                       // drawable
        return (yPosPixels / mImageView.getHeight() - borderHeight)
                / drawableHeight;
    }

    public SimpleTag getTag(int nr) {
        if (mTags.size() > nr) {
            return mTags.get(nr);
        }
        Log.w(TAG, "Tried to fetch a tag that doesn't exist.");
        return null;
    }

    public View getTagView(int nr) {
        if (mTagViews.size() > nr) {
            return mTagViews.get(nr);
        }
        Log.w(TAG, "Tried to fetch a tagView that doesn't exist.");
        return null;
    }

    private class TagDetector extends SimpleOnGestureListener implements
            OnTouchListener {

        private GestureDetectorCompat detector;

        public TagDetector(Context context) {
            detector = new GestureDetectorCompat(context, this);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float locationX = getXPercent(e.getX()); // mRootView.getWidth();
            float locationY = getYPercent(e.getY());
            createTag(null, locationX, locationY);
            Log.d(TAG, "Tap on screen");
            return super.onSingleTapConfirmed(e);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            detector.onTouchEvent(event);
            return true;
        }

    }

    public void dispose() {
        mBitmap.recycle();
    }

    public void clearTags() {
        mTags.clear();
        for (View tagView : mTagViews) {
            ((ViewGroup) tagView.getParent()).removeView(tagView);
        }
        mTagViews.clear();
    }

    public void setTagEventListener(TagEventListener listener) {
        mTagEventListener = listener;
    }

    public interface TagEventListener {
        public void tagAdded(View tagView);

    }

    public void setTagEntity(Item item, int tagId) {
        if (mTags.size() > tagId) {
            mTags.get(tagId).setEntity(item);
            ((TextView) mTagViews.get(tagId)).setText(item.getTagTitle());
        }
    }

}
