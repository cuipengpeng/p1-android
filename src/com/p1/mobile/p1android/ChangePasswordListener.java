package com.p1.mobile.p1android;

public interface ChangePasswordListener {

    public enum FailureReason {
        TOO_SHORT_PASSWORD, WRONG_OLD_PASSWORD, NETWORK_FAILURE
    }

    public void passwordChangeSuccessful();

    public void passwordChangeFailed(FailureReason reason);

}
