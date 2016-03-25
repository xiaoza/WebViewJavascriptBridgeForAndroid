package cn.xianging.jsbridge;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by xiaoz on 16/3/10.
 */
public class WebViewJsBridgeClient extends WebViewClient {

    private WebViewJsBridge bridge;
    private WebViewJsBridgeBase base;

    // 接受自定义client
    private WebViewClient mWebViewClient;

    public WebViewJsBridgeClient(WebViewJsBridge bridge) {
        this.bridge = bridge;
        if (bridge != null) {
            this.base = bridge.base;
        }
    }

    public void setWebViewClient(WebViewClient webViewClient) {
        mWebViewClient = webViewClient;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (mWebViewClient != null) {
            mWebViewClient.onPageStarted(view, url, favicon);
        } else {
            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (mWebViewClient != null) {
            mWebViewClient.onPageFinished(view, url);
        } else {
            super.onPageFinished(view, url);
        }
    }

    @TargetApi(23)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (mWebViewClient != null) {
            mWebViewClient.onReceivedError(view, request, error);
        } else {
            super.onReceivedError(view, request, error);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (mWebViewClient != null) {
            mWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
        } else {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // do nothing
        }

        if (base.isCorrectProtocolScheme(url)) {
            if (base.isBridgeLoadedUrl(url)) {
                base.injectJavascriptFile();
            } else if (base.isQueueMessageUrl(url)) {
                String javascriptCommand = base.javascriptFetchQueryCommand();
                bridge.evaluateJavascript(javascriptCommand);
            } else if (base.isReturnMessageUrl(url)) {
                String messageQueueString = base.messageQueueStringFromReturnUrl(url);
                base.flushMessageQueue(messageQueueString);
            }
            return true;
        } else if (mWebViewClient != null) {
            return mWebViewClient.shouldOverrideUrlLoading(view, url);
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
