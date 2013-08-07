package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 
 * @author Anton
 * 
 */
public class VenueList extends Content {
    public static final String TAG = VenueList.class.getSimpleName();
    public static final String TYPE = "venueList";

    // API information variables
    private List<String> idList = new ArrayList<String>();
    private double latitude;
    private double longitude;

    /**
     * Uses the search filter as id
     * 
     * @param filter
     */
    protected VenueList(String filter) {
        super(filter);

        IOSession = new VenueListIOSession();
        Log.d(TAG, "created");
    }

    @Override
    public VenueListIOSession getIOSession() {
        return (VenueListIOSession) super.getIOSession();
    }

    public class VenueListIOSession extends ContentIOSession {
        @Override
        public String getType() {
            return TYPE;
        }

        public String getSearchString() {
            // SearchString is used as Id
            return getId();
        }

        public List<String> getVenueIdList() {
            return idList;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            VenueList.this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            VenueList.this.longitude = longitude;
        }

    }

}
