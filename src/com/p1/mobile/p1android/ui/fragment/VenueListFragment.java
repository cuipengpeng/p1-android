/**
 * VenueListFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Venue;
import com.p1.mobile.p1android.content.Venue.VenueIOSession;
import com.p1.mobile.p1android.content.VenueList;
import com.p1.mobile.p1android.content.VenueList.VenueListIOSession;
import com.p1.mobile.p1android.content.logic.ReadVenue;
import com.p1.mobile.p1android.ui.adapters.VenueListAdapter;

/**
 * @author Viktor Nyblom
 * 
 */
public class VenueListFragment extends ListFragment implements
        IContentRequester {
    private static final String TAG = VenueListFragment.class.getSimpleName();
    private static final String EMPTY_SEARCH_STRING = "";

    private double mLatitude;
    private double mLongitude;
    private boolean mHasActiveRequest = false;

    private VenueLocationsCallback mCallback;

    public interface VenueLocationsCallback {
        void onVenueSelected(String venueId);

        void addVenueLocations(List<BDLocation> locationList);
    }

    public static VenueListFragment newInstance() {
        VenueListFragment fragment = new VenueListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (VenueLocationsCallback) activity;
        Log.d(TAG, "onAttach ");
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
            long id) {
        String venueId = (String) listView.getAdapter().getItem(position);

        mCallback.onVenueSelected(venueId);
    }

    /**
     * 
     * @param searchString
     */
    public void fetchVenues(String searchString) {
        Log.d(TAG, "Fetching venues containing: \"" + searchString + "\"");
        Log.d(TAG, "Nr of requests pre cancel: ");
        if (mHasActiveRequest) {
            return;
        }

        if (searchString == null) {
            searchString = EMPTY_SEARCH_STRING;
        }

        ReadVenue.requestVenueList(searchString, mLatitude, mLongitude, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ContentHandler.getInstance().removeRequester(this);
    }

    @Override
    public void contentChanged(Content content) {
        if (!(content instanceof VenueList)) {
            return;
        }
        Log.d(TAG, "contentChanged");

        VenueListIOSession io = (VenueListIOSession) content.getIOSession();
        try {
            List<String> idList = io.getVenueIdList();
            final VenueListAdapter adapter = new VenueListAdapter(getActivity()
                    .getApplicationContext(), idList);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();

            updateMap(idList);
        } finally {
            io.close();
        }
    }

    private void updateMap(List<String> idList) {
        List<BDLocation> locationsList = new ArrayList<BDLocation>();
        for (String id : idList) {
            Venue venue = ReadVenue.requestVenue(id, null);
            BDLocation location = new BDLocation();
            VenueIOSession io = venue.getIOSession();
            try {
                location.setLatitude(io.getLatitude());
                location.setLongitude(io.getLongitude());
            } finally {
                io.close();
            }
            locationsList.add(location);
        }
        mCallback.addVenueLocations(locationsList);
    }

    public void setLocation(double longitude, double latitude) {
        mLatitude = latitude;
        mLongitude = longitude;

        fetchVenues(EMPTY_SEARCH_STRING);
    }

}