package com.p1.mobile.p1android.content;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.os.Handler;
import android.util.Log;

import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IhasTimers;
import com.p1.mobile.p1android.content.logic.FakeIdTracker;
import com.p1.mobile.p1android.util.PerformanceMeasure;

public class ContentHandler {
    public static final String TAG = ContentHandler.class.getSimpleName();

    private static ContentHandler me;

    private LooperThread networkThread;
    private LooperThread lowPriorityetworkThread;

    private Queue<Runnable> failedNetworkRequests = new LinkedList<Runnable>();

    private FakeIdTracker fakeIdTracker = new FakeIdTracker();

    private Hashtable<String, User> users = new Hashtable<String, User>();
    private Hashtable<String, UserPicturesList> userPicturesLists = new Hashtable<String, UserPicturesList>();
    private Hashtable<String, Picture> pictures = new Hashtable<String, Picture>();
    private Hashtable<String, Member> members = new Hashtable<String, Member>();
    private Hashtable<String, FollowList> followingLists = new Hashtable<String, FollowList>();
    private Hashtable<String, FollowList> followersLists = new Hashtable<String, FollowList>();
    private Hashtable<String, Conversation> conversations = new Hashtable<String, Conversation>();
    private Hashtable<String, Message> messages = new Hashtable<String, Message>();
    private Hashtable<String, NotificationStory> notifications = new Hashtable<String, NotificationStory>();
    private Hashtable<String, Share> shares = new Hashtable<String, Share>();
    private Hashtable<String, Comment> comments = new Hashtable<String, Comment>();
    private Hashtable<String, Profile> profiles = new Hashtable<String, Profile>();
    private Hashtable<String, VenueList> venueLists = new Hashtable<String, VenueList>();
    private Hashtable<String, Venue> venues = new Hashtable<String, Venue>();

    private BrowseList browsePicturesList;
    private BrowseList browseMembersList;
    private ConversationList conversationList;
    private NotificationList notificationList;
    private Feed feed;
    private Account account;

    private Hashtable<IContentRequester, List<Content>> requesterTracker = new Hashtable<IContentRequester, List<Content>>();

    public synchronized static ContentHandler getInstance() {
        if (me == null) {
            Log.d(TAG, "Created");
            me = new ContentHandler();
        }
        return me;
    }

    private ContentHandler() {
        networkThread = new LooperThread();
        networkThread.start();
        lowPriorityetworkThread = new LooperThread();
        lowPriorityetworkThread.start();
    }

    public Handler getNetworkHandler() {
        return networkThread.getHandler();
    }

    public Handler getLowPriorityNetworkHandler() {
        return lowPriorityetworkThread.getHandler();
    }

    public FakeIdTracker getFakeIdTracker() {
        return fakeIdTracker;
    }

    private void track(IContentRequester requester, Content content) {
        if (requester != null) {

            if (!requesterTracker.containsKey(requester)) {
                requesterTracker.put(requester, new ArrayList<Content>());
            }
            requesterTracker.get(requester).add(content);
            content.addListener(requester);
            Log.d(TAG, "Tracking new object. Currently tracking "
                    + requesterTracker.size()
                    + " requesters. Requester is type "
                    + requester.getClass().getSimpleName());
        }
    }

    public void removeRequester(IContentRequester requester) {
        if (requester != null && requesterTracker.get(requester) != null) {
            if (requester instanceof IChildContentRequester) {
                ((IContentRequester.IChildContentRequester) requester)
                        .removeChildRequestors();
            }

            if (requester instanceof IhasTimers) {    // if have timing refresh timers, remove them
                ((IContentRequester.IhasTimers) requester)
                        .removetimer();
            }

            for (Content content : requesterTracker.get(requester)) {
                content.removeListener(requester);
            }
            requesterTracker.remove(requester);
            Log.d(TAG, "Removed tracked requester. Currently tracking "
                    + requesterTracker.size()
                    + " requesters. Requester is type "
                    + requester.getClass().getSimpleName());
        }
    }

