package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;

public class ConversationParser {
    public static final String TAG = ConversationParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String READ = "read";
    public static final String ETAG = "etag";
    public static final String ACTIVITY = "activity";
    public static final String MESSAGES = "messages";
    public static final String ID = "id";
    public static final String IDS = "ids";
    public static final String LATEST_TIME = "latest_time";
    public static final String OWNER = "owner";
    public static final String OTHER_USER = "other_user";

    public static final String PAGINATION = "pagination";
    public static final String PAGINATION_OFFSET = "offset";
    public static final String PAGINATION_TOTAL = "total";
    public static final String DATA = "data";

    /**
     * 
     * @param json
     *            Conversation json object
     * @param conversation
     * @return if the target object was changed
     */
    public static boolean parseToConversation(JsonObject json,
            Conversation conversation) {
        ConversationIOSession io = conversation.getIOSession();
        try {
            if (io.getUnfinishedUserModifications() > 0) {
                return false; // Memory representation is more valid than new
                              // json update
            }

            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("conversation")) {
                Log.e(TAG,
                        "Tried to unparse a json that is not a conversation: "
                                + json.toString());
            }
            io.setOwnerId(json.getAsJsonObject(OWNER).get(ID).getAsString());

            GenericParser.parseToContent(json, io);
            if (json.has(READ) && !json.get(READ).isJsonNull()) {
                io.setRead(json.get(READ).getAsBoolean());
            } else {
                io.setRead(false);
            }
            if (json.has(ETAG) && !json.get(ETAG).isJsonNull()) {
                io.setEtag(json.get(ETAG).getAsString());
            }
            if (json.has(LATEST_TIME) && !json.get(LATEST_TIME).isJsonNull()) {
                io.setLatestTime(GenericParser.parseAPITime(json.get(
                        LATEST_TIME).getAsString()));
            }

            JsonArray pictureArr = json.getAsJsonObject(MESSAGES)
                    .getAsJsonArray(IDS);
            Iterator<JsonElement> iterator = pictureArr.iterator();
            List<String> messageIdList = io.getMessageIdList();
            boolean firstMessage = true;
            while (iterator.hasNext()) {
                String newId = iterator.next().getAsString();
                boolean successfulInsertion = io.safelyAddMessageId(newId);
                if (firstMessage) {
                    firstMessage = false;
                    io.setNewestMessageId(newId);
                }
                if (!successfulInsertion) {
                    Log.d(TAG, "Message " + newId
                            + " is already in the conversation " + io.getId()
                            + " of " + io.getMessageIdList().size()
                            + " messages");
                }
            }
            Log.d(TAG, "Conversation with " + messageIdList.size() + "parsed");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return true;
    }

    /**
     * 
     * @param json
     *            response json containing a list of json Messages
     * @param conversation
     * @return true if the Conversation has been changed
     */
    public static boolean appendToConversation(JsonObject json,
            Conversation conversation) {
        ConversationIOSession io = conversation.getIOSession();
        try {
            // JsonObject pagination = json.getAsJsonObject(PAGINATION);
            // Ordinary pagination is not supported for conversations

            JsonArray jArr = json.getAsJsonObject(DATA)
                    .getAsJsonArray(MESSAGES);
            Iterator<JsonElement> iterator = jArr.iterator();

            int amountOfResponses = 0;
            while (iterator.hasNext()) {
                String newId = iterator
                        .next().getAsJsonObject().get(ID)
                        .getAsString();
                if (io.getNewestMessageId() == null) {
                    io.setNewestMessageId(newId);
                }
                Boolean successfulInsertion = io.safelyAddMessageId(newId);
                if (!successfulInsertion) {
                    Log.d(TAG, "Message " + newId
                            + " is already in the conversation " + io.getId()
                            + " of " + io.getMessageIdList().size()
                            + " messages");
                }
                amountOfResponses++;
            }
            if (amountOfResponses < io.getPaginationLimit()) {
                io.reportIncompleteNetworkResponse();
            }

        } finally {
            io.close();
        }
        return true;
    }

}
