package com.p1.mobile.p1android.content.parsing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;

public class AccountParser {
    public static final String TAG = AccountParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String ETAG = "etag";
    private static final String SETTINGS = "settings";
    private static final String LANGUAGE = "language";
    private static final String EMAIL = "email";
    private static final String INVISIBLE = "invisible";
    private static final String WELCOME_SCREEN_VERSION = "welcome_screen_version";
    private static final String UNREAD_SUMMARY = "unread_summary";
    private static final String NOTIFICATIONS = "notifications";
    private static final String MESSAGES = "messages";

    /**
     * 
     * @param json
     * @param account
     * @return true if the account object has been changed
     */
    public static boolean parseToAccount(JsonObject json, Account account) {
        AccountIOSession io = account.getIOSession();
        boolean hasChanged = false;
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals(io.getType())) {
                Log.e(TAG, "Tried to unparse a json that is not an account: "
                        + json.toString());
            }
            if (json.has(ETAG) && !json.get(ETAG).isJsonNull()) {
                String etag = json.getAsJsonPrimitive(ETAG).getAsString();
                io.setEtag(etag);
            }

            JsonObject settingsJson = json.getAsJsonObject(SETTINGS);
            io.setLanguage(settingsJson.getAsJsonPrimitive(LANGUAGE)
                    .getAsString());
            io.setEmail(settingsJson.getAsJsonPrimitive(EMAIL).getAsString());
            io.setInvisible(settingsJson.getAsJsonPrimitive(INVISIBLE)
                    .getAsBoolean());
            io.setWelcomeScreenVersion(settingsJson.getAsJsonPrimitive(
                    WELCOME_SCREEN_VERSION).getAsInt());

            JsonObject unreadJson = json.getAsJsonObject(UNREAD_SUMMARY);
            int unreadNotifications = unreadJson.getAsJsonPrimitive(
                    NOTIFICATIONS).getAsInt();
            if (unreadNotifications != io.getUnreadNotifications())
                hasChanged = true;
            io.setUnreadNotifications(unreadNotifications);
            int unreadMessages = unreadJson.getAsJsonPrimitive(MESSAGES)
                    .getAsInt();
            if (unreadMessages != io.getUnreadMessages())
                hasChanged = true;
            io.setUnreadMessages(unreadMessages);
            if (!io.isValid())
                hasChanged = true;
            io.setValid(true);

        } finally {
            io.closeAfterModification();
        }
        return hasChanged;
    }

    /**
     * Only serializes the parts that the API needs
     * 
     * @param account
     * @return
     */
    public static JsonObject serializeAccount(Account account) {
        AccountIOSession io = account.getIOSession();
        JsonObject json = new JsonObject();
        try {
            JsonObject settings = new JsonObject();
            settings.addProperty(INVISIBLE, io.isInvisible());
            settings.addProperty(EMAIL, io.getEmail());
            settings.addProperty(WELCOME_SCREEN_VERSION,
                    io.getWelcomeScreenVersion());
            json.add(SETTINGS, settings);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }

}
