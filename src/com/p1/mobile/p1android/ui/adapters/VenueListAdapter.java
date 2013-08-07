/**
 * VenueListAdapter.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Venue;
import com.p1.mobile.p1android.content.Venue.VenueIOSession;
import com.p1.mobile.p1android.content.logic.ReadVenue;

/**
 * @author Viktor Nyblom
 * 
 */
public class VenueListAdapter extends BaseAdapter {
    public static final String TAG = VenueListAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private List<String> mVenueIdList;

    public VenueListAdapter(Context context, List<String> venueIdList) {
        mVenueIdList = venueIdList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setVenueIdList(List<String> venueIdList) {
        Log.d(TAG, "setVenueIdList size " + venueIdList.size());
        mVenueIdList = venueIdList;
    }

    @Override
    public int getCount() {
        if (mVenueIdList == null) {
            Log.d(TAG, "getCount 0");
            return 0;
        }
        return mVenueIdList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mVenueIdList == null) {
            Log.d(TAG, "getItem " + position + " null");
            return null;
        }

        return mVenueIdList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            row = mLayoutInflater
                    .inflate(R.layout.venue_list_item, root, false);
            holder = new ViewHolder();
            holder.titleText = (TextView) row.findViewById(R.id.venueTitleText);
            holder.descText = (TextView) row.findViewById(R.id.venueDescText);
            row.setTag(holder);
        }

        holder = (ViewHolder) row.getTag();

        Venue venue = ReadVenue
                .requestVenue(mVenueIdList.get(position), holder);
        holder.contentChanged(venue);

        return row;
    }

    private class ViewHolder implements IContentRequester {

        TextView titleText;
        TextView descText;

        @Override
        public void contentChanged(Content content) {
            if (!(content instanceof Venue)) {
                return;
            }
            Log.d(TAG, "Holder content changed");
            VenueIOSession io = (VenueIOSession) content.getIOSession();
            try {
                Log.d(TAG, "Venue title " + io.getName());
                titleText.setText(io.getName());
                descText.setText(io.getCategory());
            } finally {
                io.close();
            }
        }
    }
}