package com.p1.mobile.p1android.content;

import android.util.Log;


/**
 * 
 * @author Anton
 * 
 * References a specific Content object.
 * Get the actual Content using ReadContent.requestContent(IdTypePair, IContentRequester)
 */
public class IdTypePair {

    public enum Type {
        LIKE, RELATIONSHIP, TAG, COMMENT, PICTURE, SHARE, USER, MESSAGE;

        public static Type getEnum(String value) {
            if (value == null)
                throw new IllegalArgumentException();
            for (Type v : values())
                if (value.equalsIgnoreCase(v.name()))
                    return v;
            Log.e(NotificationStory.TAG, "Unable to map " + value
                    + " to Type enum");
            throw new IllegalArgumentException();
        }
    }

    public String id;
    public Type type;
    
    public IdTypePair(String id, Type type) {
        this.id = id;
        this.type=type;
    }

    @Override
    public String toString() {
        return "IdTypePair [id=" + id + ", type=" + type + "]";
    }
    
}
