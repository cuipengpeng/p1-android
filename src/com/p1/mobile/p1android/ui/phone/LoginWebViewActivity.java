package com.p1.mobile.p1android.ui.phone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * 
 * @author pirriperdos
 * 
 */
public class LoginWebViewActivity extends FlurryActivity {
    public static final String TAG = LoginWebViewActivity.class.getSimpleName();

    private WebView mWebView;
    public static final String URL_KEY = "url";
    private String mUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(URL_KEY);
        mWebView = new WebView(this);
        setContentView(mWebView);
        mWebView.setWebViewClient(new Client());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl(mUrl);
        // mWebView.loadUrl("file:///android_asset/webViewTest.html");
        mWebView.addJavascriptInterface(this, "android");
        Log.d("d", "loading url" + mUrl);
        mWebView.requestFocus();
    }

    @android.webkit.JavascriptInterface
    public void success(String json) {
        setResult(RESULT_OK);
        Log.d("TAG", "All went well! Returned json is: " + json);
        Toast.makeText(getApplicationContext(),
                "Successfully called a method from the web view",
                Toast.LENGTH_LONG).show();

        // TODO read "AccessToken" from returned json and set it to the app.

        finish();
    }

    private static class Client extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
