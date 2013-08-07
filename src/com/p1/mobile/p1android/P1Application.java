/**
 * P1Application.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.io.model.AuthData;
import com.p1.mobile.p1android.net.Authentication;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * @author Viktor Nyblom
 * 
 */
public class P1Application extends Application {
    private static final String TAG = P1Application.class.getSimpleName();

    // User agent string should be Application version / Device type / OS
    // version / version of any relevant third-party library
    // TODO fix better user agent string
    public static final String USER_AGENT = "P1 Android/"
            + android.os.Build.BRAND + "/" + android.os.Build.VERSION.SDK_INT
            + "/" + android.os.Build.MODEL;
    public static final String AUTHDATA_KEY = "p1authdata";
    public static final String SHARED_PREFS = "p1pref";
    public static final String FLURRY_API_KEY = "GS7DY6NTMQNVY46HJQZQ";

    public static Bitmap tempCameraImage = null;
    private static P1Application me;

    public static Picasso picasso;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate() {
        super.onCreate();
        me = this;

        // Only for development
        // if (Utils.hasHoneycomb()) {
        // Utils.enableStrictMode();
        // }

        initAuthData();

        picasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(this, 100 * 1024 * 1024))
                .memoryCache(new LruCache(20 * 1024 * 1024)).build();
    }

    public static Context getP1ApplicationContext() {
        return me;
    }

    private void initAuthData() {
        NetworkUtilities.setAuthData(readAuthPref());
    }

    public boolean isLoggedIn() {
        return NetworkUtilities.getLoggedInUserId() != null;
    }

    /**
     * Called by UI thread to login and save authentication data into
     * preference.
     * 
     * @param username
     * @param password
     * @param handler
     */
    public void login(final String username, final String password,
            final LoginHandler handler) {
        new AsyncTask<Void, Void, Void>() {
            String migrationAccessToken;
            String activationAccessToken;

            @Override
            protected Void doInBackground(Void... params) {
                AuthData authData = Authentication.authenticate(username,
                        password);
                if (authData != null)
                    writeAuthPref(authData);
                else {
                    migrationAccessToken = Authentication
                            .getMigrationAccessToken(username, password);
                    if (migrationAccessToken == null) {
                        activationAccessToken = Authentication
                                .getActivationAccessToken(username, password);
                        if (activationAccessToken == null) {
                            clearAuthPref();
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (NetworkUtilities.getLoggedInUserId() != null) {
                    Log.d(TAG,
                            "LoginHandler should navigate to successful login");
                    handler.onSuccessfulLogin();
                } else if (migrationAccessToken != null) {
                    Log.d(TAG, "LoginHandler should navigate to migration");
                    handler.onStartMigration(migrationAccessToken);
                } else {
                    Log.d(TAG, "LoginHandler should display failed login");
                    handler.onFailedLogin();
                }
                // if (activationAccessToken != null) {
                // Log.d(TAG, "LoginHandler should navigate to activation");
                // handler.onStartActivation(activationAccessToken);
                // }

            }
        }.execute();
    }

    public void logout() {
        ContentHandler.getInstance().tearDown();
        if (clearAuthPref()) {
            NetworkUtilities.setAuthData(null);
        } else {
            Log.e("P1Application", "failed to clear login");
        }
    }

    public void changeAuthData(AuthData authData) {
        NetworkUtilities.setAuthData(authData);
        writeAuthPref(authData);

    }

    private AuthData readAuthPref() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, 0);
        String json = prefs.getString(AUTHDATA_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, AuthData.class);
        } else {
            return null;
        }
    }

    private boolean writeAuthPref(AuthData authData) {
        Gson gson = new Gson();
        String json = gson.toJson(authData);
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, 0);
        return prefs.edit().putString(AUTHDATA_KEY, json).commit();
    }

    private boolean clearAuthPref() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, 0);
        return prefs.edit().remove(AUTHDATA_KEY).commit();
    }

}
