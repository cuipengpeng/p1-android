package com.p1.mobile.p1android;

/**
 * 
 * @author Anton
 * 
 *         When trying to login, one of the methods in this interface will be
 *         called
 */
public interface LoginHandler {
    public void onSuccessfulLogin();

    public void onFailedLogin();

    public void onFailedConnection();

    public void onStartMigration(String migrationAccessToken);

    public void onStartActivation(String activationAccessToken);
}