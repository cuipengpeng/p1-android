package com.p1.mobile.p1android.content.logic;

import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;

public class ReadPicture {

    /**
     * Currently returns the best memory representation available. Pictures will
     * be available after retrieving a BrowsePictureList.
     */
    public static Picture requestPicture(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Picture picture = ContentHandler.getInstance()
                .getPicture(id, requester);
        // TODO start asynchronous updates

        return picture;

    }

}
