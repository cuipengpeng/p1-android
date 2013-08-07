package com.p1.mobile.p1android.content.parsing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class JsonFactory {
    private static final String TAG = JsonFactory.class.getSimpleName();

    private static final String EMAIL = "email";
    private static final String OLD = "old";
    private static final String NEW = "new";

    public static JsonObject createReadJson(String newestId) {
        JsonObject json = new JsonObject();
        json.addProperty(ConversationParser.READ, newestId);
        return json;
    }

    public static JsonObject createApplyJson(String givenName, String surname,
            String email) {
        JsonObject json = new JsonObject();
        json.addProperty(UserParser.GIVENNAME, givenName);
        json.addProperty(UserParser.SURNAME, surname);
        json.addProperty(EMAIL, email);
        json.addProperty("client_id", NetworkUtilities.APPLICATION_ID);
        json.addProperty("client_secret", NetworkUtilities.CODE);
        Log.d(TAG, "Created apply json: " + json.toString());
        return json;
    }

    public static JsonObject createChangePasswordJson(String oldPassword,
            String newPassword) {
        JsonObject json = new JsonObject();
        json.addProperty(OLD, oldPassword);
        json.addProperty(NEW, newPassword);
        Log.d(TAG, "Created apply json: " + json.toString());
        return json;
    }
}
