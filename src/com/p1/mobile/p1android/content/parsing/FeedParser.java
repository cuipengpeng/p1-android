package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Feed;
import com.p1.mobile.p1android.content.Feed.FeedIOSession;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;

public abstract class FeedParser {
    public static final String TAG = FeedParser.class.getSimpleName();

    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String SHARES = "shares";
    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";

    /**
     * 
     * @param json
     *            response json containing items from feed and pagination
     * @param feed
     * @return true if the Feed has been changed
     */
    public static boolean appendToFeed(JsonObject json, Feed feed) {
        FeedIOSession io = feed.getIOSession();
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

            if (io.isResetOnNextUpdate()) {
                io.reset();
            }

            JsonArray jArr = json.getAsJsonObject(DATA).getAsJsonArray(SHARES);
            Iterator<JsonElement> iterator = jArr.iterator();
            while (iterator.hasNext()) {
                io.incrementOffset();
                String shareId = iterator.next().getAsJsonObject().get(ID)
                        .getAsString();
                if (isShareValid(shareId)) {
                    io.getShareIdList().add(shareId);
                } else {
                    Log.w(TAG, "Decided not to add share " + shareId
                            + " as it's not valid.");
                }
            } // TODO Remove duplicates

            io.setValid(true);
        } finally {
            io.close();
        }
        return true;
    }

    /**
     * Currently a bit slow (1ms/call) as it fetches each share from the
     * ContentHandler
     * 
     * @param shareId
     * @return
     */
    private static boolean isShareValid(String shareId) {
        Share share = ContentHandler.getInstance().getShare(shareId, null);
        ShareIOSession io = share.getIOSession();
        try {
            return io.isValid();
        } finally {
            io.close();
        }
    }
}
