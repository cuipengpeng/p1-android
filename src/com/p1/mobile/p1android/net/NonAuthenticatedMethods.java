package com.p1.mobile.p1android.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.p1.mobile.p1android.ApplyListener;
import com.p1.mobile.p1android.ForgotPasswordListener;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.parsing.JsonFactory;

public class NonAuthenticatedMethods {
    public static final String TAG = NonAuthenticatedMethods.class
            .getSimpleName();

    public static void applyForAccount(final String givenName,
            final String surname, final String email,
            final ApplyListener listener) {

        new AsyncTask<Void, Void, Void>() {
            boolean success = false;

            @Override
            protected Void doInBackground(Void... params) {

                HttpResponse resp;

                final HttpPost post = new HttpPost(ApiCalls.APPLY_URI);

                post.addHeader(new BasicHeader("Accept", "*/*"));
                post.addHeader(new BasicHeader("Connection", "keep-alive"));
                post.addHeader(new BasicHeader("Accept-Language", "en-us"));
                post.addHeader(new BasicHeader("User-Agent",
                        P1Application.USER_AGENT));
                post.addHeader(new BasicHeader("Content-Type",
                        "application/json"));

                final HttpEntity entity;
                try {
                    String entityString = JsonFactory.createApplyJson(
                            givenName, surname, email).toString();
                    entity = new StringEntity(entityString, HTTP.UTF_8);
                } catch (final UnsupportedEncodingException e) {
                    // this should never happen.
                    throw new IllegalStateException(e);
                }
                Log.i(TAG, "Sending apply to: " + ApiCalls.APPLY_URI);

                post.setEntity(entity);

                try {
                    resp = NetworkUtilities.getHttpClient().execute(post);
                    if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                        Log.d(TAG, "Successfully sent apply");
                        success = true;

                    } else {

                        Log.d(TAG,
                                "Failed to send apply: "
                                        + resp.getStatusLine().getStatusCode()
                                        + " "
                                        + NetworkUtilities.getEntityString(resp
                                                .getEntity()));
                        success = false;

                    }

                } catch (final IOException e) {
                    Log.e(TAG, "IOException when applying", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (listener != null) {
                    if (success) {
                        listener.applySuccessful();
                    } else {
                        listener.applyFailed();
                    }
                } else {
                    Log.w(TAG, "No listener was bound to apply");
                }

            }
        }.execute();
    }

    public static void forgotPassword(final String email,
            final ForgotPasswordListener listener) {
        new AsyncTask<Void, Void, Void>() {
            boolean success = false;

            @Override
            protected Void doInBackground(Void... params) {
                String stringToEncode = NetworkUtilities.APPLICATION_ID + ":"
                        + NetworkUtilities.CODE;
                Log.d(TAG, "String to encode = " + stringToEncode);
                byte[] byteArray = null;
                try {
                    byteArray = stringToEncode.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                String base64 = Base64.encodeToString(byteArray,
                        Base64.URL_SAFE | Base64.NO_WRAP);
                Log.d(TAG, "String after encode = " + base64);
                HttpResponse resp;

                final HttpPost post = new HttpPost(ApiCalls.FORGOT_PASSWORD_URI);

                post.addHeader("Authorization", "Basic " + base64);
                post.addHeader(new BasicHeader("Accept", "*/*"));
                post.addHeader(new BasicHeader("Connection", "keep-alive"));
                post.addHeader(new BasicHeader("Accept-Language", "en-us"));
                post.addHeader(new BasicHeader("User-Agent",
                        P1Application.USER_AGENT));
                post.addHeader(new BasicHeader("Content-Type",
                        "application/x-www-form-urlencoded"));

                final List<NameValuePair> networkParams = new ArrayList<NameValuePair>();
                networkParams.add(new BasicNameValuePair("email", email));

                final HttpEntity entity;
                try {
                    entity = new UrlEncodedFormEntity(networkParams, HTTP.UTF_8);
                } catch (final UnsupportedEncodingException e) {
                    // this should never happen.
                    throw new IllegalStateException(e);
                }
                Log.i(TAG, "Sending forgot password to: "
                        + ApiCalls.FORGOT_PASSWORD_URI);

                post.setEntity(entity);

                try {
                    resp = NetworkUtilities.getHttpClient().execute(post);
                    if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                        Log.d(TAG, "Successfully sent forgot password email");
                        success = true;

                    } else {

                        Log.d(TAG, "Failed to send forgot password email");
                        success = false;

                    }

                } catch (final IOException e) {
                    Log.e(TAG, "IOException when forgetting password", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (listener != null) {
                    if (success) {
                        listener.forgotPasswordSuccessful();
                    } else {
                        listener.forgotPasswordFailed();
                    }
                } else {
                    Log.w(TAG, "No listener was bound to forgotPassword");
                }

            }
        }.execute();
    }

}
