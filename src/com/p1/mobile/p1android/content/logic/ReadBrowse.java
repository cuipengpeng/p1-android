package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.BrowseList;
import com.p1.mobile.p1android.content.BrowseList.BrowseListIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.parsing.BrowseListParser;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

public class ReadBrowse {
    public static final String TAG = ReadBrowse.class.getSimpleName();

    /**
     * Returns a BrowseList that is scheduled to be emptied and refilled using
     * the provided filter. Use this method when refreshing or changing filters.
     * 
     * @param requester
     * @param filter
     * @return
     */
    public static BrowseList requestBrowsePicturesList(
            IContentRequester requester, BrowseFilter filter) {
        BrowseList browseList = ContentHandler.getInstance()
                .getBrowsePicturesList(requester);

        BrowseListIOSession io = browseList.getIOSession();
        try {
            if (!io.matchExpectedFilter(filter)) {
                io.resetOnNextUpdate();
                io.setExpectedFilter(filter);
            }

        } finally {
            io.close();
        }
        fillBrowseList(browseList, filter);

        return browseList;
    }

    /**
     * Returns a BrowseList that is scheduled to be emptied and refilled using
     * the provided filter. Use this method when refreshing or changing filters.
     * 
     * @param requester
     * @param filter
     * @return
     */
    public static BrowseList requestBrowseMembersList(
            IContentRequester requester, BrowseFilter filter) {
        BrowseList browseList = ContentHandler.getInstance()
                .getBrowseMembersList(requester);

        BrowseListIOSession io = browseList.getIOSession();
        try {
            if (!io.matchExpectedFilter(filter)) {
                io.resetOnNextUpdate();
                io.setExpectedFilter(filter);
            }
        } finally {
            io.close();
        }
        fillBrowseList(browseList, filter);

        return browseList;
    }

	public static BrowseList requestRefreshedMembers(IContentRequester requester,
			BrowseFilter filter) {
		BrowseList browseList = ContentHandler.getInstance()
				.getBrowseMembersList(requester);

		BrowseListIOSession io = browseList.getIOSession();
		try {
			io.resetOnNextUpdate();
		} finally {
			io.close();
		}
		fillBrowseList(browseList, filter);

		return browseList;
	}

	public static BrowseList requestRefreshedPictures(IContentRequester requester,
			BrowseFilter filter) {
		BrowseList browseList = ContentHandler.getInstance()
				.getBrowsePicturesList(requester);

		BrowseListIOSession io = browseList.getIOSession();
		try {
			io.resetOnNextUpdate();
		} finally {
			io.close();
		}
		fillBrowseList(browseList, filter);

		return browseList;
	}

    /**
     * Will retrieve more information of a specific BrowseList using pagination,
     * and notify all IContentRequesters of any successful changes. It is safe
     * to call this method quickly a large number of times.
     * 
     * @param browseList
     */
    public static void fillBrowseList(final BrowseList browseList,
            final BrowseFilter filter) {
        boolean shouldMakeNetworkCall = false;
        BrowseListIOSession io = browseList.getIOSession();
        try {
            if (io.getLastAPIRequest() == 0 && io.matchExpectedFilter(filter)
                    && io.hasMore()) {
                shouldMakeNetworkCall = true; // Fetch new information
                io.setHasFailedNetworkOperation(false);
            }
            if (shouldMakeNetworkCall) {
                io.refreshLastAPIRequest();
            }
        } finally {
            io.close();
        }

        if (shouldMakeNetworkCall) {
            ContentHandler.getInstance().getNetworkHandler()
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            int paginationOffset, paginationLimit;
                            boolean isMemberBrowse;
                            BrowseListIOSession io = browseList.getIOSession();
                            try {
                                paginationOffset = io.getPaginationNextOffset();
                                paginationLimit = io.getPaginationLimit();
                                isMemberBrowse = io.isMemberBrowse();

                            } finally {
                                io.close();
                            }

                            String browseRequest;
                            if (isMemberBrowse) {
                                browseRequest = ReadContentUtil.netFactory
                                        .createGetBrowseMembersRequest(
                                                paginationOffset,
                                                paginationLimit, filter);
                            } else {
                                browseRequest = ReadContentUtil.netFactory
                                        .createGetBrowsePicturesRequest(
                                                paginationOffset,
                                                paginationLimit, filter);
                            }

                            try {
                                Network network = NetworkUtilities.getNetwork();

                                JsonObject jsonResponse = network
                                        .makeGetRequest(browseRequest, null)
                                        .getAsJsonObject();

                                JsonObject data = jsonResponse
                                        .getAsJsonObject("data");
                                JsonArray pictureArray = data
                                        .getAsJsonArray("pictures");
                                ReadContentUtil.saveExtraPictures(pictureArray);
                                JsonArray usersArray = data
                                        .getAsJsonArray("users");
                                ReadContentUtil.saveExtraUsers(usersArray);
                                if (isMemberBrowse) {
                                    JsonArray membersArray = data
                                            .getAsJsonArray("members");
                                    ReadContentUtil
                                            .saveExtraMembers(membersArray);
                                }

                                BrowseListParser.appendToBrowseList(
                                        jsonResponse, browseList, filter);

                                Log.d(TAG,
                                        "All listeners notified as result of requestBrowseList");
                            } catch (Exception e) {
                                Log.e(TAG,
                                        "Failed getting browse list", e);
                                io = browseList.getIOSession();
                                try {
                                    io.setHasFailedNetworkOperation(true);
                                } finally {
                                    io.close();
                                }

                            } finally {
                                io = browseList.getIOSession();
                                try {
                                    io.clearLastAPIRequest();
                                } finally {
                                    io.close();
                                }
                                browseList.notifyListeners();
                            }
                        }
                    });
        }
    }

}