    /**
     * Gets the current memory representation of a specific User. Returned User
     * may not be valid (Contains only Id and Type). IContentRequesters should
     * not call this function directly, but instead use methods of ReadContent.
     * 
     * @param id
     * @param requester
     *            may be null
     * @return A User Content object with at least Id and type.
     */
    public User getUser(String id, IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (users) {
            User user = users.get(id);
            if (user == null) {
                user = new User(id);
                users.put(id, user);
            }
            track(requester, user);
            PerformanceMeasure.endMeasure(measureId, "ContentHandler getUser");
            return user;
        }
    }

    public UserPicturesList getUserPicturesList(String id,
            IContentRequester requester) {
        synchronized (userPicturesLists) {
            UserPicturesList userPicturesList = userPicturesLists.get(id);
            if (userPicturesList == null) {
                userPicturesList = new UserPicturesList(id);
                userPicturesLists.put(id, userPicturesList);
            }
            track(requester, userPicturesList);
            return userPicturesList;
        }
    }

    public Picture getPicture(String id, IContentRequester requester) {
        synchronized (pictures) {
            Picture picture = pictures.get(id);
            if (picture == null) {
                picture = new Picture(id);
                pictures.put(id, picture);
            }
            track(requester, picture);
            return picture;
        }
    }

    public Member getMember(String id, IContentRequester requester) {
        synchronized (members) {
            Member member = members.get(id);
            if (member == null) {
                member = new Member(id);
                members.put(id, member);
            }
            track(requester, member);
            return member;
        }
    }

    public FollowList getFollowing(String id, IContentRequester requester) {
        synchronized (followingLists) {
            FollowList following = followingLists.get(id);
            if (following == null) {
                following = new FollowList(id, FollowList.FOLLOWING);
                followingLists.put(id, following);
            }
            track(requester, following);
            return following;
        }
    }

    public FollowList getFollowers(String id, IContentRequester requester) {
        synchronized (followersLists) {
            FollowList followers = followersLists.get(id);
            if (followers == null) {
                followers = new FollowList(id, FollowList.FOLLOWERS);
                followersLists.put(id, followers);
            }
            track(requester, followers);
            return followers;
        }
    }

    public BrowseList getBrowsePicturesList(IContentRequester requester) {
        synchronized (me) {
            if (browsePicturesList == null) {
                browsePicturesList = new BrowseList(false);
            }
            track(requester, browsePicturesList);
            return browsePicturesList;
        }
    }

    public BrowseList getBrowseMembersList(IContentRequester requester) {
        synchronized (me) {
            if (browseMembersList == null) {
                browseMembersList = new BrowseList(true);
            }
            track(requester, browseMembersList);
            return browseMembersList;
        }
    }

    public Conversation getConversation(String id, IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (conversations) {
            Conversation conversation = conversations.get(id);
            if (conversation == null) {
                conversation = new Conversation(id);
                conversations.put(id, conversation);
            }
            track(requester, conversation);
            PerformanceMeasure.endMeasure(measureId,
                    "ContentHandler getConversation");
            return conversation;
        }
    }

    public ConversationList getConversationList(IContentRequester requester) {
        synchronized (me) {
            if (conversationList == null) {
                conversationList = new ConversationList();
            }
            track(requester, conversationList);
            return conversationList;
        }
    }

    public Message getMessage(String id, IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (messages) {
            Message message = messages.get(id);
            if (message == null) {
                message = new Message(id);
                messages.put(id, message);
            }
            track(requester, message);
            PerformanceMeasure.endMeasure(measureId,
                    "ContentHandler getMessage");
            return message;
        }
    }

    public NotificationStory getNotification(String id,
            IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (notifications) {
            NotificationStory notification = notifications.get(id);
            if (notification == null) {
                notification = new NotificationStory(id);
                notifications.put(id, notification);
            }
            track(requester, notification);
            PerformanceMeasure.endMeasure(measureId,
                    "ContentHandler getNotification");
            return notification;
        }
    }

