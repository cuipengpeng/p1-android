package com.p1.mobile.p1android.net;

import com.google.gson.JsonElement;


public interface NetResponseListener {
    public void notify(JsonElement reply);
}
