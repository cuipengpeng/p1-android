package com.p1.mobile.p1android.content.parsing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;

public abstract class UserParser {
    public static final String TAG = UserParser.class.getSimpleName();

    public static final String NAME = "name";
    public static final String EN_US = "en-US";
    public static final String ZH_CN = "zh-CN";
    public static final String PREFERREDLANGUAGE = "preferred_name"; // Contains
                                                                     // en-US or
                                                                     // zh-CN
    public static final String FULLNAME = "fullname";
    public static final String SURNAME = "surname";
    public static final String GIVENNAME = "givenname";
    public static final String PROFILE_PICTURE = "profile_picture";
    public static final String COVER_PICTURE = "cover_picture";
    public static final String THUMB_100 = "thumb100";
    public static final String THUMB_50 = "thumb50";
    public static final String THUMB_30 = "thumb30";
    public static final String URL = "url";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String COVER = "cover";
    public static final String BIRTHDATE = "birthdate";
    public static final String TYPE = "type";
    public static final String ETAG = "etag";
    public static final String GENDER = "gender";
    public static final String EDUCATION = "education";
    public static final String CAREER = "career";
    public static final String COMPANY = "company";
    public static final String POSITION = "position";
    public static final String CITY = "city";

    /**
     * 
     * @param json
     *            User json object
     * @param user
     * @return if the target object was changed
     */
    public static boolean parseToUser(JsonObject json, User user) {
        UserIOSession io = user.getIOSession();
        try {
            if (!json.has(TYPE) || !json.get(TYPE).getAsString().equals("user")) {
                Log.e(TAG, "Tried to unparse a json that is not a user: "
                        + json.toString());
            }
            GenericParser.parseToContent(json, io);
            if (json.has(GENDER) && !json.get(GENDER).isJsonNull()) {
                io.setGender(json.get(GENDER).getAsString());
            } else {
                io.setGender("");
            }
            if (json.has(PREFERREDLANGUAGE)
                    && !json.get(PREFERREDLANGUAGE).isJsonNull()) {
                io.setPreferredLanguage(json.get(PREFERREDLANGUAGE)
                        .getAsString());
            } else {
                io.setPreferredLanguage("");
            }
            setName(io, json);
            if (json.has(EDUCATION) && !json.get(EDUCATION).isJsonNull()) {
                io.setEducation(json.get(EDUCATION).getAsString());
            } else {
                io.setEducation("");
            }
            if (json.has(CAREER) && !json.get(CAREER).isJsonNull()
                    && !json.get(CAREER).isJsonArray()) {
                JsonObject career = json.getAsJsonObject(CAREER);
                io.setCareerCompany(career.get(COMPANY).getAsString());
                io.setCareerPosition(career.get(POSITION).getAsString());
            } else {
                io.setCareerCompany("");
                io.setCareerPosition("");
            }
            if (json.has(CITY) && !json.get(CITY).isJsonNull()) {
                io.setCity(json.get(CITY).getAsString());
            } else {
                io.setCity("");
            }
            if (json.has(BIRTHDATE) && !json.get(BIRTHDATE).isJsonNull()) {
                io.setBirthdate(GenericParser.parseAPIDate(json.get(BIRTHDATE)
                        .getAsString()));
            } else {
                io.setBirthdate(null);
            }

            setProfilePictures(io, json);
            setCoverPicture(io, json);

            io.setValid(true);
        } finally {
            io.close();
        }
        return true;
    }

    private static void setName(UserIOSession io, JsonObject username) {
        JsonObject name = username.getAsJsonObject(NAME);
        if (name != null) {
            JsonObject innerName;

            if (name.has(EN_US) && !name.get(EN_US).isJsonNull()) {
                innerName = name.getAsJsonObject(EN_US);
                Log.d(TAG, innerName.toString());
                if (innerName.get(FULLNAME) != null) {
                    io.setEnUsFullname(innerName.get(FULLNAME).getAsString());
                }
                if (innerName.get(SURNAME) != null) {
                    io.setEnUsSurname(innerName.get(SURNAME).getAsString());
                }
                if (innerName.get(GIVENNAME) != null) {
                    io.setEnUsGivenName(innerName.get(GIVENNAME).getAsString());
                }
            }
        }
    }

    private static void setProfilePictures(UserIOSession io, JsonObject userJson) {
        if (userJson.has(PROFILE_PICTURE)) {
            JsonObject profilePicture = userJson
                    .getAsJsonObject(PROFILE_PICTURE);
            JsonObject image;

            if (profilePicture.has(THUMB_100)) {
                image = profilePicture.getAsJsonObject(THUMB_100);
                io.setProfileThumb100Url(image.get(URL).getAsString());
            }

            if (profilePicture.has(THUMB_50)) {
                image = profilePicture.getAsJsonObject(THUMB_50);
                io.setProfileThumb50Url(image.get(URL).getAsString());
            }

            if (profilePicture.has(THUMB_30)) {
                image = profilePicture.getAsJsonObject(THUMB_30);
                io.setProfileThumb30Url(image.get(URL).getAsString());
            }
        }
    }

    private static void setCoverPicture(UserIOSession io, JsonObject userJson) {

        if (userJson.has(COVER_PICTURE)) {
            JsonObject coverPicture = userJson.getAsJsonObject(COVER_PICTURE);
            JsonObject image;
            if (coverPicture.has(COVER)) {
                image = coverPicture.getAsJsonObject(COVER);

                io.setCoverUrl(image.get(URL).getAsString());
                io.setCoverHeight(image.get(HEIGHT).getAsInt());
                io.setCoverWidth(image.get(WIDTH).getAsInt());
            }
        }
    }

    /**
     * Only serializes the parts that the API needs
     * 
     * @param user
     * @return
     */
    public static JsonObject serializeUser(User user) {
        UserIOSession io = user.getIOSession();
        JsonObject json = new JsonObject();
        try {

            json.addProperty(CITY, io.getCity());

            JsonObject nameJson = new JsonObject();
            JsonObject enNameJson = new JsonObject();
            enNameJson.addProperty(GIVENNAME, io.getEnUsGivenName());
            enNameJson.addProperty(SURNAME, io.getEnUsSurname());
            nameJson.add(EN_US, enNameJson);
            json.add(NAME, nameJson);

            JsonObject careerJson = new JsonObject();
            careerJson.addProperty(COMPANY, io.getCareerCompany());
            careerJson.addProperty(POSITION, io.getCareerPosition());
            json.add(CAREER, careerJson);

            json.addProperty(EDUCATION, io.getEducation());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
        return json;
    }

}
