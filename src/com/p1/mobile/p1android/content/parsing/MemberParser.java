package com.p1.mobile.p1android.content.parsing;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Member;
import com.p1.mobile.p1android.content.Member.MemberIOSession;

public class MemberParser {
    public static final String TAG = MemberParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String ACTIVITY = "activity";
    public static final String PICTURES = "pictures";
    public static final String ID = "id";

    /**
     * 
     * @param json
     *            Member json object
     * @param member
     * @return if the target object was changed
     */
    public static boolean parseToMember(JsonObject json, Member member) {
        MemberIOSession io = member.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals("member")) {
                Log.e(TAG, "Tried to unparse a json that is not a member: "
                        + json.toString());
            }
            GenericParser.parseToContent(json, io);
            if (json.has(ACTIVITY) && !json.get(ACTIVITY).isJsonNull()) {
                io.setLatestActivity(GenericParser.parseAPITime(json.get(
                        ACTIVITY).getAsString()));
            } else {
                io.setLatestActivity(null);
            }

            JsonArray pictureArr = json.getAsJsonArray(PICTURES);
            Iterator<JsonElement> iterator = pictureArr.iterator();
            List<String> pictureIdList = io.getPictureIds();
            pictureIdList.clear();
            while (iterator.hasNext()) { // Add all pictures without regard to
                                         // amount, they will be trimmed to 0, 4
                                         // or 8 when requested
                pictureIdList.add(iterator.next().getAsJsonObject().get(ID)
                        .getAsString());
            }
            io.setValid(true);

        } finally {
            io.close();
        }
        return true;
    }

}
