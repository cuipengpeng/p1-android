package com.p1.mobile.p1android.content.parsing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Venue;
import com.p1.mobile.p1android.content.Venue.VenueIOSession;

public class VenueParser {
    public static final String TAG = VenueParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String ADDRESS = "address";
    public static final String CATEGORY = "category";
    public static final String NAME = "name";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    /**
     * 
     * @param json
     *            Venue json object
     * @param venue
     * @return if the target object was changed
     */
    public static boolean parseVenue(JsonObject json, Venue venue) {
        VenueIOSession io = venue.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("venue")) {
                Log.e(TAG, "Tried to unparse a json that is not a venue: "
                        + json.toString());
            }

            GenericParser.parseToContent(json, io);
            if (json.has(ADDRESS) && !json.get(ADDRESS).isJsonNull()) {
                io.setAddress(json.get(ADDRESS).getAsString());
            }
            if (json.has(CATEGORY) && !json.get(CATEGORY).isJsonNull()) {
                io.setCategory(json.get(CATEGORY).getAsString());
            }
            if (json.has(NAME) && !json.get(NAME).isJsonNull()) {
                io.setName(json.get(NAME).getAsString());
            }

            if (json.has(LATITUDE) && !json.get(LATITUDE).isJsonNull()) {
                io.setLatitude(json.get(LATITUDE).getAsDouble());
            }
            if (json.has(LONGITUDE) && !json.get(LONGITUDE).isJsonNull()) {
                io.setLongitude(json.get(LONGITUDE).getAsDouble());
            }
            
            io.setValid(true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return true;
    }

}
