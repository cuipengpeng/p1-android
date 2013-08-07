package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

/**
 * 
 * @author Anton
 * 
 */
public class UserPicturesList extends Content {
    public static final String TAG = UserPicturesList.class.getSimpleName();

    public static final String TYPE = "user_pictures";

    public static final int PAGINATION_UNKNOWN = -1;

    public static final int INCOMPLETE_RESPONSES_ALLOWED = 2;

    // API information variables
    private List<String> paginatedIdList = new ArrayList<String>();

    private HashMap<String, Integer> knownIds = new HashMap<String, Integer>();

    private HashMap<String, UnlinkedIdCollection> unlinkedIds = new HashMap<String, UnlinkedIdCollection>();

    private int paginationLimit = 27; // Set amount of rows X*3, Uneven for a
                                      // center browse image
    private int paginationTotal = PAGINATION_UNKNOWN;

    private int notFullResponceCount = 0;

    protected UserPicturesList(String id) {
        super(id);

        IOSession = new UserPicturesListIOSession();
        Log.d(TAG, "created");
    }

    @Override
    public UserPicturesListIOSession getIOSession() {
        return (UserPicturesListIOSession) super.getIOSession();
    }

    public class UserPicturesListIOSession extends ContentIOSession {
        @Override
        public String getType() {
            return TYPE;

        }

        /**
         * Useful to get all pictures
         * 
         * @param index
         * @return
         */
        public String getPictureId(int index) {
            return paginatedIdList.get(index);
        }

        /**
         * For optimal use, call with the same originId each time
         * 
         * @param originId
         * @param offset
         * @return
         */
        public String getPictureId(String originId, int offset) {
            Integer originIndex = knownIds.get(originId);
            if (originIndex != null) { // origin is contained in the paginated
                                       // list
                int returnIndex = originIndex + offset;
                if (returnIndex >= 0 && returnIndex < paginatedIdList.size()) {
                    return paginatedIdList.get(returnIndex);
                }
                return null;
            }
            UnlinkedIdCollection unlinkedCollection = unlinkedIds.get(originId);
            if (unlinkedCollection == null) {
                int foundOriginIndex = paginatedIdList.indexOf(originId);
                if (foundOriginIndex != -1) {
                    knownIds.put(originId, foundOriginIndex);
                    return getPictureId(originId, offset); // KnownIds has been
                                                           // updated so that
                                                           // this method can
                                                           // return a proper id
                }
                ArrayList<String> singleIdArray = new ArrayList<String>();
                singleIdArray.add(originId);
                unlinkedCollection = new UnlinkedIdCollection(singleIdArray,
                        originId);
                unlinkedIds.put(originId, unlinkedCollection);
            }

            int returnIndex = unlinkedCollection.originOffset + offset;
            if (returnIndex >= 0
                    && returnIndex < unlinkedCollection.idList.size()) {
                return unlinkedCollection.idList.get(returnIndex);
            }
            return null;
        }

        public boolean requiresFillingNear(String originId) {
            Integer originIndex = knownIds.get(originId);
            if (originIndex != null) { // origin is contained in the paginated
                                       // list
                return false;
            }
            if (paginatedIdList.contains(originId)) {
                return false;
            }

            UnlinkedIdCollection unlinkedCollection = unlinkedIds.get(originId);
            if (unlinkedCollection == null) {
                return true;
            }
            return (unlinkedCollection.idList.size() <= 1);
        }

        public UnlinkedIdCollection getUnlinkedCollection(String originId) {
            Integer originIndex = knownIds.get(originId);
            if (originIndex != null) { // origin is contained in the paginated
                                       // list
                return new UnlinkedIdCollection(paginatedIdList, originId);
            }
            UnlinkedIdCollection unlinkedCollection = unlinkedIds.get(originId);
            if (unlinkedCollection == null) {
                int foundOriginIndex = paginatedIdList.indexOf(originId);
                if (foundOriginIndex != -1) {
                    knownIds.put(originId, foundOriginIndex);
                    return new UnlinkedIdCollection(paginatedIdList, originId);
                }
                ArrayList<String> singleIdArray = new ArrayList<String>();
                singleIdArray.add(originId);
                unlinkedCollection = new UnlinkedIdCollection(singleIdArray,
                        originId);
                unlinkedIds.put(originId, unlinkedCollection);
            }
            return unlinkedCollection;
        }

        public List<String> getPictureIdList(String originId) {
            UnlinkedIdCollection collection = getUnlinkedCollection(originId);

            return collection.getIdList();
        }

