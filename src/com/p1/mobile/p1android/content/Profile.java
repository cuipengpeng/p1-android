package com.p1.mobile.p1android.content;

import android.util.Log;

public class Profile extends Content {
    public static final String TYPE = "profile";

    public static enum BloodType {
        O, A, B, AB, UNKNOWN
    }

    public static enum Zodiac {
        ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARUS, CAPRICORNUS, AQUARIUS, PISCES
    }

    public static enum MaritalStatus {
        SINGLE, IN_RELATIONSHIP, ENGAGED, MARRIED, COMPLICATED, OTHER
    }

    private BloodType bloodtype;
    private Zodiac zodiac;
    private MaritalStatus marital;
    private String description;

    protected Profile(String id) {
        super(id);
        IOSession = new ProfileIOSession();
        Log.d(TAG, "Profile " + id + " created");
    }

    @Override
    public ProfileIOSession getIOSession() {
        return (ProfileIOSession) super.getIOSession();
    }

    public class ProfileIOSession extends ContentIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        public String getOwnerId() {
            return getId();
        }

        public BloodType getBloodtype() {
            return bloodtype;
        }

        public void setBloodtype(BloodType bloodtype) {
            Profile.this.bloodtype = bloodtype;
        }

        public Zodiac getZodiac() {
            return zodiac;
        }

        public void setZodiac(Zodiac zodiac) {
            Profile.this.zodiac = zodiac;
        }

        public MaritalStatus getMarital() {
            return marital;
        }

        public void setMarital(MaritalStatus marital) {
            Profile.this.marital = marital;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            Profile.this.description = description;
        }
    }

}
