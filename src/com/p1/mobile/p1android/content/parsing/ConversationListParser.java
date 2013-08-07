package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.ConversationList;
import com.p1.mobile.p1android.content.ConversationList.ConversationListIOSession;

public abstract class ConversationListParser {
    public static final String TAG = ConversationListParser.class
            .getSimpleName();

    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String CONVERSATIONS = "conversations";
    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";
    public static final String PAGINATION_TOTAL = "total";

    /**
     * 
     * @param json
     *            response json containing a list of json Conversations and
     *            pagination
     * @param conversationList
     * @return true if the ConversationList has been changed
     */
    public static boolean appendToConversationList(JsonObject json,
            ConversationList conversationList) {
        ConversationListIOSession io = conversationList.getIOSession();
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
            io.setPaginationTotal(json.getAsJsonObject(PAGINATION)
                    .getAsJsonPrimitive(PAGINATION_TOTAL).getAsInt());

            JsonArray jArr = json.getAsJsonObject(DATA).getAsJsonArray(
                    CONVERSATIONS);
            Iterator<JsonElement> iterator = jArr.iterator();

            io.getConversationIdList().clear(); // Always clear the list as
                                                // backend doesn't use
                                                // pagination for conversations

            while (iterator.hasNext()) {
                io.getConversationIdList()
                        .add(iterator.next().getAsJsonObject().get(ID)
                                .getAsString());
                io.incrementOffset();
            }
            if (io.getConversationIdList().size() < io.getPaginationLimit()) {
                io.reportIncompleteNetworkResponse();
            }

            io.setValid(true);
            io.clearLastAPIRequest();

        } finally {
            io.close();
        }
        return true;
    }
}
