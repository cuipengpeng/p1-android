package com.p1.mobile.p1android.content.logic;

import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Share;

public class ReadShare {

    /**
     * Currently returns the best memory representation available.
     */
    public static Share requestShare(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Share share = ContentHandler.getInstance().getShare(id, requester);

        return share;

    }

}
