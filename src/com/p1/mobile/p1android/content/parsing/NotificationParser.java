package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.NotificationStory;
import com.p1.mobile.p1android.content.NotificationStory.NotificationIOSession;

public class NotificationParser {
    public static final String TAG = NotificationParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String READ = "read";
    public static final String ID = "id";
    public static final String INTERACTION = "interaction";
    public static final String RELEVANCE = "relevance";
    public static final String STORY_TYPE = "story_type";
    public static final String USERS = "users";

    /**
     * 
     * @param json
     *            Notification json object
     * @param notification
     * @return if the target object was changed
     */
    public static boolean parseToNotification(JsonObject json,
            NotificationStory notification) {
        NotificationIOSession io = notification.getIOSession();
        try {
            if (io.getUnfinishedUserModifications() > 0) { // Should not be
                                                           // overwritten by new
                                                           // json information
                return false;
            }

            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("story")) {
                Log.e(TAG, "Tried to unparse a json that is not a story: "
                        + json.toString());
            }

            GenericParser.parseToContent(json, io);
            if (json.has(READ) && !json.get(READ).isJsonNull()) {
                io.setRead(json.get(READ).getAsBoolean());
            }

            if (json.has(RELEVANCE) && !json.get(RELEVANCE).isJsonNull()) {
                io.setRelevance(NotificationStory.Relevance.getEnum(json.get(
                        RELEVANCE).getAsString()));
			}
            if (json.has(STORY_TYPE) && !json.get(STORY_TYPE).isJsonNull()) {
                io.setStoryType(NotificationStory.StoryType.getEnum(json.get(
                        STORY_TYPE).getAsString()));
            }
            
            // Assumes the interaction is ordered
            JsonArray interactionArr = json.getAsJsonArray(INTERACTION);
            Iterator<JsonElement> iterator = interactionArr.iterator();
            io.setCreatedObject(GenericParser.parseIdType(iterator.next()
                    .getAsJsonObject()));
            io.setLinkedObject(GenericParser.parseIdType(iterator.next()
                    .getAsJsonObject()));
            if (iterator.hasNext()) {
                io.setTopLevelObject(GenericParser.parseIdType(iterator.next()
                        .getAsJsonObject()));
            } else {
                io.setTopLevelObject(io.getLinkedObject());
            }

            JsonArray usersArr = json.getAsJsonArray(USERS);
            iterator = usersArr.iterator();
            io.getRelatedUserIds().clear();
            while (iterator.hasNext()) {
                io.getRelatedUserIds().add(iterator.next()
                        .getAsString());
            }

            io.setValid(true);
            io.clearLastAPIRequest();

        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to map to enum");
            iae.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse");
            e.printStackTrace();
        } finally {
            io.close();
        }
        return true;
    }

}
