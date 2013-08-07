package com.p1.mobile.p1android.content;

import android.util.Log;

public class Venue extends Content {
    public static final String TYPE = "venue";

    private String address;
    private String category;
    private String name;

    private double latitude;
    private double longitude;

    protected Venue(String id) {
        super(id);
        IOSession = new VenueIOSession();
        Log.d(TAG, "Venue " + id + " created");
    }

    @Override
    public VenueIOSession getIOSession() {
        return (VenueIOSession) super.getIOSession();
    }

    public class VenueIOSession extends ContentIOSession {
        
        @Override
        public String getType(){
            return TYPE;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            Venue.this.address = address;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            Venue.this.category = category;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            Venue.this.name = name;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            Venue.this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            Venue.this.longitude = longitude;
        }

    }


}
