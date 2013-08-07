/**
 * NetworkUtilities.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.io.model.AuthData;

/**
 * @author Viktor Nyblom
 * 
 */
final public class NetworkUtilities {

    private static final String TAG = "NetworkUtilities";

    private static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    static final String PARAM_GRANT_TYPE = "grant_type";

    public static final String CHARSET = "UTF-8";

    public static final String APPLICATION_ID = "100003";
    public static final String CODE = "ba87fc568a8fa189f621717747b0277f060cdc74";

    static AuthData mAuthData = null;

    private static Network network = new DefaultNetwork();

    private NetworkUtilities() {
    }

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static HttpClient getHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params,
                HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);

        Log.d(TAG, "user agent " + P1Application.USER_AGENT);

        return httpClient;
    }

    private static HttpRequestBase setCommonHeaders(HttpRequestBase request,
            boolean authorization) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "*/*");
        request.addHeader(new BasicHeader("User-Agent",
                P1Application.USER_AGENT));
        if (authorization) {
            request.addHeader("Authorization", mAuthData.access_token);
        }
        return request;
    }

    public static String getMethod(String uri) {
        Log.i(TAG, "Uri: " + uri);
        Log.i(TAG, "Auth: " + mAuthData.access_token);
        final HttpResponse resp;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("format", "json"));

        HttpGet get = new HttpGet(uri);
        get = (HttpGet) setCommonHeaders(get, true);

        try {
            resp = getHttpClient().execute(get);
            String meString = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream istream = (resp.getEntity() != null) ? resp
                        .getEntity().getContent() : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(
                            new InputStreamReader(istream));
                    meString = ireader.readLine().trim();
                    ireader.close();
                }
            }
            if ((meString != null) && (meString.length() > 0)) {
                Log.v(TAG, "Successful me get");
                return meString;
            } else {
                Log.e(TAG, "Error getting me " + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when getting", e);
            return null;
        } finally {
            Log.v(TAG, "getMethod completing");
        }
    }

    public static String postMethod(String value, String uri) {

        Log.i(TAG, "Uri: " + uri);
        final HttpResponse resp;
        HttpPost post = new HttpPost(uri);

        post = (HttpPost) setCommonHeaders(post, true);

        try {
            post.setEntity(new StringEntity(value, CHARSET));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        try {
            resp = getHttpClient().execute(post);
            String meString = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                Log.v(TAG, "Successful post");
                return null;
            }
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    || resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                InputStream istream = (resp.getEntity() != null) ? resp
                        .getEntity().getContent() : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(
                            new InputStreamReader(istream));
                    meString = ireader.readLine().trim();
                    ireader.close();
                }
            }
            if ((meString != null) && (meString.length() > 0)) {
                Log.v(TAG, "Successful post");
                return meString;
            } else {
                Log.e(TAG, "Error posting " + uri + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when posting" + uri, e);
            return null;
        } finally {
            Log.v(TAG, "postMethod completing");
        }
    }

    public static String postImageMethod(HttpEntity entity, String uri) {

        Log.i(TAG, "Uri: " + uri);
        Log.d(TAG, "Posting image of length " + entity.getContentLength());
        final HttpResponse resp;
        HttpPost post = new HttpPost(uri);

        post.addHeader("Content-Type", "image/jpeg");
        post.addHeader("Accept", "*/*");
        post.addHeader(new BasicHeader("User-Agent", P1Application.USER_AGENT));
        post.addHeader("Authorization", mAuthData.access_token);

        post.setEntity(entity);

        try {
            resp = getHttpClient().execute(post);
            String meString = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    || resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                InputStream istream = (resp.getEntity() != null) ? resp
                        .getEntity().getContent() : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(
                            new InputStreamReader(istream));
                    meString = ireader.readLine().trim();
                    ireader.close();
                }
            }
            if ((meString != null) && (meString.length() > 0)) {
                Log.v(TAG, "Successful post");
                return meString;
            } else {
                Log.e(TAG, "Error posting " + uri + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when posting" + uri, e);
            return null;
        } finally {
            Log.v(TAG, "postMethod completing");
        }
    }

    public static String putMethod(String value, String uri) {
        Log.i(TAG, "Uri: " + uri);
        final HttpResponse resp;

        HttpPut put = new HttpPut(uri + "?" + "value=" + value);

        put = (HttpPut) setCommonHeaders(put, true);

        try {
            resp = getHttpClient().execute(put);
            String meString = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream istream = (resp.getEntity() != null) ? resp
                        .getEntity().getContent() : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(
                            new InputStreamReader(istream));
                    meString = ireader.readLine().trim();
                    ireader.close();
                }
            }
            if ((meString != null) && (meString.length() > 0)) {
                Log.v(TAG, "Successful put");
                return meString;
            } else {
                Log.e(TAG,
                        "Error puting " + uri + " Reason: "
                                + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when puting " + uri, e);
            return null;
        } finally {
            Log.v(TAG, "putMethod completing");
        }
    }

    public static String postBatchMethod(MultipartEntity multipartEntity,
            String uri) {
        String boundry = BatchUtil.DEFAULT_BOUNDRY;
        Log.i(TAG, "Uri: " + uri);
        Log.d(TAG,
                "Posting batch of length " + multipartEntity.getContentLength());
        if (!multipartEntity.isRepeatable()) {
            Log.w(TAG, "The multipartEntity of Batch upload is not repeatable");
        }
        final HttpResponse resp;

        HttpPost post = new HttpPost(uri);

        post.addHeader("Content-Type", "multipart/mixed boundary=" + boundry);
        post.addHeader("Accept", "*/*");
        post.addHeader(new BasicHeader("User-Agent", P1Application.USER_AGENT));
        post.addHeader("Authorization", mAuthData.access_token);
        for (Header h : post.getAllHeaders()) {
            Log.d(TAG, "Header: " + h);
        }

        post.setEntity(multipartEntity);

        try {
            resp = getHttpClient().execute(post);
            String meString = null;
            for (Header h : resp.getAllHeaders()) {
                Log.d(TAG, "Response header: " + h);
            }
            Log.d(TAG, "Response code: " + resp.getStatusLine().getStatusCode());
            InputStream istream = (resp.getEntity() != null) ? resp.getEntity()
                    .getContent() : null;
            if (istream != null) {
                BufferedReader ireader = new BufferedReader(
                        new InputStreamReader(istream));
                meString = ireader.readLine().trim();
                ireader.close();
            }
            if ((resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK || resp
                    .getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
                    && (meString != null) && (meString.length() > 0)) {
                Log.v(TAG, "Successful batch post");
                return meString;
            } else {
                Log.e(TAG,
                        "Error posting batch " + uri + " Reason: "
                                + resp.getStatusLine());
                return meString;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when posting batch " + uri, e);
            return null;
        } finally {
            Log.v(TAG, "batchMethod completing");
        }
    }

    public static String deleteMethod(String uri) {

        Log.i(TAG, "Uri: " + uri);
        final HttpResponse resp;

        HttpDelete delete = new HttpDelete(uri);

        delete = (HttpDelete) setCommonHeaders(delete, true);

        try {
            resp = getHttpClient().execute(delete);
            String meString = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream istream = (resp.getEntity() != null) ? resp
                        .getEntity().getContent() : null;
                if (istream != null) {
                    BufferedReader ireader = new BufferedReader(
                            new InputStreamReader(istream));
                    meString = ireader.readLine().trim();
                    ireader.close();
                }
            }
            if ((meString != null)
                    && (meString.length() > 0)
                    || resp.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                Log.v(TAG, "Successful delete");
                return meString;
            } else {
                Log.e(TAG, "Error deleting " + uri + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when deleting" + uri, e);
            return null;
        } finally {
            Log.v(TAG, "deleteMethod completing");
        }
    }

    /**
     * Temporary fix for images
     * 
     * @return
     */
    public static String getAuth() {
        return mAuthData.access_token;
    }

    /**
     * Before the first network request is completed, this method will return
     * null.
     * 
     * @return
     */
    public static String getLoggedInUserId() {
        if (mAuthData != null)
            return mAuthData.user_id;
        Log.w(TAG, "No logged in user id found");
        return null;
    }

    /**
     * Before the first network request is completed, this method will return
     * 101.
     * 
     * @return
     */
    public static String getSafeLoggedInUserId() {
        if (mAuthData != null)
            return mAuthData.user_id;
        Log.e(TAG, "No logged in user id found, returning hardcoded 101");
        return "101";
    }

    public static Network getNetwork() {
        return network;
    }

    public static void setAuthData(AuthData data) {
        mAuthData = data;
    }

    public static void clearAuthData() {
        mAuthData = null;
    }

    public static String getEntityString(HttpEntity entity) throws IOException {
        String entityString = null;
        InputStream istream = (entity != null) ? entity.getContent() : null;
        if (istream != null) {
            BufferedReader ireader = new BufferedReader(new InputStreamReader(
                    istream));
            entityString = ireader.readLine().trim();
            ireader.close();
        }
        return entityString;
    }
}