    public NotificationList getNotificationList(IContentRequester requester) {
        synchronized (me) {
            if (notificationList == null) {
                notificationList = new NotificationList();
            }
            track(requester, notificationList);
            return notificationList;
        }
    }

    public Feed getFeed(IContentRequester requester) {
        synchronized (me) {
            if (feed == null) {
                feed = new Feed();
            }
            track(requester, feed);
            return feed;
        }
    }

    public Account getAccount(IContentRequester requester) {
        synchronized (me) {
            if (account == null) {
                account = new Account();
            }
            track(requester, account);
            return account;
        }
    }

    public Share getShare(String id, IContentRequester requester) {
        synchronized (shares) {
            Share share = shares.get(id);
            if (share == null) {
                share = new Share(id);
                shares.put(id, share);
            }
            track(requester, share);
            return share;
        }
    }

    public Comment getComment(String id, IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (comments) {
            Comment comment = comments.get(id);
            if (comment == null) {
                comment = new Comment(id);
                comments.put(id, comment);
            }
            track(requester, comment);
            PerformanceMeasure.endMeasure(measureId,
                    "ContentHandler getComment");
            return comment;
        }
    }

    public VenueList getVenueList(String id, IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (venueLists) {
            VenueList venueList = venueLists.get(id);
            if (venueList == null) {
                venueList = new VenueList(id);
                venueLists.put(id, venueList);
            }
            track(requester, venueList);
            PerformanceMeasure.endMeasure(measureId,
                    "ContentHandler getVenueList");
            return venueList;
        }
    }

    public Venue getVenue(String id, IContentRequester requester) {
        int measureId = PerformanceMeasure.startMeasure();
        synchronized (venues) {
            Venue venue = venues.get(id);
            if (venue == null) {
                venue = new Venue(id);
                venues.put(id, venue);
            }
            track(requester, venue);
            PerformanceMeasure.endMeasure(measureId, "ContentHandler getVenue");
            return venue;
        }
    }

    /**
     * If a user with the new id already exists, that User will be removed. The
     * overwritten User will linger in some view pointers, but will eventually
     * be completely replaced by the User having the previous Id
     * 
     * @param previousUserId
     * @param newUserId
     */
    public void changeUserId(String previousUserId, String newUserId) {
        changeId(users, previousUserId, newUserId);
    }

    public void changeMessageId(String previousMessageId, String newMessageId) {
        changeId(messages, previousMessageId,
                newMessageId);
    }

    public void changeShareId(String previousShareId, String newShareId) {
        changeId(shares, previousShareId, newShareId);
    }

    public void changePictureId(String previousPictureId, String newPictureId) {
        changeId(pictures, previousPictureId, newPictureId);
    }

    public void changeCommentId(String previousCommentId, String newCommentId) {
        changeId(comments, previousCommentId, newCommentId);
    }

    private <T extends Content> void changeId(
            Hashtable<String, T> affectedTable,
            String previousId, String newId) {
        synchronized (affectedTable) {
            T changedContent = affectedTable.get(previousId);
            T overwrittenContent = affectedTable.get(newId);
            if (overwrittenContent != null) {
                for (IContentRequester requester : overwrittenContent
                        .getListeners()) {
                    track(requester, changedContent);
                }
            }
            affectedTable.put(newId, changedContent);
            Log.d(TAG, "Id changed from " + previousId + " to " + newId);
        }
    }

    /**
     * Clears all cached information.
     */
    public void tearDown() {
        // A new instance of the ContentHandler will be created, and the old one
        // will be garbage collected and lost.
        me = null;
        // TODO Be more selective when it comes to which information to discard
        // while keeping the same ContentHandler object.
    }

    public Queue<Runnable> getFailedNetworkRequests() {
        return failedNetworkRequests;
    }

    public Profile getProfile(String id, IContentRequester requester) {
        synchronized (profiles) {
            Profile profile = profiles.get(id);
            if (profile == null) {
                profile = new Profile(id);
                profiles.put(id, profile);
            }

            track(requester, profile);
            return profile;

        }
    }
}
