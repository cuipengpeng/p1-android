package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.p1.mobile.p1android.util.Utils;

/**
 * 
 * @author Anton
 * 
 */
public class Feed extends Content {
    public static final String TAG = Feed.class.getSimpleName();

    public static final String TYPE = "feed";

    // API information variables
    private List<String> shareIdList = new ArrayList<String>();
    private List<String> fakeShareIdList = new ArrayList<String>();

    private int paginationNextOffset = 0;
    private int paginationLimit = 15; // Can be anything

    private boolean resetOnNextUpdate = true;

    protected Feed() {
        super("0"); // Id is irrelevant for feed
        

        IOSession = new FeedIOSession();
        Log.d(TAG, "created");
    }

    @Override
    public FeedIOSession getIOSession() {
        return (FeedIOSession) super.getIOSession();
    }

    public class FeedIOSession extends ContentIOSession {
        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public void replaceFakeId(String oldId, String newId) {
            boolean replaced = Utils.checkAndReplaceId(shareIdList, oldId,
                    newId);
            if (!replaced && fakeShareIdList.contains(oldId)) {
                shareIdList.add(0, newId);
            }
            fakeShareIdList.remove(oldId);
        }

        public List<String> getShareIdList() {
            return shareIdList;
        }

        public void addFakeShareId(String fakeId) {
            shareIdList.add(0, fakeId);
            fakeShareIdList.add(0, fakeId);
        }

        public int getPaginationNextOffset() {
            return paginationNextOffset;
        }

        public void incrementOffset() {
            paginationNextOffset++;
        }

        public int getPaginationLimit() {
            return paginationLimit;
        }

        public boolean hasMore() {
            return true;
        }

        public boolean isResetOnNextUpdate() {
            return resetOnNextUpdate;
        }

        public void resetOnNextUpdate() {
            clearLastAPIRequest(); // Allows a new request to be made
            paginationNextOffset = 0;
            resetOnNextUpdate = true;
        }

        /**
         * Resets to empty state, but keeping listeners. Used when
         * refreshing/changing filter.
         */
        public void reset() {
            shareIdList.clear();
            shareIdList.addAll(fakeShareIdList);
            resetOnNextUpdate = false;
        }

    }

}
