package com.p1.mobile.p1android.net.withclause;


import android.util.Log;

public class LatLongParam implements Param {
    public static final String TAG = LatLongParam.class.getSimpleName();

    private double latitude;
    private double longitude;
    private static final String SEPARATOR = ",";
    private static final String START = "ll=";

    @Override
    public boolean isEmpty() {
        return latitude == 0 && longitude == 0;
    }

    @Override
    public void addParam(String param) {
        Log.e(TAG, "RangeParam does not support addParam");
    }
    
    public void setValues(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String getParamString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append(START);
        builder.append(latitude);
        builder.append(SEPARATOR);
        builder.append(longitude);

        return builder.toString();
    }

}
