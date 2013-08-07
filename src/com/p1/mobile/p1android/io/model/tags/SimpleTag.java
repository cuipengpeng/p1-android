package com.p1.mobile.p1android.io.model.tags;


public interface SimpleTag {
    /** Measured in percentage of the image width */
    float getLocationX();

    /** Measured in percentage of the image height */
    float getLocationY();
    
    String getTitle();

    void setEntity(TagEntity entitiy);
    
    TagEntity getEntity();
}
