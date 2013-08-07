package com.p1.mobile.p1android.content.parsing;

import android.graphics.Point;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;

public class PictureParser {
    public static final String TAG = PictureParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String OWNER = "owner";
    public static final String ID = "id";
    public static final String CAPTION = "caption";
    public static final String SIZE = "size";

    private static final String IDS = "ids";
    private static final String TAGS = "tags";
    private static final String IMAGES = "images";
    private static final String IMAGE_314 = "314x314";
    private static final String IMAGE_180 = "180x180";
    private static final String IMAGE_154 = "154x154";
    private static final String IMAGE_980x145 = "980x145";
    private static final String IMAGE_W_720 = "width_720";
    private static final String IMAGE_W_640 = "width_640";
    private static final String IMAGE_W_480 = "width_480";
    private static final String URL = "url";

    /**
     * 
     * @param json
     *            Picture json object
     * @param user
     * @return if the target object was changed
     */
    public static boolean parseToPicture(JsonObject json, Picture picture) {
        PictureIOSession io = picture.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("picture")) {
                Log.e(TAG, "Tried to unparse a json that is not a picture: "
                        + json.toString());
            }
            GenericParser.parseToContent(json, io);

            io.setOwnerId(json.getAsJsonObject(OWNER).get(ID).getAsString());
            if (json.has(CAPTION)) {
                io.setCaption(json.get(CAPTION).getAsString());
            }

            JsonArray sizeArr = json.getAsJsonArray(SIZE); // API returns
                                                           // (width, height)
            io.setSize(new Point(sizeArr.get(0).getAsInt(), sizeArr.get(1)
                    .getAsInt()));

            CommentParser.setComments(io, json);
            setPictures(io, json);
            LikeParser.setLikes(io, json);
            setTags(io, json);

            io.setValid(true);
        } finally {
            io.close();
        }
        return true;
    }

    private static void setPictures(PictureIOSession io, JsonObject json) {

        if (json.has(IMAGES)) {
            JsonObject newJson = json.getAsJsonObject(IMAGES);

            if (newJson.has(IMAGE_314) && !newJson.get(IMAGE_314).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_SQUARE_314, newJson
                        .getAsJsonObject(IMAGE_314).get(URL).getAsString());
            }
            if (newJson.has(IMAGE_180) && !newJson.get(IMAGE_180).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_SQUARE_180, newJson
                        .getAsJsonObject(IMAGE_180).get(URL).getAsString());
            }
            if (newJson.has(IMAGE_154) && !newJson.get(IMAGE_154).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_SQUARE_154, newJson
                        .getAsJsonObject(IMAGE_154).get(URL).getAsString());
            }
            if (newJson.has(IMAGE_980x145)
                    && !newJson.get(IMAGE_980x145).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_980x145, newJson
                        .getAsJsonObject(IMAGE_980x145).get(URL).getAsString());
            }
            if (newJson.has(IMAGE_W_720)
                    && !newJson.get(IMAGE_W_720).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_WIDTH_720, newJson
                        .getAsJsonObject(IMAGE_W_720).get(URL).getAsString());
            }
            if (newJson.has(IMAGE_W_640)
                    && !newJson.get(IMAGE_W_640).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_WIDTH_640, newJson
                        .getAsJsonObject(IMAGE_W_640).get(URL).getAsString());
            }
            if (newJson.has(IMAGE_W_480)
                    && !newJson.get(IMAGE_W_480).isJsonNull()) {
                io.setImageUrl(ImageFormat.IMAGE_WIDTH_480, newJson
                        .getAsJsonObject(IMAGE_W_480).get(URL).getAsString());
            }
        }
    }

    private static void setTags(PictureIOSession io, JsonObject json) {
        if (json.has(TAGS)) {
            JsonObject newJson = json.getAsJsonObject(TAGS);
            JsonArray array = newJson.get(IDS).getAsJsonArray();
            if (!array.isJsonNull() && array.size() != 0) {
                String id;
                for (int i = 0; i < array.size(); ++i) {
                    id = array.get(i).getAsString();
                    io.getTags().add(id);
                }
            }
        }
    }

    /**
     * Only serializes the parts that the API needs
     * 
     * @param picture
     * @return
     */
    public static JsonObject serializePicture(Picture picture) {
        PictureIOSession io = picture.getIOSession();
        JsonObject json = new JsonObject();
        try {

            json.addProperty(CAPTION, io.getCaption());

            // The API batch service requires Id's to be integers
            json.addProperty(ID, Integer.valueOf(io.getId()));
            json.addProperty(TYPE, io.getType());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }
}
