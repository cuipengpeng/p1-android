package com.p1.mobile.p1android.content.parsing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Profile.BloodType;
import com.p1.mobile.p1android.content.Profile.MaritalStatus;
import com.p1.mobile.p1android.content.Profile.ProfileIOSession;
import com.p1.mobile.p1android.content.Profile.Zodiac;

public class ProfileParser {
    public static final String TAG = ProfileParser.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String OWNER = "owner";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    private static final String BLOODTYPE = "bloodtype";
    private static final String ZODIAC = "zodiac";
    private static final String MARITAL_STATUS = "marital";

    // Zodiac keys
    private static final String CAPRICORN = "capricorn";
    private static final String SAGITTARIUS = "sagittarius";
    private static final String SCORPIO = "scorpio";
    private static final String LIBRA = "libra";
    private static final String VIRGO = "virgo";
    private static final String LEO = "leo";
    private static final String CANCER = "cancer";
    private static final String GEMINI = "gemini";
    private static final String TAURUS = "taurus";
    private static final String ARIES = "aries";
    private static final String PISCES = "pisces";
    private static final String AQUARIUS = "aquarius";

    // Blood type keys
    private static final String BLOOD_O = "O";
    private static final String BLOOD_A = "A";
    private static final String BLOOD_B = "B";
    private static final String BLOOD_AB = "AB";

    // Marital status keys
    private static final String SINGLE = "single";
    private static final String IN_RELATIONSHIP = "in-relationship";
    private static final String ENGAGED = "engaged";
    private static final String MARRIED = "married";
    private static final String COMPLICATED = "it-complicated";
    private static final String OTHER = "other";

    public static boolean parseToProfile(JsonObject json, Profile profile) {
        ProfileIOSession io = profile.getIOSession();
        try {
            if (!json.has(TYPE)
                    || !json.get(TYPE).getAsString().equals(io.getType())) {
                Log.e(TAG, "Tried to unparse a json that is not a profile: "
                        + json.toString());
            }

            GenericParser.parseToContent(json, io);

            if (json.has(DESCRIPTION)) {
                io.setDescription(json.get(DESCRIPTION).getAsString());
            }

            setBloodType(io, json);
            setMaritalStatus(io, json);
            setZodiac(io, json);
        } finally {
            io.close();
        }
        return true;
    }

    private static void setZodiac(ProfileIOSession io, JsonObject json) {
        if (json.has(ZODIAC)) {
            String keyString = json.get(ZODIAC).getAsString();

            if (keyString.equalsIgnoreCase(CAPRICORN)) {
                io.setZodiac(Zodiac.CAPRICORNUS);
                return;
            }
            if (keyString.equalsIgnoreCase(SAGITTARIUS)) {
                io.setZodiac(Zodiac.SAGITTARUS);
                return;
            }
            if (keyString.equalsIgnoreCase(SCORPIO)) {
                io.setZodiac(Zodiac.SCORPIO);
                return;
            }
            if (keyString.equalsIgnoreCase(LIBRA)) {
                io.setZodiac(Zodiac.LIBRA);
                return;
            }
            if (keyString.equalsIgnoreCase(VIRGO)) {
                io.setZodiac(Zodiac.VIRGO);
                return;
            }
            if (keyString.equalsIgnoreCase(LEO)) {
                io.setZodiac(Zodiac.LEO);
                return;
            }
            if (keyString.equalsIgnoreCase(CANCER)) {
                io.setZodiac(Zodiac.CANCER);
                return;
            }
            if (keyString.equalsIgnoreCase(GEMINI)) {
                io.setZodiac(Zodiac.GEMINI);
                return;
            }
            if (keyString.equalsIgnoreCase(TAURUS)) {
                io.setZodiac(Zodiac.TAURUS);
                return;
            }
            if (keyString.equalsIgnoreCase(ARIES)) {
                io.setZodiac(Zodiac.ARIES);
                return;
            }
            if (keyString.equalsIgnoreCase(PISCES)) {
                io.setZodiac(Zodiac.PISCES);
                return;
            }
            if (keyString.equalsIgnoreCase(AQUARIUS)) {
                io.setZodiac(Zodiac.AQUARIUS);
                return;
            }
        }
    }

    private static void setMaritalStatus(ProfileIOSession io, JsonObject json) {
        if (json.has(MARITAL_STATUS)) {
            String keyString = json.get(MARITAL_STATUS).getAsString();
            if (keyString.equalsIgnoreCase(SINGLE)) {
                io.setMarital(MaritalStatus.SINGLE);
                return;
            }
            if (keyString.equalsIgnoreCase(IN_RELATIONSHIP)) {
                io.setMarital(MaritalStatus.IN_RELATIONSHIP);
                return;
            }
            if (keyString.equalsIgnoreCase(ENGAGED)) {
                io.setMarital(MaritalStatus.ENGAGED);
                return;
            }
            if (keyString.equalsIgnoreCase(MARRIED)) {
                io.setMarital(MaritalStatus.MARRIED);
                return;
            }
            if (keyString.equalsIgnoreCase(COMPLICATED)) {
                io.setMarital(MaritalStatus.COMPLICATED);
                return;
            }
            if (keyString.equalsIgnoreCase(OTHER)) {
                io.setMarital(MaritalStatus.OTHER);
                return;
            }
        }
    }

    private static void setBloodType(ProfileIOSession io, JsonObject json) {
        if (json.has(BLOODTYPE)) {

            String keyString = json.get(BLOODTYPE).getAsString();
            if (keyString.equalsIgnoreCase(BLOOD_A)) {
                io.setBloodtype(BloodType.A);
                return;
            }
            if (keyString.equalsIgnoreCase(BLOOD_B)) {
                io.setBloodtype(BloodType.B);
                return;
            }
            if (keyString.equalsIgnoreCase(BLOOD_AB)) {
                io.setBloodtype(BloodType.AB);
                return;
            }
            if (keyString.equalsIgnoreCase(BLOOD_O)) {
                io.setBloodtype(BloodType.O);
                return;
            }
        }
    }

    /**
     * Only serializes the parts that the API needs
     * 
     * @param profile
     * @return
     */
    public static JsonObject serializeProfile(Profile profile) {
        ProfileIOSession io = profile.getIOSession();
        JsonObject json = new JsonObject();
        try {

            json.addProperty(DESCRIPTION, io.getDescription());
            json.addProperty(BLOODTYPE, io.getBloodtype().toString());
            json.addProperty(MARITAL_STATUS, io.getMarital().toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }

}
