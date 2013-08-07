package com.p1.mobile.p1android.content;

import java.util.List;

public interface LikeableIOSession {

    public String getType();

    public String getId();

    /**
     * This method does not reflect all likes that are available through the
     * API. The size of the list is increased through calling
     * ReadLike.fillLikes()
     * 
     * @return ordered list of all likes fetched comments. Most recent like is
     *         at index 0.
     */
    public List<String> getLikeUserIds();

    /**
     * 
     * @return The total amount of likes that are on the object. This is
     *         accurate even before likes are fetched from the API.
     */
    public int getTotalLikes();

    public void setTotalLikes(int totalLikes);

    public void incrementTotalLikes();

    public void decrementTotalLikes();

    public boolean hasLiked();

    public void setHasLiked(boolean hasLiked);

    public void incrementUnfinishedUserModifications();

    public void decrementUnfinishedUserModifications();

    public long getLastAPIRequest();
    public void refreshLastAPIRequest();
    public void clearLastAPIRequest();

    public boolean hasMoreLikes();

    public void close();

    public boolean isValid();
}
