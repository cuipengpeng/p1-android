package com.p1.mobile.p1android.content.logic;

import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Message;

public class ReadMessage {
    /**
     * Currently returns the best memory representation available.
     */
    public static Message requestMessage(String id, IContentRequester requester) {
        if (id == null)
            throw new NullPointerException("Id must be non-null");
        Message message = ContentHandler.getInstance()
                .getMessage(id, requester);

        return message;

    }

}
