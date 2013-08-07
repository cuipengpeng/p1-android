package com.p1.mobile.p1android.io.model.tags;


public class StringWrapperEntity implements TagEntity {
    private final String mString;
    
    public StringWrapperEntity(String text) {
        mString = text;
    }
    
    @Override
    public String getTagTitle() {
        return mString;
    }

}
