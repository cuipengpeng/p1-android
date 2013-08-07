package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.LikeableIOSession;

public class LikeParser {
    public static final String TAG = LikeParser.class.getSimpleName();

    public static final String ID = "id";
    public static final String LIKES = "likes";
    public static final String DATA = "data";
    public static final String COMMENTS = "comments";

    private static final String COUNT = "count";
    private static final String IDS = "ids";
    private static final String HAS_LIKED = "has_liked";


    public static void appendToLikeable(JsonObject likesJson,
            Content likeableContent, boolean clearList) {
        LikeableIOSession io = (LikeableIOSession) likeableContent
                .getIOSession();
        try{
            if(clearList){
                io.getLikeUserIds().clear();
            }
            JsonArray jArr = likesJson.getAsJsonObject(DATA).getAsJsonArray(
                    LIKES);
            Iterator<JsonElement> iterator = jArr.iterator();
            while (iterator.hasNext()) {
                String likeId = iterator.next().getAsJsonObject().get(ID)
                        .getAsString();
                io.getLikeUserIds().add(likeId);
            } // TODO Remove duplicates
        } finally {
            io.close();
        }
    }

    protected static void setLikes(LikeableIOSession io, JsonObject json) {
        if (json.has(LIKES) && !json.get(LIKES).isJsonNull()) {
            JsonObject likesJson = json.getAsJsonObject(LIKES);
            io.setHasLiked(likesJson.get(HAS_LIKED).getAsBoolean());
            io.setTotalLikes(likesJson.get(COUNT).getAsInt());

            if (io.getLikeUserIds().size() == 0) {
                JsonArray array = likesJson.get(IDS).getAsJsonArray();
                if (!array.isJsonNull() && array.size() != 0) {
                    String id;
                    for (int i = 0; i < array.size(); ++i) {
                        id = array.get(i).getAsString();
                        io.getLikeUserIds().add(id);
                    }
                }
            } else {
                JsonArray array = likesJson.get(IDS).getAsJsonArray();
                if (!array.isJsonNull() && array.size() != 0) {
                    String id;
                    for (int i = array.size() - 1; i >= 0; i--) {
                        id = array.get(i).getAsString();
                        if (!io.getLikeUserIds().contains(id)) {
                            io.getLikeUserIds().add(0, id);
                        }

                    }
                }

            }

        }
    }

}
