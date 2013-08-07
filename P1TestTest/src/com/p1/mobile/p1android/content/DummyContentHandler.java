package com.p1.mobile.p1android.content;

/**
 * 
 * @author Anton
 * 
 * This class creates Content objects without the need of the singleton ContentHandler.
 * Use this to get rid of the overhead of tearing down the ContentHandler.
 */
public class DummyContentHandler {

    public static UserPicturesList createUserPicturesList(String id){
        return new UserPicturesList(id);
    }
    
    
    
}
