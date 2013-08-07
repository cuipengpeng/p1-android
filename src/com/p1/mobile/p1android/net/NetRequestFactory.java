package com.p1.mobile.p1android.net;

import java.util.Date;

import android.util.Log;

import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.UserPicturesList.RangePagination;
import com.p1.mobile.p1android.net.withclause.WithClauseBuilder;

public class NetRequestFactory {
    private static final String TAG = NetRequestFactory.class.getSimpleName();

    // Regular expression for finding {any_character_sequence}
    private static final String REGEX = "(\\{)(.*)(\\})";

    private static final String ME = "me";

    public String createGetFollowersRequest(String id, int paginationOffset,
            int paginationLimit) {
        String url = ApiCalls.FOLLOWERS_URI;
        url = url.replaceAll(REGEX, ME);
        url = url
                + new WithClauseBuilder()
                        .addOrderParam(WithClauseBuilder.ORDER_DESC)
                        .addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit);
        Log.d(TAG, "Followers request: " + url);
        return url;
    }

    public String createGetFollowingRequest(String id, int paginationOffset,
            int paginationLimit) {
        String url = ApiCalls.FOLLOWING_URI;
        url = url.replaceAll(REGEX, ME);
        url = url
                + new WithClauseBuilder()
                        .addOrderParam(WithClauseBuilder.ORDER_DESC)
                        .addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit);
        Log.d(TAG, "Following request: " + url);
        return url;
    }

    public String createGetVenuesRequest(String searchString, double latitude,
            double longitude) {
        String url = ApiCalls.VENUE_URI;
        url = url
                + new WithClauseBuilder().addSearchParam(searchString)
                        .addPositionParam(latitude, longitude);
        Log.d(TAG, "Venues request: " + url);
        return url;
    }

    public String createGetUserPicturesListRequest(String id,
            RangePagination range) {
        String url = ApiCalls.PICTURES_URI;
        url = url.replaceAll(REGEX, id);
        url = url + new WithClauseBuilder().addRangeParam(range);
        Log.d(TAG, "UserPicturesList request: " + url);
        return url;
    }

    public String createGetUserPicturesListRequest(String id,
            int paginationOffset, int paginationLimit) {
        String url = ApiCalls.PICTURES_URI;
        url = url.replaceAll(REGEX, id);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit);
        Log.d(TAG, "UserPicturesList request: " + url);
        return url;
    }

    public String createGetBrowsePicturesRequest(int paginationOffset,
            int paginationLimit, BrowseFilter filter) {
        if (filter == null) {
            filter = new BrowseFilter();
        }

        String url = ApiCalls.BROWSE_PICTURES_URI;
        Log.d(TAG, "Browse request url " + url);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addBrowseFilter(filter)
                        .addWithUsersParam();
        Log.d(TAG, "Browse request after with " + url);
        return url;
    }

    public String createGetBrowseMembersRequest(int paginationOffset,
            int paginationLimit, BrowseFilter filter) {
        if (filter == null) {
            filter = new BrowseFilter();
        }

        String url = ApiCalls.BROWSE_MEMBERS_URI;
        Log.d(TAG, "Browse request url " + url);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addBrowseFilter(filter)
                        .addWithPicturesParam().addWithUsersParam();
        Log.d(TAG, "Browse request after with " + url);
        return url;
    }

    public String createGetSpecificMembersRequest(String memberId) {
        String url = ApiCalls.BROWSE_MEMBERS_URI + "/" + memberId;
        Log.d(TAG, "Member request url " + url);
        return url;
    }

    public String createGetConversationsRequest(int paginationOffset,
            int paginationLimit) {

        String url = ApiCalls.CONVERSATIONS_URI;
        Log.d(TAG, "Conversations request url " + url);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addWithUsersParam()
                        .addWithMessagesParam();
        Log.d(TAG, "Conversations request after with " + url);
        return url;
    }

    public String createConversationRequest(String conversationId) {

        String url = ApiCalls.CONVERSATIONS_URI + "/" + conversationId;
        Log.d(TAG, "Conversations request url " + url);
        return url;
    }

    /**
     * 
     * @param conversationId
     * @param oldestMessageTime
     *            may be null for retrieving the latest messages
     * @param paginationLimit
     * @return
     */
    public String createGetConversationMessagesRequest(String conversationId,
            Date oldestMessageTime, int paginationLimit) {
        String url = ApiCalls.MESSAGES_URI;
        url = url.replaceAll(REGEX, conversationId);
        WithClauseBuilder withClause = new WithClauseBuilder()
                .addLimitParam(paginationLimit);
        if (oldestMessageTime != null) {
            withClause.addUntilParam(oldestMessageTime);
        }
        url = url
 + withClause;
        Log.d(TAG, "message request url: " + url);
        return url;
    }

    public String createGetNotificationsRequest(int paginationOffset,
            int paginationLimit) {
        String url = ApiCalls.NOTIFICATIONS_URI;
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addWithUsersParam()
                        .addWithPicturesParam().addWithSharesParam()
                        .addWithCommentsParam();
        Log.d(TAG, "notification request url: " + url);
        return url;
    }

    public String createNotificationsRequest() {
        String url = ApiCalls.NOTIFICATIONS_URI;
        Log.d(TAG, "notification url: " + url);
        return url;
    }

    public String createGetFeedRequest(int paginationOffset, int paginationLimit) {

        String url = ApiCalls.FEED;
        Log.d(TAG, "Feed request url " + url);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addWithUsersParam()
                        .addWithCommentsParam().addWithPicturesParam()
                        .addWithTagsParam().addWithVenuesParam();
        Log.d(TAG, "Feed request after with " + url);
        return url;
    }

    public String createGetUserRequest(String userId) {
        String url = ApiCalls.SPECIFIC_USER_URI;
        url = url.replaceAll(REGEX, userId);
        Log.d(TAG, "user request url: " + url);
        return url;
    }

    public String createGetProfileRequest(String profileId) {
        String url = ApiCalls.PROFILE_URI;
        url = url.replaceAll(REGEX, profileId);
        url = url + new WithClauseBuilder().addWithUsersParam();
        Log.d(TAG, "profile request url: " + url);
        return url;
    }

    public String createProfileRequest(String profileId) {
        String url = ApiCalls.PROFILE_URI;
        url = url.replaceAll(REGEX, profileId);
        Log.d(TAG, "profile url: " + url);
        return url;
    }

    public String createAccountRequest() {
        String url = ApiCalls.MY_ACCOUNT_URI;
        Log.d(TAG, "account request url: " + url);
        return url;
    }

    public String createPostMessageRequest(String conversationId) {
        String url = ApiCalls.MESSAGES_URI;
        url = url.replaceAll(REGEX, conversationId);
        Log.d(TAG, "message post url: " + url);
        return url;
    }

    public String createBatchRequest() {
        String url = ApiCalls.BATCH;
        Log.d(TAG, "batch url: " + url);
        return url;
    }

    /**
     * Used for both PUT and DELETE
     * 
     * @param likedType
     * @param likedId
     * @return
     */
    public String createLikeRequest(String likedType, String likedId) {
        String url = ApiCalls.MY_LIKE_URI;
        url = url.replaceAll("\\{content_type\\}", likedType + "s");
        url = url.replaceAll("\\{content_id\\}", likedId);
        Log.d(TAG, "like url: " + url);
        return url;
    }

    public String createGetLikesRequest(String likedType, String likedId,
            int paginationOffset, int paginationLimit) {
        String url = ApiCalls.MY_LIKE_URI;
        url = url.replaceAll("\\{content_type\\}", likedType + "s");
        url = url.replaceAll("\\{content_id\\}", likedId);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addWithUsersParam();
        Log.d(TAG, "like url: " + url);
        return url;
    }

    public String createCommentRequest(String commentedType, String commentedId) {
        String url = ApiCalls.COMMENTS_URI;
        url = url.replaceAll("\\{content_type\\}", commentedType + "s");
        url = url.replaceAll("\\{content_id\\}", commentedId);
        Log.d(TAG, "comment url: " + url);
        return url;
    }

    public String createGetCommentsRequest(String commentedType,
            String commentedId, int paginationOffset, int paginationLimit) {
        String url = ApiCalls.COMMENTS_URI;
        url = url.replaceAll("\\{content_type\\}", commentedType + "s");
        url = url.replaceAll("\\{content_id\\}", commentedId);
        url = url
                + new WithClauseBuilder().addOffsetParam(paginationOffset)
                        .addLimitParam(paginationLimit).addWithUsersParam();
        Log.d(TAG, "comment url: " + url);
        return url;
    }

    /**
     * Used for both PUT and DELETE
     * 
     * @param targetUserId
     * @return
     */
    public String createRelationshipRequest(String targetUserId) {
        String url = ApiCalls.RELATIONSHIP_MODIFICATION_URI;
        url = url.replaceAll(REGEX, targetUserId);
        Log.d(TAG, "Relationship url: " + url);
        return url;
    }
}
