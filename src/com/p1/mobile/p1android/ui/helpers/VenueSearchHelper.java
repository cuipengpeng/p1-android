package com.p1.mobile.p1android.ui.helpers;

import java.util.Timer;
import java.util.TimerTask;

import android.text.TextUtils;

import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.logic.ReadVenue;

public class VenueSearchHelper {

    private static final long SEARCH_DELAY_MILLIS = 1500;

    private IContentRequester mRequester;
    private String mSearchString;
    private double mLatitude;
    private double mLongitude;
    private Timer mTimer;

    public VenueSearchHelper(IContentRequester requester, double latitude,
            double longitude) {
        mRequester = requester;
        mLatitude = latitude;
        mLongitude = longitude;
        mTimer = new Timer();
    }

    public void updateLocation(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public void setNewSearchString(String string) {
        mTimer.cancel();
        mTimer = new Timer();
        mSearchString = string;
        mTimer.schedule(new DownloadTimerTask(), SEARCH_DELAY_MILLIS);
    }

    public void destroy() {
        mTimer.cancel();
        mTimer = null;
    }

    private class DownloadTimerTask extends TimerTask {

        @Override
        public void run() {
            if (!TextUtils.isEmpty(mSearchString.trim())) {
                ReadVenue.requestVenueList(mSearchString, mLatitude,
                        mLongitude, mRequester);
            }
        }

    }
}
