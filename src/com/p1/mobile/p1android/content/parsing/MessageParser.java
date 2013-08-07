package com.p1.mobile.p1android.content.parsing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Message;
import com.p1.mobile.p1android.content.Message.MessageIOSession;

public class MessageParser {
    public static final String TAG = MessageParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String OWNER = "owner";
    public static final String VALUE = "value";

    /**
     * 
     * @param json
     *            Message json object
     * @param message
     * @return if the target object was changed
     */
    public static boolean parseMessage(JsonObject json, Message message) {
        MessageIOSession io = message.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("message")) {
                Log.e(TAG, "Tried to unparse a json that is not a message: "
                        + json.toString());
            }

            GenericParser.parseToContent(json, io);
            if (json.has(VALUE) && !json.get(VALUE).isJsonNull()) {
                io.setValue(json.get(VALUE).getAsString());
            }

			if (json.has(OWNER) && !json.get(OWNER).isJsonNull()) {
				io.setOwnerId(json.get(OWNER).getAsJsonObject().get(ID).getAsString());
			}
            io.setValid(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return true;
    }

    /**
     * Only serializes the parts that the API needs
     * 
     * @param message
     * @return
     */
    public static JsonObject serializeMessage(Message message) {
        MessageIOSession io = message.getIOSession();
        JsonObject json = new JsonObject();
        try {

            json.addProperty(VALUE, io.getValue());
            Log.d(TAG, "Message json: " + json.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }

}
