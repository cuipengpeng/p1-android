/**
 * P1ApiCalls.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.net;

import com.p1.mobile.p1android.Config;

/**
 * Class containing exactly one method for each API call.
 * 
 * @author Viktor Nyblom
 * 
 */
public class ApiCalls {
    // URIs
    // public static final String BASE_URI =
    // "http://api.master.testing.p1staff.com/v1/core";
    // public static final String AUTH_URI =
    // "http://accounts.bjs.dev.p1staff.com/v1/oauth2/access_token";
    public static final String DEVICE = "android";
    public static final String DOMAIN = Config.API_URL;
    // public static final String WEB_DOMAIN = DOMAIN;
    private static final String WEB_DOMAIN = Config.WEB_URL;
    // public static final String FRONTEND_TESTING_BASE_URL =
    // "http://frontend.testing.office.p1staff.com";
    public static final String API_VERSION = "v2";

    public static final String ACCOUNT_URI = "http://accounts." + DOMAIN + "/"
            + API_VERSION;
    private static final String WEB_BASE_URL = "http://" + WEB_DOMAIN;
    public static final String BASE_URI = "http://api." + DOMAIN + "/"
            + API_VERSION + "/core";

    private static final String WEB_MIGRATION_URL = WEB_BASE_URL
            + "/signup-profile?device=" + DEVICE
            + "&p1cnAccessToken=$access";
    
    
    private static final String WEB_INVITATION_URL = WEB_BASE_URL
            + "/invitation-profile?device=" + DEVICE
            + "&activationAccessToken=$access";

    public static String getWebInvitationUrl(String invitationAccessToken) {
        return WEB_INVITATION_URL.replace("$access", invitationAccessToken);
    }

    public static String getWebMigrationUrl(String migrationAccessToken) {
        return WEB_MIGRATION_URL.replace("$access", migrationAccessToken);
    }
    
    
    public static final String FORGOT_PASSWORD_URI = ACCOUNT_URI
            + "/forgot-password";
    public static final String NEW_PASSWORD_URI = ACCOUNT_URI
            + "/change-password";

    public static final String APPLY_URI = ACCOUNT_URI + "/invitation-request";

    public static final String AUTH_URI = ACCOUNT_URI + "/oauth2/access_token";
    public static final String CN_AUTH_URI = ACCOUNT_URI
            + "/detect-p1cn-password";
    public static final String MIGRATION_URI = ACCOUNT_URI
            + "/p1cn/access_token";
    public static final String ACTIVATION_URI = ACCOUNT_URI
            + "/activation/access_token";

    public static final String HOST = "api.master.unstable.p1staff.com";

    public static final String USERS_URI = BASE_URI + "/users";
    public static final String USERS_ME_URI = USERS_URI + "/me";
    public static final String SPECIFIC_USER_URI = USERS_URI + "/{user_id}";

    public static final String ACCOUNTS_URI = BASE_URI + "/accounts";
    public static final String MY_ACCOUNT_URI = ACCOUNTS_URI;

    public static final String RELATIONSHIPS_URI = USERS_URI
            + "/{user_id}/relationships";
    public static final String RELATIONSHIP_ID_URI = RELATIONSHIPS_URI
            + "/{relationship_id}";
    public static final String RELATIONSHIP_MODIFICATION_URI = USERS_ME_URI
            + "/relationships/{relationship_id}";

    public static final String FRIENDS_URI = USERS_URI + "/{user_id}/friends";
    public static final String MUTUAL_FRIENDS = "/mutual-friends";

    public static final String FOLLOWING_URI = USERS_URI
            + "/{user_id}/following";
    public static final String FOLLOWERS_URI = USERS_URI
            + "/{user_id}/followers";

    public static final String GALLERY_PICTURES = BASE_URI
            + "/users/{user_id}/galleries/{gallery_id}/pictures";

    public static final String PICTURE_LIKE = GALLERY_PICTURES
            + "/{picture_id}/likes/{liker_id}";

    public static final String PICTURE_UNLIKE = BASE_URI
            + "/pictures/{picture_id}/likes/{liker_id}";

    public static final String COMMENTS = BASE_URI
            + "/pictures/{picture_id}/comments";

    public static final String BATCH = BASE_URI + "/batch";

    public static final String FEED = BASE_URI + "/feed";

    public static final String BROWSE_PICTURES_URI = BASE_URI + "/pictures";
    public static final String BROWSE_MEMBERS_URI = BASE_URI + "/members";

    public static final String PICTURES_URI = BASE_URI
            + "/users/{user_id}/pictures";

    public static final String CONVERSATIONS_URI = USERS_ME_URI
            + "/conversations";

    public static final String PICTURES = BASE_URI
            + "/users/{user_id}/pictures";
    public static final String PROFILE_PICTURE_URI = BASE_URI
            + "/users/me/pictures/profile";
    public static final String COVER_PICTURE_URI = BASE_URI
            + "/users/me/pictures/cover";

    public static final String MESSAGES_URI = BASE_URI
            + "/users/me/conversations/{user_id}/messages";

    public static final String NOTIFICATIONS_URI = BASE_URI + "/notifications";

    public static final String PROFILE_URI = BASE_URI
            + "/profiles/{profile_id}";

    public static final String MY_LIKE_URI = BASE_URI
            + "/{content_type}/{content_id}/likes/me";

    public static final String COMMENTS_URI = BASE_URI
            + "/{content_type}/{content_id}/comments";

    public static final String VENUE_URI = BASE_URI + "/venues";


    // #define URL_P1_GET_CONVERSATIONS @""URL_P1_BASE"/users/me/conversations"
    // #define URL_P1_GET_CONVERSATION_WITH_USER
    // @""URL_P1_BASE"/users/me/conversations/%i" // Me / userID
    // #define URL_P1_PATCH_CONVERSATION_WITH_USER
    // URL_P1_GET_CONVERSATION_WITH_USER // Me / userID
    // #define URL_P1_USER_DELETE_CONVERSATION_WITH_USER
    // URL_P1_GET_CONVERSATION_WITH_USER
    // #define URL_P1_USER_GET_MESSAGES
    // @""URL_P1_BASE"/users/me/conversations/%i/messages" // Me / userID
    // #define URL_P1_USER_POST_MESSAGE URL_P1_USER_GET_MESSAGES

}
