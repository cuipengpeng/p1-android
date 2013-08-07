package com.p1.mobile.p1android.net;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;

import com.google.gson.JsonElement;

public interface Network {
    public JsonElement makeGetRequest(String url, NetResponseListener listener);

    public JsonElement makePostRequest(String url,
            NetResponseListener listener, JsonElement json);

    public JsonElement makePostImageRequest(String url,
            NetResponseListener listener, HttpEntity imageEntity);

    public JsonElement makePutRequest(String url, NetResponseListener listener,
            JsonElement json);

    public JsonElement makePatchRequest(String url,
            NetResponseListener listener, JsonElement json);

    public JsonElement makeDeleteRequest(String url,
            NetResponseListener listener);

    public JsonElement makeBatchRequest(String url,
            NetResponseListener listener, MultipartEntity httpEntity);

    public JsonElement makeHeadRequest(String url, NetResponseListener listener);
}
