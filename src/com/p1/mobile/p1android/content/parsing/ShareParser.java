package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;

public class ShareParser {
    public static final String TAG = ShareParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String CAPTION = "value"; // Caption is a more proper
                                                  // synonym
    public static final String PICTURES = "pictures";
    public static final String PICTURE = "picture";
    public static final String ID = "id";
    public static final String IDS = "ids";
    public static final String LIKES = "likes";
    public static final String TAGS = "tags";
    public static final String OWNER = "owner";
    public static final String VENUE = "venue";

    /**
     * 
     * @param json
     *            Share json object
     * @param share
     * @return if the target object was changed
     */
    public static boolean parseToShare(JsonObject json, Share share) {
        ShareIOSession io = share.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("share")) {
                Log.e(TAG, "Tried to unparse a json that is not a share: "
                        + json.toString());
            }
            GenericParser.parseToContent(json, io);
            if (json.has(CAPTION) && !json.get(CAPTION).isJsonNull()) {
                io.setCaption(json.get(CAPTION).getAsString());
            }
            if (json.has(VENUE) && !json.get(VENUE).isJsonNull()) {
                io.setVenueId(json.getAsJsonObject(VENUE).get(ID).getAsString());
            }
            io.setOwnerId(json.getAsJsonObject(OWNER).get(ID).getAsString());

            JsonArray pictureArr = json.getAsJsonArray(PICTURES);
            Iterator<JsonElement> iterator = pictureArr.iterator();
            List<String> pictureIdList = io.getPictureIds();
            pictureIdList.clear();
            while (iterator.hasNext() && pictureIdList.size() < 9) {
                pictureIdList.add(iterator.next().getAsJsonObject().get(ID)
                        .getAsString());
            }

            LikeParser.setLikes(io, json);

            if (json.has(TAGS) && !json.get(TAGS).isJsonNull()) {
                JsonArray tagArr = json.getAsJsonObject(TAGS).getAsJsonArray(
                        IDS);
                iterator = tagArr.iterator();
                List<String> tagIdList = io.getTagIds();
                tagIdList.clear();
                while (iterator.hasNext()) {
                    tagIdList.add(iterator.next().getAsString());
                }
            }

            CommentParser.setComments(io, json);

            io.updateValidity();

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
     * @param share
     * @return
     */
    public static JsonObject serializeShare(Share share) {
        ShareIOSession io = share.getIOSession();
        JsonObject json = new JsonObject();
        try {
            if (!io.isSinglePictureShare())
                json.addProperty(CAPTION, io.getCaption());
            // The API batch service requires Id's to be integers
            json.addProperty(ID, Integer.valueOf(io.getId()));
            json.addProperty(TYPE, io.getType());

            // The API does not want Picture ids
            // JsonArray pictureArray = new JsonArray();
            // for(String id : io.getPictureIds()){
            // JsonObject pictureIdJson = new JsonObject();
            // // The API batch service requires Id's to be integers
            // pictureIdJson.addProperty(ID, Integer.valueOf(id));
            // pictureIdJson.addProperty(TYPE, PICTURE);
            // pictureArray.add(pictureIdJson);
            // }
            // json.add(PICTURES, pictureArray);

            // JsonArray tagArray = new JsonArray();
            // for (String id : io.getTagIds()) {
            // tagArray.add(new JsonPrimitive(id));
            // }
            // json.add(TAGS, tagArray);
            Log.d(TAG, "Share json: " + json.toString());

            if (io.getVenueId() != null) {
                JsonObject venueJson = new JsonObject();
                venueJson.addProperty(ID, io.getVenueId());
                json.add(VENUE, venueJson);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }

}
