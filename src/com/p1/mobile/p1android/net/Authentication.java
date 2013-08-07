package com.p1.mobile.p1android.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.io.model.AuthData;

public class Authentication {
    private static final String TAG = Authentication.class.getSimpleName();

    /**
     * Connects to the P1 test server, authenticates the provided username and
     * password.
     * 
     * @param username
     *            The server account username
     * @param password
     *            The server account password
     * @return String The authentication token returned by the server (or null)
     */
    public static AuthData authenticate(String username, String password) {
        if (NetworkUtilities.mAuthData != null) {
            return NetworkUtilities.mAuthData;
        }

        Log.d(TAG, "UserName= " + username);
        Log.d(TAG, "Password= " + password);
        String stringToEncode = NetworkUtilities.APPLICATION_ID + ":"
                + NetworkUtilities.CODE;
        Log.d(TAG, "String to encode = " + stringToEncode);
        byte[] byteArray = null;
        try {
            byteArray = stringToEncode.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String base64 = Base64.encodeToString(byteArray, Base64.URL_SAFE
                | Base64.NO_WRAP);
        Log.d(TAG, "String after encode = " + base64);
        HttpResponse resp;

        final HttpPost post = new HttpPost(ApiCalls.AUTH_URI);

        post.addHeader("Authorization", "Basic " + base64);
        post.addHeader(new BasicHeader("Accept", "*/*"));
        post.addHeader(new BasicHeader("Connection", "keep-alive"));
        post.addHeader(new BasicHeader("Accept-Language", "en-us"));
        post.addHeader(new BasicHeader("User-Agent", P1Application.USER_AGENT));
        post.addHeader(new BasicHeader("Content-Type",
                "application/x-www-form-urlencoded"));

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_USERNAME,
                username));
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_PASSWORD,
                password));
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_GRANT_TYPE,
                "password"));

        final HttpEntity entity;
        try {
            entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
        } catch (final UnsupportedEncodingException e) {
            // this should never happen.
            throw new IllegalStateException(e);
        }
        for (NameValuePair param : params)
            Log.i(TAG, param.getName() + " " + param.getValue());
        Log.i(TAG, "Authenticating to: " + ApiCalls.AUTH_URI);

        post.setEntity(entity);

        try {
            resp = NetworkUtilities.getHttpClient().execute(post);
            String authString = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                authString = NetworkUtilities.getEntityString(resp.getEntity());

                Log.d(TAG, "Normal login gave correct response");
            } else {
                Log.d(TAG, "Failed normal login, trying P1.cn login");
                post.setURI(new URI(ApiCalls.CN_AUTH_URI));
                resp = NetworkUtilities.getHttpClient().execute(post);
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                    authString = NetworkUtilities.getEntityString(resp
                            .getEntity());

                    Log.d(TAG, "P1.cn login gave correct response");
                } else {
                    Log.w(TAG, "Failed P1.cn login");
                }
            }
            if ((authString != null) && (authString.length() > 0)) {
                Gson gson = new Gson();
                NetworkUtilities.mAuthData = gson.fromJson(authString,
                        AuthData.class);
                Log.d(TAG, NetworkUtilities.mAuthData.access_token);
                return NetworkUtilities.mAuthData;
            } else {
                Log.e(TAG, "Error authenticating" + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when getting authtoken", e);
            return null;
        } catch (URISyntaxException e) {
            Log.e(TAG, "The P1.cn uri " + ApiCalls.CN_AUTH_URI
                    + " is not a valid uri");
            e.printStackTrace();
            return null;
        } finally {
            Log.v(TAG, "getAuthtoken completing");
        }
    }

    private static String extractAccessToken(String authString) {
        Log.d(TAG, "Extracting access token from: " + authString);
        JsonParser parser = new JsonParser();
        JsonObject authJson = parser.parse(authString).getAsJsonObject();
        String accessToken = null;
        if (authJson.has("p1cn_access_token")) {
            accessToken = authJson.get("p1cn_access_token").getAsString();
        } else if (authJson.has("activation_access_token")) {
            accessToken = authJson.get("activation_access_token").getAsString();
        }

        if (accessToken == null || accessToken.equals("")) {
            throw new InvalidParameterException(
                    "Failed extracting access token");
        }
        return accessToken;

    }

    public static String getMigrationAccessToken(String username,
            String password) {

        String stringToEncode = NetworkUtilities.APPLICATION_ID + ":"
                + NetworkUtilities.CODE;
        Log.d(TAG, "String to encode = " + stringToEncode);
        byte[] byteArray = null;
        try {
            byteArray = stringToEncode.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String base64 = Base64.encodeToString(byteArray, Base64.URL_SAFE
                | Base64.NO_WRAP);
        Log.d(TAG, "String after encode = " + base64);
        HttpResponse resp;

        final HttpPost post = new HttpPost(ApiCalls.MIGRATION_URI);

        post.addHeader("Authorization", "Basic " + base64);
        post.addHeader(new BasicHeader("Accept", "*/*"));
        post.addHeader(new BasicHeader("Connection", "keep-alive"));
        post.addHeader(new BasicHeader("Accept-Language", "en-us"));
        post.addHeader(new BasicHeader("User-Agent", P1Application.USER_AGENT));
        post.addHeader(new BasicHeader("Content-Type",
                "application/x-www-form-urlencoded"));

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_USERNAME,
                username));
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_PASSWORD,
                password));
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_GRANT_TYPE,
                "p1cn"));

        final HttpEntity entity;
        try {
            entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
        } catch (final UnsupportedEncodingException e) {
            // this should never happen.
            throw new IllegalStateException(e);
        }
        Log.i(TAG, "Checking migration for: " + ApiCalls.MIGRATION_URI);

        post.setEntity(entity);

        try {
            resp = NetworkUtilities.getHttpClient().execute(post);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(TAG, "Detected account migration!");

                String authString = NetworkUtilities.getEntityString(resp
                        .getEntity());

                return extractAccessToken(authString);
            } else {
                return null;
            }

        } catch (final IOException e) {
            Log.e(TAG, "IOException when checking migration", e);
            return null;
        } finally {
            Log.v(TAG, "detectMigrationAccount completing");
        }
    }

    public static String getActivationAccessToken(String username,
            String password) {

        String stringToEncode = NetworkUtilities.APPLICATION_ID + ":"
                + NetworkUtilities.CODE;
        Log.d(TAG, "String to encode = " + stringToEncode);
        byte[] byteArray = null;
        try {
            byteArray = stringToEncode.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String base64 = Base64.encodeToString(byteArray, Base64.URL_SAFE
                | Base64.NO_WRAP);
        Log.d(TAG, "String after encode = " + base64);
        HttpResponse resp;

        final HttpPost post = new HttpPost(ApiCalls.ACTIVATION_URI);

        post.addHeader("Authorization", "Basic " + base64);
        post.addHeader(new BasicHeader("Accept", "*/*"));
        post.addHeader(new BasicHeader("Connection", "keep-alive"));
        post.addHeader(new BasicHeader("Accept-Language", "en-us"));
        post.addHeader(new BasicHeader("User-Agent", P1Application.USER_AGENT));
        post.addHeader(new BasicHeader("Content-Type",
                "application/x-www-form-urlencoded"));

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_USERNAME,
                username));
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_PASSWORD,
                password));
        params.add(new BasicNameValuePair(NetworkUtilities.PARAM_GRANT_TYPE,
                "activation"));

        final HttpEntity entity;
        try {
            entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
        } catch (final UnsupportedEncodingException e) {
            // this should never happen.
            throw new IllegalStateException(e);
        }
        Log.i(TAG, "Checking activation for: " + ApiCalls.ACTIVATION_URI);

        post.setEntity(entity);

        try {
            resp = NetworkUtilities.getHttpClient().execute(post);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(TAG, "Detected account activation!");

                String authString = NetworkUtilities.getEntityString(resp
                        .getEntity());

                return extractAccessToken(authString);
            } else {
                return null;
            }

        } catch (final IOException e) {
            Log.e(TAG, "IOException when checking activation", e);
            return null;
        } finally {
            Log.v(TAG, "detectMigrationAccount completing");
        }
    }

}
