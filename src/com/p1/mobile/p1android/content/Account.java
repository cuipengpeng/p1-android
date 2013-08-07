package com.p1.mobile.p1android.content;

import android.util.Log;

public class Account extends Content {
    public static final String TYPE = "account";

    private String language;
    private String email;
    private boolean invisible;
    private int welcomeScreenVersion;
    private int unreadNotifications;
    private int unreadMessages;

    protected Account() {
        super("me");
        IOSession = new AccountIOSession();
        Log.d(TAG, "Account created");
    }

    @Override
    public AccountIOSession getIOSession() {
        return (AccountIOSession) super.getIOSession();
    }

    public class AccountIOSession extends ContentIOSession {

        @Override
        public String getType() {
            return TYPE;
        }

        public String getOwnerId() {
            return getId();
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            Account.this.language = language;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            Account.this.email = email;
        }

        public boolean isInvisible() {
            return invisible;
        }

        public void setInvisible(boolean invisible) {
            Account.this.invisible = invisible;
        }

        public int getWelcomeScreenVersion() {
            return welcomeScreenVersion;
        }

        public void setWelcomeScreenVersion(int welcomeScreenVersion) {
            Account.this.welcomeScreenVersion = welcomeScreenVersion;
        }

        public int getUnreadNotifications() {
            return unreadNotifications;
        }

        public void setUnreadNotifications(int unreadNotifications) {
            Account.this.unreadNotifications = unreadNotifications;
        }

        public int getUnreadMessages() {
            return unreadMessages;
        }

        public void setUnreadMessages(int unreadMessages) {
            Account.this.unreadMessages = unreadMessages;
        }

        public void decrementUnreadMessages() {
            if (unreadMessages > 0) {
                unreadMessages--;
            }

        }

    }

}
