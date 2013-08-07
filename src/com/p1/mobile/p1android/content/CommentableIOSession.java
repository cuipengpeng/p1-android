package com.p1.mobile.p1android.content;

import java.util.List;

public interface CommentableIOSession {

    public String getType();

    public String getId();

    /**
     * This method does not reflect all comments that are available through the
     * API. The size of the list is increased through calling
     * ReadComment.fillComments()
     * 
     * @return ordered list of all currently fetched comments. Most recent
     *         comment is at index 0.
     */
    public List<String> getCommentIds();

    /**
     * 
     * @return The total amount of comments that are on the object. This is
     *         accurate even before comments are fetched from the API.
     */
    public int getTotalComments();

    public void setTotalComments(int totalComments);

    public void incrementTotalComments();

    public void incrementUnfinishedUserModifications();

    public void decrementUnfinishedUserModifications();
    
    public long getLastAPIRequest();
    public void refreshLastAPIRequest();
    public void clearLastAPIRequest();

    public boolean hasMoreComments();

    public void close();

    public boolean isValid();
}
