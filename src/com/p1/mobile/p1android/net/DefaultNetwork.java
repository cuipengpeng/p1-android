package com.p1.mobile.p1android.net;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;

import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.background.BackgroundNetworkService;
import com.p1.mobile.p1android.util.PerformanceMeasure;

public class DefaultNetwork implements Network {
    public static final String TAG = DefaultNetwork.class.getSimpleName();

    JsonParser parser = new JsonParser();

    @Override
    public JsonElement makeGetRequest(String url, NetResponseListener listener) {
        Log.d(TAG, "Get url: " + url);
        int measureId = PerformanceMeasure.startMeasure();
        String replyString = NetworkUtilities.getMethod(url);
        if (replyString != null) {
            PerformanceMeasure.endMeasure(measureId, "Network request: " + url
                    + " for " + replyString.length() + " characters");
        } else {
            PerformanceMeasure.endMeasure(measureId,
                    "Network request: null replyString");
        }
        Log.d(TAG, "Get reply: " + replyString);
        JsonElement reply = parser.parse(replyString);

        if (reply != null) {
            Intent successfulNetworkIntent = new Intent(
                    P1Application.getP1ApplicationContext(),
                    BackgroundNetworkService.class);
            P1Application.getP1ApplicationContext().startService(
                    successfulNetworkIntent);
        }

        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

    @Override
    public JsonElement makePostRequest(String url,
            NetResponseListener listener, JsonElement json) {
        int measureId = PerformanceMeasure.startMeasure();
        String sentString = "";
        if (json != null)
            sentString = json.toString();
        String replyString = NetworkUtilities.postMethod(sentString, url);
        int replyLength = 0;
        JsonElement reply = null;
        if (replyString != null) {
            replyLength = replyString.length();
            reply = parser.parse(replyString);

            Intent successfulNetworkIntent = new Intent(
                    P1Application.getP1ApplicationContext(),
                    BackgroundNetworkService.class);
            successfulNetworkIntent.putExtra(
                    BackgroundNetworkService.START_CODE,
                    BackgroundNetworkService.CODE_INCREASE_POLLING);
            P1Application.getP1ApplicationContext().startService(
                    successfulNetworkIntent);

        }


        PerformanceMeasure.endMeasure(measureId, "Network POST request: " + url
                + " for " + replyLength + " characters");
        Log.d(TAG, "Post reply: " + replyString);


        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

    @Override
    public JsonElement makePostImageRequest(String url,
            NetResponseListener listener, HttpEntity imageEntity) {

        int measureId = PerformanceMeasure.startMeasure();
        String replyString = NetworkUtilities.postImageMethod(imageEntity, url);
        PerformanceMeasure.endMeasure(measureId, "Network POST image request: "
                + url + " for " + replyString.length() + " characters");
        Log.d(TAG, "Post image reply: " + replyString);
        JsonElement reply = parser.parse(replyString);

        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

    @Override
    public JsonElement makePatchRequest(String url,
            NetResponseListener listener, JsonElement json) {
        if (url.contains("?")) {
            url = url + "&method=patch";
        } else {
            url = url + "?method=patch";
        }
        int measureId = PerformanceMeasure.startMeasure();
        String sentString = "";
        if (json != null)
            sentString = json.toString();
        String replyString = NetworkUtilities.postMethod(sentString, url);
        int replyLength = 0;
        if (replyString != null) {
            replyLength = replyString.length();
        }
        PerformanceMeasure.endMeasure(measureId, "Network PATCH request: "
                + url + " for " + replyLength + " characters");
        Log.d(TAG, "Patch reply: " + replyString);
        if (replyString == null) {
            return null;
        }
        JsonElement reply = parser.parse(replyString);

        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

    @Override
    public JsonElement makePutRequest(String url, NetResponseListener listener,
            JsonElement json) {
        int measureId = PerformanceMeasure.startMeasure();
        String sentString = "";
        if (json != null)
            sentString = json.toString();
        String replyString = NetworkUtilities.putMethod(sentString, url);
        PerformanceMeasure.endMeasure(measureId, "Network PUT request: " + url
                + " for " + replyString.length() + " characters");
        Log.d(TAG, "Put reply: " + replyString);
        JsonElement reply = parser.parse(replyString);

        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

    @Override
    public JsonElement makeDeleteRequest(String url,
            NetResponseListener listener) {
        int measureId = PerformanceMeasure.startMeasure();
        String replyString = NetworkUtilities.deleteMethod(url);
        PerformanceMeasure.endMeasure(measureId, "Network DELETE request: "
                + url);
        Log.d(TAG, "Delete reply: " + replyString);
        JsonElement reply = null;
        if (replyString != null)
            reply = parser.parse(replyString);

        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

    @Override
    public JsonElement makeHeadRequest(String url, NetResponseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonElement makeBatchRequest(String url,
            NetResponseListener listener, MultipartEntity httpEntity) {

        int measureId = PerformanceMeasure.startMeasure();
        String replyString = NetworkUtilities.postBatchMethod(httpEntity, url);
        PerformanceMeasure.endMeasure(measureId, "Network BATCH request: "
                + url + " for " + replyString.length() + " characters");
        Log.d(TAG, "Batch reply: " + replyString);
        JsonElement reply = parser.parse(replyString);

        if (listener != null) {
            listener.notify(reply);
        }
        return reply;
    }

}
