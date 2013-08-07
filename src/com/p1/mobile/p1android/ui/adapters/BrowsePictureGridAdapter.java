package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.BrowseList;
import com.p1.mobile.p1android.content.BrowseList.BrowseListIOSession;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.logic.ReadBrowse;
import com.p1.mobile.p1android.content.logic.ReadPicture;

public class BrowsePictureGridAdapter extends BaseAdapter {

    @Override
    public int getItemViewType(int position) {
        if (position < 3)
            return 0;
        else
            return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public static final String TAG = BrowsePictureGridAdapter.class
            .getSimpleName();
    /** Load more data before getting the last one */
    private static final int LOAD_BEFORE_HAND_NUM = 18;
    private static final int DEFAULT_SCROLL_BELOW_ALLOWENCE = 30;

    private int scrollBelowAllowence = DEFAULT_SCROLL_BELOW_ALLOWENCE;
    private Context mContext;
    private BrowseList mBrowsePicturesList;
    private BrowseFilter mFilter;
    private List<String> mPictuerIdList = new ArrayList<String>();
    private int mHighestRequest = 0;
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    private IContentRequester mRequester;

    public BrowsePictureGridAdapter(BrowseList list, BrowseFilter filter,
            Context context, IContentRequester requester) {
        super();

        this.mContext = context;
        this.mBrowsePicturesList = list;
        this.mFilter = filter;
        this.mRequester = requester;
    }

    @Override
    public void notifyDataSetChanged() {
        BrowseListIOSession io = this.mBrowsePicturesList.getIOSession();
        try {
            mPictuerIdList.clear();
            mPictuerIdList.addAll(io.getIdList());
            if (io.hasMore()) {
                scrollBelowAllowence = DEFAULT_SCROLL_BELOW_ALLOWENCE;
            } else {
                scrollBelowAllowence = 0;
            }
        } finally {
            io.close();
        }

        Log.d(TAG, "Dataset changed. Available information size is "
                + mPictuerIdList.size() + ", requested is " + mHighestRequest);
        if (mHighestRequest >= mPictuerIdList.size()) { // Not enough
                                                        // information is
                                                        // present
            ReadBrowse.requestBrowsePicturesList(mRequester, mFilter);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPictuerIdList.size() + scrollBelowAllowence;
    }

    @Override
    public Object getItem(int position) {
        //
        // first three grid are empty
        if (position < 3) {
            return null;
        }
        position = position - 3;

        if (position > mHighestRequest) {
            mHighestRequest = position;
            Log.d(TAG, "Highest request is " + mHighestRequest);
        }

        if (position % 12 == 0) {
            Log.d(TAG, P1Application.picasso.getSnapshot().toString());
        }
        if (position >= (mPictuerIdList.size() - LOAD_BEFORE_HAND_NUM)) {
            ReadBrowse.requestBrowsePicturesList(mRequester, mFilter);
        }

        if (position >= mPictuerIdList.size()) {
            return null;
        }

        return mPictuerIdList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /** Place three transparent grid as the gridview header */
        if (position < 3) {
            if (convertView == null) {
                convertView = new View(mContext);
                convertView.setVisibility(View.INVISIBLE);
                convertView.setEnabled(false);
                convertView.setClickable(false);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        mContext.getResources().getDimensionPixelSize(
                                R.dimen.actionbar_and_auto_hide_height_padding));
                convertView.setLayoutParams(param);
            }
        } else {
            PictureViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mContext,
                        R.layout.browse_picture_fragment_grid_item, null);
                holder = new PictureViewHolder();
                mActiveIContentRequesters.add(holder);
                holder.imageView = (ImageView) convertView;
                convertView.setTag(holder);
            } else {
                holder = (PictureViewHolder) convertView.getTag();
                ContentHandler.getInstance().removeRequester(holder);
            }
            String pictureId = (String) getItem(position);
            if (pictureId != null) {
                Picture picture = (Picture) ReadPicture.requestPicture(
                        pictureId, holder);
                holder.contentChanged(picture); // Consistently update the UI
            } else {
                holder.contentChanged(null);
            }
        }

        return convertView;
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
