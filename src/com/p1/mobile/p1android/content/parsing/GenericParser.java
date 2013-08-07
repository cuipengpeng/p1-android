package com.p1.mobile.p1android.content.parsing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.IdTypePair;
import com.p1.mobile.p1android.content.IdTypePair.Type;

public abstract class GenericParser {
    public static final String TAG = GenericParser.class.getSimpleName();

    public static final String ID = "id";
    public static final String ETAG = "etag";
    public static final String TYPE = "type";
    public static final String CREATED_TIME = "created_time";

    public static SimpleDateFormat timeFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.US);

    /**
     * Will help parsing all common Content variables - id, etag and
     * created_time. Only for internal parser use
     * 
     * @param json
     * @param io
     *            An open io session.
     * @return if the Content was modified
     */
    protected static boolean parseToContent(JsonObject json, ContentIOSession io) {
        String id = json.getAsJsonPrimitive(ID).getAsString();
        if (!id.equals(io.getId())) {
            Log.w(TAG,
                    "Content " + io.getType() + " changed id from "
                            + io.getId() + " to " + id);
            io.setId(id);
        }

        if (json.has(ETAG) && !json.get(ETAG).isJsonNull()) {
            String etag = json.getAsJsonPrimitive(ETAG).getAsString();
            io.setEtag(etag);
        }
        if (json.has(CREATED_TIME) && !json.get(CREATED_TIME).isJsonNull()) {
            String createdTime = json.getAsJsonPrimitive(CREATED_TIME)
                    .getAsString();

            io.setCreatedTime(parseAPITime(createdTime));

        }

        return true;
    }

    public static IdTypePair parseIdType(JsonObject json) {
        String id = json.getAsJsonPrimitive(ID).getAsString();
        Type type = IdTypePair.Type.getEnum(json.getAsJsonPrimitive(TYPE)
                .getAsString());
        if (id != null && type != null)
            return new IdTypePair(id, type);
        else {
            Log.e(TAG, "Invalid parse arguments for idTypePair: "
                    + json.getAsJsonPrimitive(TYPE).getAsString() + " : " + id);
            return null;
        }
    }

    /**
     * Use for exact time in the format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
     * 
     * @param time
     * @return
     */
    public static Date parseAPITime(String time) {
        try {
            return timeFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a string representation of the Date which conforms with the API
     * 
     * @param time
     * @return
     */
    public static String formatAPITime(Date time) {
        return timeFormat.format(time);
    }

    /**
     * Use for day precision, such as birthdates in the format "yyyy-MM-dd"
     * 
     * @param date
     * @return
     */
    public static Date parseAPIDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
