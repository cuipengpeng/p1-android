package com.p1.mobile.p1android.content.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.UserPicturesList;
import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;
import com.p1.mobile.p1android.content.UserPicturesList.UserPicturesListIOSession;

public abstract class UserPicturesListParser {
    public static final String TAG = UserPicturesListParser.class
            .getSimpleName();

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String PICTURES = "pictures";
    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";
    public static final String PAGINATION_TOTAL = "total";

    public static boolean appendToUserPicturesList(JsonObject json,
            UserPicturesList userPicturesList, RangePagination requestPagination) {
        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            List<String> newIds = new ArrayList<String>();

            JsonArray jArr = json.getAsJsonObject(DATA)
                    .getAsJsonArray(PICTURES);
            Iterator<JsonElement> iterator = jArr.iterator();
            while (iterator.hasNext()) {
                newIds.add(iterator.next().getAsJsonObject().get(ID)
                        .getAsString());
            }
            Log.d(TAG, "Filled " + newIds.size() + " new ids");
            io.fillIds(newIds, requestPagination);

            io.setValid(true);
        } finally {
            io.close();
        }
        return true;
    }

    public static boolean appendToUserPicturesList(JsonObject json,
            UserPicturesList userPicturesList) {
        UserPicturesListIOSession io = userPicturesList.getIOSession();
        try {
            JsonObject pagination = json.getAsJsonObject(PAGINATION);
            int responseOffset = pagination.getAsJsonPrimitive(
                    PAGINATION_OFFSET).getAsInt();
            if (responseOffset != io.getPaginationNextOffset()) {
                Log.e(TAG,
                        "Pagination offset is off! Returned offset is "
                                + responseOffset + ", should be "
                                + io.getPaginationNextOffset());
            }
            io.setPaginationTotal(pagination.getAsJsonPrimitive(
                    PAGINATION_TOTAL).getAsInt());

            List<String> newIds = new ArrayList<String>();

            JsonArray jArr = json.getAsJsonObject(DATA)
                    .getAsJsonArray(PICTURES);
            Iterator<JsonElement> iterator = jArr.iterator();
            while (iterator.hasNext()) {
                newIds.add(iterator.next().getAsJsonObject().get(ID)
                        .getAsString());
            }
            Log.d(TAG, "Filled " + newIds.size() + " new ids");
            io.addPaginatedIds(newIds);

            if (newIds.size() < io.getPaginationLimit()) {
                io.reportIncompleteNetworkResponse();
            }

            io.setValid(true);
        } finally {
            io.close();
        }
        return true;
    }
}
