package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class CustomGalleryAdapter extends ResourceCursorAdapter implements
        OnClickListener {
    private static final String TAG = CustomGalleryAdapter.class
            .getSimpleName();

    public interface OnPictureSelectedListener {
        public void onPictureSelectedChanged(int numberOfSelected);
    }

    private static final int MAX_SELECTED_NUM = 9;
    private static final int NUM_COLUMNS = 3;

    private List<Integer> mSelectedIdList = new ArrayList<Integer>();
    private OnPictureSelectedListener mOnPictureSelectedListener;
    private boolean mIsFromSetting = false;
    private SparseArray<Uri> mImageUriMap = new SparseArray<Uri>();
    private int mImageDimentions = 0;

    public CustomGalleryAdapter(Context context, Cursor cursor,
            OnPictureSelectedListener onPictureSelectedListener) {
        super(context, R.layout.custom_gallery_item, cursor, 0);
        init(context, onPictureSelectedListener);

    }

    public CustomGalleryAdapter(Context context, Cursor cursor,
            OnPictureSelectedListener onPictureSelectedListener,
            boolean isFromSetting) {
        super(context, R.layout.custom_gallery_item_for_setting, cursor, 0);
        init(context, onPictureSelectedListener);

        mIsFromSetting = isFromSetting;
    }

    private void init(Context context,
            OnPictureSelectedListener onPictureSelectedListener) {

        Point size = new Point();
        Utils.getScreenSize(size, context);
        mImageDimentions = size.x / NUM_COLUMNS;

        mOnPictureSelectedListener = onPictureSelectedListener;

    }

    @Override
    public int getCount() {
        if (getCursor() == null) {
            return 0;
        }
        return getCursor().getCount();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int imageId = cursor.getInt(cursor
                .getColumnIndexOrThrow(Images.Media._ID));
        Uri imageUri = mImageUriMap.get(imageId);
        if (imageUri == null) {
            imageUri = ContentUris.withAppendedId(
                    Images.Media.EXTERNAL_CONTENT_URI, imageId);
            mImageUriMap.append(imageId, imageUri);
        }

        ImageView imageView = (ImageView) view
                .findViewById(R.id.gallery_thumb_icon);

        if (!mIsFromSetting) {
            View selectedView = view
                    .findViewById(R.id.gallery_thumb_icon_selected);
            if (mSelectedIdList.contains(Integer.valueOf(imageId))) {
                selectedView.setSelected(true);
            } else {
                selectedView.setSelected(false);
            }
            selectedView.setOnClickListener(this);
            selectedView.setTag(Integer.valueOf(imageId));
        }

        imageView.setTag(imageUri.toString());
        P1Application.picasso.load(imageUri)
                .resize(mImageDimentions, mImageDimentions).centerCrop()
                .placeholder(null).into(imageView);
    }

    public int getSelectedCount() {
        return mSelectedIdList.size();
    }

    private boolean isBelowSelectionLimit() {
        return mSelectedIdList.size() < MAX_SELECTED_NUM;
    }

    public boolean isInSelectionMode() {
        return mSelectedIdList.size() > 0;
    }

    public void clearSelectionList() {
        mSelectedIdList.clear();
        notifySelectedChanged();
    }

    public void setPictureSelected(boolean selected, Integer imageId,
            View selectionView) {
        if (!selected && isBelowSelectionLimit()) {
            mSelectedIdList.add(imageId);
            selectionView.setSelected(true);
        } else {
            mSelectedIdList.remove(imageId);
            selectionView.setSelected(false);
        }

        notifySelectedChanged();
    }

    private void notifySelectedChanged() {
        this.notifyDataSetChanged();
        mOnPictureSelectedListener.onPictureSelectedChanged(mSelectedIdList
                .size());
    }

    @Override
    public void onClick(View view) {
        Integer imageId = (Integer) view.getTag();
        setPictureSelected(view.isSelected(), imageId, view);
    }

    public List<Integer> getSelectedIds() {
        return mSelectedIdList;
    }

}
