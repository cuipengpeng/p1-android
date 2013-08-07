package com.p1.mobile.p1android.content.logic;

import java.util.Iterator;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.p1.mobile.p1android.ChangePasswordListener;
import com.p1.mobile.p1android.ChangePasswordListener.FailureReason;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.parsing.AccountParser;
import com.p1.mobile.p1android.content.parsing.JsonFactory;
import com.p1.mobile.p1android.io.model.AuthData;
import com.p1.mobile.p1android.net.ApiCalls;
import com.p1.mobile.p1android.net.Network;
import com.p1.mobile.p1android.net.NetworkUtilities;

/**
 * 
 * @author Anton
 * 
 */
public class WriteAccount {
    public static final String TAG = WriteAccount.class.getSimpleName();

    public static void changeEmail(Account account, String email) {
        AccountIOSession io = account.getIOSession();
        try {

            io.setEmail(email);
            io.incrementUnfinishedUserModifications();

        } finally {
            io.close();
        }
        account.notifyListeners();

        sendAccount(account);
    }

    public static void changeInvisibility(Account account, boolean invisible) {
        AccountIOSession io = account.getIOSession();
        try {
            io.setInvisible(invisible);
            io.incrementUnfinishedUserModifications();

        } finally {
            io.close();
        }
        account.notifyListeners();

        sendAccount(account);
    }

    public static void changeWelcomeScreenVersion(Account account,
            int welcomeScreenVersion) {
        AccountIOSession io = account.getIOSession();
        try {
            io.setWelcomeScreenVersion(welcomeScreenVersion);
            io.incrementUnfinishedUserModifications();

        } finally {
            io.close();
        }
        account.notifyListeners();

        sendAccount(account);
    }

    private static void sendAccount(final Account account) {

        ContentHandler.getInstance().getNetworkHandler().post(new Runnable() {
            @Override
            public void run() {
                String accountRequest = ReadContentUtil.netFactory
                        .createAccountRequest();

                try {
                    Network network = NetworkUtilities.getNetwork();

                    JsonObject jsonResponse = network.makePatchRequest(
                            accountRequest, null,
                            AccountParser.serializeAccount(account))
                            .getAsJsonObject();
                    Log.d(TAG, "Account response: " + jsonResponse);

                    JsonObject data = jsonResponse.getAsJsonObject("data");

                    JsonArray accountsArray = data.getAsJsonArray("accounts");
                    Iterator<JsonElement> iterator = accountsArray.iterator();
                    if (iterator.hasNext()) { // Will save the
                                              // single returned
                                              // account
                        JsonObject accountJson = iterator.next()
                                .getAsJsonObject();
                        AccountParser.parseToAccount(accountJson, account);

                    }

                    account.notifyListeners();

                    Log.d(TAG, "Account modification successful");
                } catch (Exception e) {
                    Log.e(TAG, "Failed modifying account", e);
                } finally {
                    AccountIOSession io = account.getIOSession();
                    try {
                        io.decrementUnfinishedUserModifications();
                    } finally {
                        io.close();
                    }
                }
            }
        });
    }

    public static void changePassword(final String oldPassword,
            final String newPassword, final ChangePasswordListener listener,
            final P1Application application) {
        if (newPassword.length() < 6) {
            listener.passwordChangeFailed(FailureReason.TOO_SHORT_PASSWORD);
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            FailureReason failureReason = FailureReason.NETWORK_FAILURE;

            @Override
            protected Void doInBackground(Void... params) {
                Network network = NetworkUtilities.getNetwork();
                String changePasswordRequest = ApiCalls.NEW_PASSWORD_URI;
                JsonElement changePasswordJson = JsonFactory
                        .createChangePasswordJson(oldPassword, newPassword);
                JsonElement jsonResponseElement = network.makePostRequest(
                        changePasswordRequest, null, changePasswordJson);
                if (jsonResponseElement == null) {
                    failureReason = FailureReason.NETWORK_FAILURE;
                    return null;
                }
                JsonObject jsonResponse = jsonResponseElement.getAsJsonObject();
                if (!jsonResponse.has("access_token")) {
                    String responseMessage = jsonResponse.get("message").getAsString();
                    if(responseMessage.equalsIgnoreCase("incorrect old password")){
                        failureReason = FailureReason.WRONG_OLD_PASSWORD;
                    } else{
                        Log.w(TAG, "Unknown failure reason: " + responseMessage);
                        failureReason = FailureReason.NETWORK_FAILURE;
                    }
                    return null;
                }

                Log.d(TAG, "Successfully set new password");
                Gson gson = new Gson();
                application.changeAuthData(gson.fromJson(jsonResponse,
                        AuthData.class));
                failureReason = null;

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (listener != null) {
                    if (failureReason == null) {
                        listener.passwordChangeSuccessful();
                    } else {
                        listener.passwordChangeFailed(failureReason);
                    }
                } else {
                    Log.w(TAG, "No listener was bound to changePassword");
                }

            }
        }.execute();
    }
}
