package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

public class Member extends Content {
    public static final String TYPE = "member";

    private List<String> pictureIds = new ArrayList<String>();
    private Date latestActivity;

    protected Member(String id) {
        super(id);
        IOSession = new MemberIOSession();
        Log.d(TAG, "Member " + id + " created");
    }

    @Override
    public MemberIOSession getIOSession() {
        return (MemberIOSession) super.getIOSession();
    }

    public class MemberIOSession extends ContentIOSession {
        
        @Override
        public String getType(){
            return TYPE;
        }

        public String getOwnerId() {
            return getId(); // Owner has same id as member
        }

        /**
         * Returns a list of 0, 4 or 8 picture ids
         * @return
         */
        public List<String> getPictureIds() {
            while(pictureIds.size() % 4 != 0 || pictureIds.size() > 8){
                pictureIds.remove(pictureIds.size()-1); // Remove the last picture
            }
            
            return pictureIds;
        }
        
        /**
         * Returns a short string saying amount of a time unit, for example 15s, 3h or 2d
         * Later on, this will return a localized string
         * @return
         */
        public String getFormattedLatestActivity() { // TODO Use localized strings for 's', 'm'...
            if (latestActivity == null) {
                return "UnKnown";
            }
            
            long difference = System.currentTimeMillis() - latestActivity.getTime();
            if (difference < 0){ // Account for poorly configured clock
                difference = 0;
            }
            difference /= 1000; // to seconds
            if(difference < 60){
                return difference + "s";
            }
            difference /= 60; // to minutes
            if(difference < 60){
                return difference + "m";
            }
            difference /= 60; // to hours
            if(difference < 60){
                return difference + "h";
            }
            difference /= 24; // to days
            return difference + "d";
            
        }
        
        public Date getLatestActivity() {
            return latestActivity;
        }

        public void setLatestActivity(Date latestActivity) {
            Member.this.latestActivity = latestActivity;
        }
    }

    

}
