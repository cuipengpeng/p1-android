package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.VenueList;
import com.p1.mobile.p1android.content.VenueList.VenueListIOSession;

public abstract class VenueListParser {
    public static final String TAG = VenueListParser.class.getSimpleName();

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String VENUES = "venues";

    /**
     * 
     * @param json
     *            response json
     * @param venueList
     * @return true if the VenueList has been changed
     */
    public static boolean appendToVenueList(JsonObject json, VenueList venueList) {
        VenueListIOSession io = venueList.getIOSession();
        try {

            JsonArray jArr = json.getAsJsonObject(DATA).getAsJsonArray(VENUES);
            io.getVenueIdList().clear();
            Iterator<JsonElement> iterator = jArr.iterator();
            while (iterator.hasNext()) {
                io.getVenueIdList()
                        .add(iterator.next().getAsJsonObject().get(ID)
                                .getAsString());
            }
            io.setValid(true);

        } finally {
            io.close();
        }
        return true;
    }
}
