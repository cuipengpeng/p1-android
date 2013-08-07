package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.NotificationList;
import com.p1.mobile.p1android.content.NotificationList.NotificationListIOSession;

public abstract class NotificationListParser {
    public static final String TAG = NotificationListParser.class
            .getSimpleName();

    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String STORIES = "stories";
    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";
    public static final String PAGINATION_TOTAL = "total";

    /**
     * 
     * @param json
     *            response json containing a list of json Stories and pagination
     * @param notificationList
     * @return true if the NotificationList has been changed
     */
    public static boolean appendToNotificationList(JsonObject json,
            NotificationList notificationList) {
        NotificationListIOSession io = notificationList.getIOSession();
        try {
            JsonObject pagination = json.getAsJsonObject(PAGINATION);
            int responseOffset = pagination.getAsJsonPrimitive(
                    PAGINATION_OFFSET).getAsInt();
            io.setPaginationTotal(json.getAsJsonObject(PAGINATION)
                    .getAsJsonPrimitive(PAGINATION_TOTAL).getAsInt());

            JsonArray jArr = json.getAsJsonObject(DATA).getAsJsonArray(STORIES);
            Iterator<JsonElement> iterator = jArr.iterator();
            if (responseOffset == 0) {
                io.getNotificationIdList().clear();
            }
            while (iterator.hasNext()) {
                io.getNotificationIdList()
                        .add(iterator.next().getAsJsonObject().get(ID)
                                .getAsString());
                io.incrementOffset();
            }

            Log.d(TAG, "Now have " + io.getNotificationIdList().size()
                    + " notifications");

            io.setValid(true);

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse");
            e.printStackTrace();
        } finally {
            io.close();
        }
        return true;
    }
}
