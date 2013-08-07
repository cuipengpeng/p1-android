package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 
 * @author Anton
 * 
 */
public class BrowseList extends Content {
    public static final String TAG = BrowseList.class.getSimpleName();
    public static final String TYPE_BROWSE_PICTURE = "browse_pictures";
    public static final String TYPE_BROWSE_MEMBER = "browse_members";

    public static final int PAGINATION_UNKNOWN = -1;

    public static final int INCOMPLETE_RESPONSES_ALLOWED = 2;

    // API information variables
    private List<String> idList = new ArrayList<String>();

    private boolean isMemberBrowse;

    private int paginationNextOffset = 0;
    private int paginationLimit = 30; // Can be anything

    private boolean resetOnNextUpdate = true;
    private BrowseFilter expectedFilter = new BrowseFilter();
    private int notFullResponceCount = 0;

    protected BrowseList(boolean isMemberBrowse) {
        super("0"); // Id is irrelevant for browse
        this.isMemberBrowse = isMemberBrowse;
        if (isMemberBrowse) {
            paginationLimit = 10; // Member objects are much larger than picture
                                  // objects, as each include several pictures
        }

        IOSession = new BrowseListIOSession();
        Log.d(TAG, "created");
    }

    @Override
    public BrowseListIOSession getIOSession() {
        return (BrowseListIOSession) super.getIOSession();
    }

    public class BrowseListIOSession extends ContentIOSession {
        @Override
        public String getType() {
            if (isMemberBrowse) {
                return TYPE_BROWSE_MEMBER;
            } else {
                return TYPE_BROWSE_PICTURE;
            }

        }

        /**
         * Returns a list of Id's to either Picture or Member Content objects,
         * depending on which BrowseList is used
         * 
         * @return
         */
        public List<String> getIdList() {
            return idList;
        }

        public void addId(String newId) {
            if (!idList.contains(newId)) {
                idList.add(newId);
            } else {
                Log.d(TAG, "Browse skipped adding id " + newId
                        + " as it is already present in the list");
            }
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

        public void reportIncompleteNetworkResponse() {
            notFullResponceCount++;
            Log.d(TAG, "Incomplete network response. Current count is "
                    + notFullResponceCount);
        }

        public boolean hasMore() {
            return notFullResponceCount < INCOMPLETE_RESPONSES_ALLOWED;
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
            idList.clear();
            resetOnNextUpdate = false;
            Log.d(TAG, "Successfully reset the list");

        }

        public boolean matchExpectedFilter(BrowseFilter testedFilter) {
            return expectedFilter.equals(testedFilter);
        }

        public void setExpectedFilter(BrowseFilter expectedFilter) {
            if (expectedFilter == null) {
                throw new NullPointerException("Filter can not be null");
            }
            BrowseList.this.expectedFilter = expectedFilter;
            notFullResponceCount = 0; // Be able to fetch more info again
        }

        public boolean isMemberBrowse() {
            return isMemberBrowse;
        }

    }

}
