package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.UserPicturesList.UserPicturesListIOSession;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.p1.mobile.p1android.content.logic.ReadUserPictures;
import com.squareup.picasso.Picasso;

/**
 * Because GridView cannot set header view, we must use a ListView of 3 pictures
 * rows. HighestRequest etc. still counts by picture numbers, not the number of
 * rows.
 */
public class UserPictureGridAdapter extends BaseAdapter {
    public static final String TAG = UserPictureGridAdapter.class
            .getSimpleName();
    /** Load more data before getting the last one */
    private static final int LOAD_BEFORE_HAND_NUM = 15;

    private Context mContext;
    private UserPicturesList mUserPicturesList;
    private List<String> mPictureIdList = new ArrayList<String>();
    private int mHighestRequest = 0;
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    private int mPictureTotalCount = 1;

    private ImageView mTempItemImageView;
    private int mImageViewIndex;

    private ImageView mImageView0;
    private ImageView mImageView1;
    private ImageView mImageView2;

    public UserPictureGridAdapter(UserPicturesList list,  Context context) {
        super();
        this.mContext = context;
        this.mUserPicturesList = list;
    }

    @Override
    public void notifyDataSetChanged() {
        UserPicturesListIOSession io = this.mUserPicturesList.getIOSession();
        try {
            mPictureIdList.clear();
            mPictureTotalCount = io.getPaginationTotal();
            Log.d(TAG, "" + mPictureTotalCount);
            for (int i = 0; i < io.getPaginationNextOffset(); i++) {
                mPictureIdList.add(io.getPictureId(i));
                Log.d(TAG, "pictureId added " + io.getPictureId(i));
            }
        } finally {
            io.close();
        }

        Log.d(TAG, "Dataset changed. Available information size is "
                + mPictureIdList.size() + ", requested is " + mHighestRequest);
        if (mHighestRequest >= mPictureIdList.size()) { // Not enough
                                                        // information is
                                                        // present
            ReadUserPictures.fillUserPicturesList(mUserPicturesList);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mPictureTotalCount + 2) / 3;
        // Lets you scroll through
        // more items
        // than what's loaded. Actual number
        // is not very important
    }

    @Override
    public Object getItem(int position) {
        throw new UnsupportedOperationException(); // A row do not corresponding
                                                   // to a specific object
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (position * 3 >= (mPictureIdList.size() - LOAD_BEFORE_HAND_NUM)) {
            ReadUserPictures.fillUserPicturesList(mUserPicturesList);
        }
        PictureViewHolder holder;
        ViewGroup row = (ViewGroup) convertView;
        if (row == null) {
            row = (ViewGroup) View.inflate(mContext,
                    R.layout.user_profile_picture_row, null);
            for (int i = 0; i < 3; i++) {
                holder = new PictureViewHolder();
                mActiveIContentRequesters.add(holder);
                holder.imageView = (ImageView) row.getChildAt(i);
                holder.imageView.setTag(holder);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                holder = (PictureViewHolder) row.getChildAt(i).getTag();
                ContentHandler.getInstance().removeRequester(holder);
                holder.contentChanged(null);
            }

        }

        mImageView0 = (ImageView) row.getChildAt(0);
        mImageView1 = (ImageView) row.getChildAt(1);
        mImageView2 = (ImageView) row.getChildAt(2);

        mImageView0.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String picID = mPictureIdList.get(position * 3 + 0);
                if (picID != null) {
                    Picture pic = ReadPicture.requestPicture(picID, null);

                    PictureIOSession io = pic.getIOSession();
                    String ownerId;
                    try {
                        ownerId = io.getOwnerId();
                    } finally {
                        io.close();
                    }
                    startPictureView(ownerId, picID, mImageView0);
                }
                // Toast.makeText(mContext, "picId = " + picID,
                // Toast.LENGTH_SHORT)
                // .show();
            }
        });
        mImageView1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String picID = mPictureIdList.get(position * 3 + 1);
                if (picID != null) {
                    Picture pic = ReadPicture.requestPicture(picID, null);

                    PictureIOSession io = pic.getIOSession();
                    String ownerId;
                    try {
                        ownerId = io.getOwnerId();
                    } finally {
                        io.close();
                    }
                    startPictureView(ownerId, picID, mImageView1);
                }
                // Toast.makeText(mContext, "picId = " + picID,
                // Toast.LENGTH_SHORT)
                // .show();
            }
        });
        mImageView2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String picID = mPictureIdList.get(position * 3 + 2);
                if (picID != null) {
                    Picture pic = ReadPicture.requestPicture(picID, null);

                    PictureIOSession io = pic.getIOSession();
                    String ownerId;
                    try {
                        ownerId = io.getOwnerId();
                    } finally {
                        io.close();
                    }
                    startPictureView(ownerId, picID, mImageView2);
                }
                // Toast.makeText(mContext, "picId = " + picID,
                // Toast.LENGTH_SHORT)
                // .show();
            }
        });

        // for(int i = 0 ;i<3;i++) {
        // mTempItemImageView = (ImageView) row.getChildAt(i);
        // mImageViewIndex = i;
        //
        // mTempItemImageView.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        //
        // String picID = mPictureIdList.get(position * 3 + mImageViewIndex);
        // if (picID != null) {
        // Picture pic = ReadPicture.requestPicture(picID, null);
        //
        // PictureIOSession io = pic.getIOSession();
        // String ownerId;
        // try {
        // ownerId = io.getOwnerId();
        // } finally {
        // io.close();
        // }
        // startPictureView(ownerId,picID,mTempItemImageView);
        // }
        // Toast.makeText(mContext, "picId = "+picID,
        // Toast.LENGTH_SHORT).show();
        // }
        // });
        // }

        for (int i = 0; i < 3; i++) {
            holder = (PictureViewHolder) row.getChildAt(i).getTag();
            row.getChildAt(i).setVisibility(View.VISIBLE);
            if (position * 3 + i < mPictureIdList.size()) {
                String pictureId = mPictureIdList.get(position * 3 + i);
                if (pictureId != null) {
                    // Log.e(TAG, "Row " + position + ", col " + i +
                    // ", pictureId " + pictureId);
                    Picture picture = (Picture) ReadPicture.requestPicture(
                            pictureId, holder);
                    holder.contentChanged(picture); // Consistently update the
                                                    // UI
                } else {
                    row.getChildAt(i).setVisibility(View.INVISIBLE);
                    holder.contentChanged(null);
                }
            } else if (position * 3 + i < mPictureTotalCount) {
            } else {
                row.getChildAt(i).setVisibility(View.INVISIBLE);
                holder.contentChanged(null);
            }
        }
        return row;
    }

    private void startPictureView(String ownerId, String picID, View view) {
        Intent intent = new Intent(Actions.USER_PICTURES);
        intent.putExtra("userId", ownerId);
        intent.putExtra("pictureId", picID);
        Log.d(TAG, "Opened picture is " + picID);
        mContext.startActivity(intent);
    }

    public class PictureViewHolder implements IContentRequester {
        public ImageView imageView;

        @Override
        public void contentChanged(Content content) {
            P1Application.picasso.cancelRequest(imageView);
            if (content == null) {
                imageView.setImageDrawable(null);
                return;
            }

            PictureIOSession io = ((com.p1.mobile.p1android.content.Picture) content)
                    .getIOSession();
            try {
                P1Application.picasso
                        .load(Uri.parse(io
                                .getImageUrl(ImageFormat.IMAGE_SQUARE_180)))
                        .placeholder(null).into(imageView);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    }

    public void destroy() {
        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
    }

}
