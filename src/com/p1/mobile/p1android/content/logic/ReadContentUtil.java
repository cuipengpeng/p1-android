package com.p1.mobile.p1android.content.logic;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IdTypePair;
import com.p1.mobile.p1android.content.Member;
import com.p1.mobile.p1android.content.Message;
import com.p1.mobile.p1android.content.NotificationStory;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.Venue;
import com.p1.mobile.p1android.content.parsing.CommentParser;
import com.p1.mobile.p1android.content.parsing.ConversationParser;
import com.p1.mobile.p1android.content.parsing.MemberParser;
import com.p1.mobile.p1android.content.parsing.MessageParser;
import com.p1.mobile.p1android.content.parsing.NotificationParser;
import com.p1.mobile.p1android.content.parsing.PictureParser;
import com.p1.mobile.p1android.content.parsing.ProfileParser;
import com.p1.mobile.p1android.content.parsing.ShareParser;
import com.p1.mobile.p1android.content.parsing.UserParser;
import com.p1.mobile.p1android.content.parsing.VenueParser;
import com.p1.mobile.p1android.net.NetRequestFactory;

/**
 * 
 * @author Anton
 * 
 *         request***(id, requester) methods are the standard way of retrieving
 *         any information.
 */
public class ReadContentUtil {

    public static final String TAG = ReadContentUtil.class.getSimpleName();

    static NetRequestFactory netFactory = new NetRequestFactory();

    public static Content requestContent(IdTypePair idType,
            IContentRequester requester) {

        throw new UnsupportedOperationException("not yet implemented");
    }

    static void saveExtraUsers(JsonArray userArray) {
        Iterator<JsonElement> iterator = userArray.iterator();
        while (iterator.hasNext()) {
            JsonObject userJson = iterator.next().getAsJsonObject();
            String userId = userJson.get("id").getAsString();
            User user = ContentHandler.getInstance().getUser(userId, null);
            UserParser.parseToUser(userJson, user);
            user.notifyListeners();
        }
    }

    static void saveExtraPictures(JsonArray pictureArray) {
        Iterator<JsonElement> iterator = pictureArray.iterator();
        while (iterator.hasNext()) {
            JsonObject pictureJson = iterator.next().getAsJsonObject();
            String pictureId = pictureJson.get("id").getAsString();
            Picture picture = ContentHandler.getInstance().getPicture(
                    pictureId, null);
            PictureParser.parseToPicture(pictureJson, picture);
            picture.notifyListeners();
        }
    }

    static void saveExtraMembers(JsonArray memberArray) {
        Iterator<JsonElement> iterator = memberArray.iterator();
        while (iterator.hasNext()) {
            JsonObject memberJson = iterator.next().getAsJsonObject();
            String memberId = memberJson.get("id").getAsString();
            Member member = ContentHandler.getInstance().getMember(memberId,
                    null);
            MemberParser.parseToMember(memberJson, member);
            member.notifyListeners();
        }
    }

    static void saveExtraMessages(JsonArray messageArray) {
        Iterator<JsonElement> iterator = messageArray.iterator();
        while (iterator.hasNext()) {
            JsonObject messageJson = iterator.next().getAsJsonObject();
            String messageId = messageJson.get("id").getAsString();
            Message message = ContentHandler.getInstance().getMessage(
                    messageId, null);
            MessageParser.parseMessage(messageJson, message);
            message.notifyListeners();
        }
    }

    static void saveExtraConversations(JsonArray conversationArray) {
        Iterator<JsonElement> iterator = conversationArray.iterator();
        while (iterator.hasNext()) {
            JsonObject conversationJson = iterator.next().getAsJsonObject();
            String conversationId = conversationJson.get("id").getAsString();
            Conversation conversation = ContentHandler.getInstance()
                    .getConversation(conversationId, null);
            ConversationParser.parseToConversation(conversationJson,
                    conversation);
            conversation.notifyListeners();
        }
    }

    static void saveExtraNotifications(JsonArray notificationArray) {
        Iterator<JsonElement> iterator = notificationArray.iterator();
        while (iterator.hasNext()) {
            JsonObject notificationJson = iterator.next().getAsJsonObject();
            String notificationId = notificationJson.get("id").getAsString();
            NotificationStory notification = ContentHandler.getInstance()
                    .getNotification(notificationId, null);
            NotificationParser.parseToNotification(notificationJson,
                    notification);
            notification.notifyListeners();
        }
    }

    static void saveExtraShares(JsonArray shareArray) {
        Iterator<JsonElement> iterator = shareArray.iterator();
        while (iterator.hasNext()) {
            JsonObject shareJson = iterator.next().getAsJsonObject();
            String shareId = shareJson.get("id").getAsString();
            Share share = ContentHandler.getInstance().getShare(shareId, null);
            ShareParser.parseToShare(shareJson, share);
            share.notifyListeners();
        }
    }

    static void saveExtraComments(JsonArray commentArray) {
        Iterator<JsonElement> iterator = commentArray.iterator();
        while (iterator.hasNext()) {
            JsonObject commentJson = iterator.next().getAsJsonObject();
            String commentId = commentJson.get("id").getAsString();
            Comment comment = ContentHandler.getInstance().getComment(
                    commentId, null);
            CommentParser.parseToComment(commentJson, comment);
            comment.notifyListeners();
        }
    }

    static void saveExtraProfiles(JsonArray profileArray) {
        Iterator<JsonElement> iterator = profileArray.iterator();
        while (iterator.hasNext()) {
            JsonObject profileJson = iterator.next().getAsJsonObject();
            String profileId = profileJson.get("id").getAsString();
            Profile profile = ContentHandler.getInstance().getProfile(
                    profileId, null);
            ProfileParser.parseToProfile(profileJson, profile);
            profile.notifyListeners();
        }
    }

    static void saveExtraVenues(JsonArray venueArray) {
        Iterator<JsonElement> iterator = venueArray.iterator();
        while (iterator.hasNext()) {
            JsonObject venueJson = iterator.next().getAsJsonObject();
            String venueId = venueJson.get("id").getAsString();
            Venue venue = ContentHandler.getInstance().getVenue(venueId,
                    null);
            VenueParser.parseVenue(venueJson, venue);
            venue.notifyListeners();
        }
    }

}
