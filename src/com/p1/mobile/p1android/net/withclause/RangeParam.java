package com.p1.mobile.p1android.net.withclause;


import android.util.Log;

import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;

public class RangeParam implements Param {
    public static final String TAG = RangeParam.class.getSimpleName();

    private RangePagination range;
    private static final String SEPARATOR = ",";
    private static final String START = "range=";

    @Override
    public boolean isEmpty() {
        return range == null;
    }

    @Override
    public void addParam(String param) {
        Log.e(TAG, "RangeParam does not support addParam");
    }
    
    public void setRange(RangePagination range){
        this.range = range;
    }

    @Override
    public String getParamString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append(START);
        builder.append(range.originId);
        builder.append(SEPARATOR);
        builder.append(range.positiveRange);
        builder.append(SEPARATOR);
        builder.append(range.negativeRange);

        return builder.toString();
    }

}
