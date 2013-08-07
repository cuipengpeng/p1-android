package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Venue;
import com.p1.mobile.p1android.content.VenueList;
import com.p1.mobile.p1android.content.VenueList.VenueListIOSession;
import com.p1.mobile.p1android.content.parsing.VenueListParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadVenue {
    public static final String TAG = ReadVenue.class.getSimpleName();

    public static final double DISTANCE_THRESHHOLD = 0.005; // roughly 300m

    public static VenueList requestVenueList(String searchString,
            double latitude, double longitude, IContentRequester requester) {
        if (searchString == null) {
            throw new NullPointerException("searchString must be non-null");
        }

        VenueList venueList = ContentHandler.getInstance().getVenueList(
                searchString, requester);

        VenueListIOSession io = venueList.getIOSession();
        try {
            if (!io.isValid()
                    || (Math.abs(latitude - io.getLatitude()) < DISTANCE_THRESHHOLD && Math
                            .abs(longitude - io.getLongitude()) < DISTANCE_THRESHHOLD)) {
                io.setLatitude(latitude);
                io.setLongitude(longitude);
                io.setValid(false);

                fillVenueList(venueList);
            }

        } finally {
            io.close();
        }
        return venueList;
    }

    public static Venue requestVenue(String venueId, IContentRequester requester) {
        if (venueId == null) {
            throw new NullPointerException("venueId must be non-null");
        }

        return ContentHandler.getInstance().getVenue(venueId, requester);
    }

    private static void fillVenueList(final VenueList venueList) {

        boolean shouldMakeRequest;
        VenueListIOSession io = venueList.getIOSession();
        try {
            shouldMakeRequest = io.getLastAPIRequest() == 0;
            if (shouldMakeRequest) {
                io.refreshLastAPIRequest();
            }
        } finally {
            io.close();
        }

        if (shouldMakeRequest) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            String searchString = null;
                            double latitude, longitude;
                            VenueListIOSession io = venueList.getIOSession();
                            try {
                                searchString = io.getSearchString();
                                latitude = io.getLatitude();
                                longitude = io.getLongitude();
                            } finally {
                                io.close();
                            }
                            String venueRequest;

                            venueRequest = ReadContentUtil.netFactory
                                    .createGetVenuesRequest(searchString,
                                            latitude, longitude);
                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject object = network.makeGetRequest(
                                        venueRequest, null).getAsJsonObject();

                                JsonObject data = object
                                        .getAsJsonObject("data");
                                JsonArray venueArray = data
                                        .getAsJsonArray("venues");
                                ReadContentUtil.saveExtraVenues(venueArray);
                                VenueListParser.appendToVenueList(object,
                                        venueList);

                                venueList.notifyListeners();

                                Log.d(TAG,
                                        "All listeners notified as result of fillVenueList");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed getting venue list", e);
                            } finally {
                                io = venueList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                            }
                        }
                    });
        }
    }

}