        public void addPaginatedIds(List<String> newIds) {
            paginatedIdList.addAll(newIds); // TODO remove duplicates caused by
                                            // offset errors.
        }

        public void fillIds(List<String> newIds, RangePagination range) { // TODO
                                                                          // doublecheck
                                                                          // cornercases
            Integer originIndex = knownIds.get(range.originId);
            if (originIndex != null) { // origin is contained in the paginated
                                       // list
                int startIndex;
                if (newIds.size() == range.expectedSize()) {
                    startIndex = originIndex - range.negativeRange;
                } else {
                    startIndex = paginatedIdList.indexOf(newIds.get(0));
                }
                paginatedIdList.addAll(startIndex, newIds);
            } else {
                UnlinkedIdCollection unlinkedCollection = unlinkedIds
                        .get(range.originId);
                if (unlinkedCollection == null) {
                    ArrayList<String> singleIdArray = new ArrayList<String>();
                    singleIdArray.add(range.originId);
                    unlinkedCollection = new UnlinkedIdCollection(
                            singleIdArray, range.originId);
                    unlinkedIds.put(range.originId, unlinkedCollection);
                }
                unlinkedCollection.fillIds(newIds, range);
            }
        }

        public boolean isAKnownId(String pictureId) {
            return knownIds.containsKey(pictureId);
        }

        public int getPaginationNextOffset() {
            return paginatedIdList.size();
        }

        public int getPaginationLimit() {
            return paginationLimit;
        }

        public RangePagination getPaginationNegative(String originId) {
            RangePagination pagination = new RangePagination(originId);
            UnlinkedIdCollection unlinkedCollection = unlinkedIds.get(originId);
            pagination.positiveRange = -unlinkedCollection.negativeSize() - 1;
            pagination.negativeRange = unlinkedCollection.negativeSize()
                    + paginationLimit;
            return pagination;
        }

        public RangePagination getPaginationPositive(String originId) {
            RangePagination pagination = new RangePagination(originId);
            UnlinkedIdCollection unlinkedCollection = unlinkedIds.get(originId);
            pagination.negativeRange = -unlinkedCollection.positiveSize() - 1;
            pagination.positiveRange = unlinkedCollection.positiveSize()
                    + paginationLimit;
            return pagination;

        }

        public RangePagination getPaginationInitial(String originId) {
            return new RangePagination(originId);
        }

        public boolean hasMore() {
            return getPaginationNextOffset() != paginationTotal
                    && notFullResponceCount < INCOMPLETE_RESPONSES_ALLOWED;
        }

        public int getPaginationTotal() {
            return paginationTotal;
        }

        public void setPaginationTotal(int paginationTotal) {
            UserPicturesList.this.paginationTotal = paginationTotal;
        }

        public void reportIncompleteNetworkResponse() {
            notFullResponceCount++;
        }

    }

    private class UnlinkedIdCollection {
        private List<String> idList = new ArrayList<String>();
        private int originOffset;

        /**
         * 
         * @param list
         *            must contain centerId
         * @param centerId
         */
        public UnlinkedIdCollection(List<String> list, String centerId) {
            idList = list;
            originOffset = idList.indexOf(centerId);
        }

        /**
         * This function assumes that UnlinkedIdCollection is newly created, or
         * that just one end of the collection is increased.
         * 
         * @param newIds
         * @param range
         */
        public void fillIds(List<String> newIds, RangePagination range) {
            if (idList.size() == 1) { // newly created
                idList = newIds;
                originOffset = idList.indexOf(range.originId);
                return;
            }
            if (range.negativeRange > 0 && range.positiveRange > 0) {
                Log.e(TAG,
                        "Tried to fill UnlinkedIdCollection with a range spanning over origin. Nothing has been added.");
                return;
            }
            if (range.negativeRange > 0) {
                newIds.addAll(idList);
                idList = newIds;
                originOffset = idList.indexOf(range.originId);
                return;
            }
            if (range.positiveRange > 0) {
                idList.addAll(newIds);
            }
        }

        public List<String> getIdList() {
            return idList;
        }

        /**
         * @return amount of Ids with index greater than origin
         */
        public int positiveSize() {
            return idList.size() - originOffset - 1;
        }

        /**
         * @return amount of Ids with index lesser than origin
         */
        public int negativeSize() {
            return originOffset;
        }
    }

    public class RangePagination {
        public String originId;
        public int negativeRange;
        public int positiveRange;

        private RangePagination(String originId) {
            this.originId = originId;
            negativeRange = paginationLimit / 2;
            positiveRange = paginationLimit / 2;
        }

        public int expectedSize() {
            return negativeRange + positiveRange + 1; // +1 for center
        }

    }

}
