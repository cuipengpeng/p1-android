package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.Comment.CommentIOSession;
import com.p1.mobile.p1android.content.CommentableIOSession;
import com.p1.mobile.p1android.content.Content;

public class CommentParser {
    public static final String TAG = CommentParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String VALUE = "value"; // More proper synonym
    public static final String PARENT = "parent";
    public static final String ID = "id";
    public static final String IDS = "ids";
    private static final String COUNT = "count";
    public static final String LIKES = "likes";
    public static final String TAGS = "tags";
    public static final String HAS_LIKED = "has_liked";
    public static final String OWNER = "owner";
    public static final String DATA = "data";
    public static final String COMMENTS = "comments";

    /**
     * 
     * @param json
     *            Comment json object
     * @param comment
     * @return if the target object was changed
     */
    public static boolean parseToComment(JsonObject json, Comment comment) {
        CommentIOSession io = comment.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("comment")) {
                Log.e(TAG, "Tried to unparse a json that is not a share: "
                        + json.toString());
            }
            GenericParser.parseToContent(json, io);
            if (json.has(VALUE) && !json.get(VALUE).isJsonNull()) {
                io.setValue(json.get(VALUE).getAsString());
            } else {
                io.setValue("");
            }


            io.setOwnerId(json.getAsJsonObject(OWNER).get(ID).getAsString());
            io.setParent(GenericParser.parseIdType(json.getAsJsonObject(PARENT)));

            LikeParser.setLikes(io, json);

            JsonArray tagArr = json.getAsJsonObject(TAGS).getAsJsonArray(IDS);
            Iterator<JsonElement> iterator = tagArr.iterator();
            List<String> tagIdList = io.getTagIdList();
            tagIdList.clear();
            while (iterator.hasNext()) {
                tagIdList.add(iterator.next().getAsString());
            }


            io.setValid(true);

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse");
            e.printStackTrace();
        } finally {
            io.close();
        }
        return true;
    }

    /**
     * Only serializes the parts that the API needs
     * 
     * @param comment
     * @return
     */
    public static JsonObject serializeComment(Comment comment) {
        CommentIOSession io = comment.getIOSession();
        JsonObject json = new JsonObject();
        try {

            json.addProperty(VALUE, io.getValue());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }

    protected static void setComments(CommentableIOSession io, JsonObject json) {
        if (json.has(COMMENTS) && !json.get(COMMENTS).isJsonNull()) {
            JsonObject commentsJson = json.getAsJsonObject(COMMENTS);
            io.setTotalComments(commentsJson.get(COUNT).getAsInt());

            if (io.getCommentIds().size() == 0) {
                JsonArray array = commentsJson.get(IDS).getAsJsonArray();
                if (!array.isJsonNull() && array.size() != 0) {
                    String id;
                    for (int i = 0; i < array.size(); ++i) {
                        id = array.get(i).getAsString();
                        io.getCommentIds().add(id);
                    }
                }
            } else {
                JsonArray array = commentsJson.get(IDS).getAsJsonArray();
                if (!array.isJsonNull() && array.size() != 0) {
                    String id;
                    for (int i = array.size() - 1; i >= 0; i--) {
                        id = array.get(i).getAsString();
                        if (!io.getCommentIds().contains(id)) {
                            io.getCommentIds().add(0, id);
                        }

                    }
                }

            }

        }
    }

    public static void appendToCommentable(JsonObject commentsJson, Content commentableContent, boolean clearList) {
        CommentableIOSession io = (CommentableIOSession) commentableContent
                .getIOSession();
        try{
            if(clearList){
                io.getCommentIds().clear();
            }
            JsonArray jArr = commentsJson.getAsJsonObject(DATA).getAsJsonArray(COMMENTS);
            Iterator<JsonElement> iterator = jArr.iterator();
            while (iterator.hasNext()) {
                String commentId = iterator.next().getAsJsonObject().get(ID)
                        .getAsString();
                io.getCommentIds().add(commentId);
            } // TODO Remove duplicates
            if (io.getCommentIds().size() > io.getTotalComments()) {
                Log.w(TAG,
                        "Detected too many comments! "
                                + io.getCommentIds().size() + "/"
                                + io.getTotalComments());
            }
        } finally {
            io.close();
        }
    }

}
