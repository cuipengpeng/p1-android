package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.BrowseList;
import com.p1.mobile.p1android.content.BrowseList.BrowseListIOSession;

public abstract class BrowseListParser {
    public static final String TAG = BrowseListParser.class.getSimpleName();

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String PICTURES = "pictures";
    public static final String MEMBERS = "members";
    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";

    /**
     * 
     * @param json
     *            response json containing items from browse and pagination
     * @param browseList
     * @return true if the BrowseList has been changed
     */
    public static boolean appendToBrowseList(JsonObject json,
            BrowseList browseList, BrowseFilter filterOfNewData) {
        BrowseListIOSession io = browseList.getIOSession();
        try {
            if (io.matchExpectedFilter(filterOfNewData)) {
                JsonObject pagination = json.getAsJsonObject(PAGINATION);
                int responseOffset = pagination.getAsJsonPrimitive(
                        PAGINATION_OFFSET).getAsInt();
                if (responseOffset != io.getPaginationNextOffset()) {
                    Log.e(TAG,
                            "Pagination offset is off! Returned offset is "
                                    + responseOffset + ", should be "
                                    + io.getPaginationNextOffset());
                }

                if (io.isResetOnNextUpdate()) {
                    io.reset();
                }

                String relevantContentType = PICTURES;
                if (io.isMemberBrowse()) {
                    relevantContentType = MEMBERS;
                }
                JsonArray jArr = json.getAsJsonObject(DATA).getAsJsonArray(
                        relevantContentType);
                Iterator<JsonElement> iterator = jArr.iterator();
                int amountOfResponses = 0;
                while (iterator.hasNext()) {
                    String newId = iterator.next().getAsJsonObject().get(ID)
                            .getAsString();
                    io.addId(newId);
                    io.incrementOffset();
                    amountOfResponses++;
                }
                if (amountOfResponses < io.getPaginationLimit()) {
                    io.reportIncompleteNetworkResponse();
                }

                io.setValid(true);
            } else {
                Log.w(TAG,
                        "Decided not to parse data that did not match expected filter");
            }
        } finally {
            io.close();
        }
        return true;
    }
}
