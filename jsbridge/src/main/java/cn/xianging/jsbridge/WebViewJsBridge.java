package cn.xianging.jsbridge;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by xiaoz on 16/3/10.
 */
public class WebViewJsBridge implements WebViewJsBridgeBase.WebViewJsBridgeBaseProxy {

    private WebView webView;
    private WebViewJsBridgeClient client;
    WebViewJsBridgeBase base;

    public static WebViewJsBridge bridgeForWebView(WebView webView) {
        return new WebViewJsBridge(webView);
    }

    public WebViewJsBridge(WebView webView) {
        this.webView = webView;
        this.base = WebViewJsBridgeBase.instance(this);
        this.client = new WebViewJsBridgeClient(this);
        this.webView.setWebViewClient(client);
    }

    public void registerHandler(String handlerName, WVJBHandler handler) {
        base.messageHandlers.put(handlerName, handler);
    }

    public void callHandler(String handlerName) {
        callHandler(handlerName, null);
    }

    public void callHandler(String handlerName, String data) {
        callHandler(handlerName, data, null);
    }

    public void callHandler(String handlerName, String data, WVJBResponseCallback callback) {
        base.sendData(data, callback, handlerName);
    }

    public void setWebViewClient(WebViewClient client) {
        this.client.setWebViewClient(client);
    }

    @Override
    public void evaluateJavascript(String javascriptCommand) {
        webView.loadUrl("javascript:" + javascriptCommand);
    }

    @Override
    public String getJavascriptFileString() {
        return assetFile2Str("WebViewJavascriptBridge.js");
    }

    public String assetFile2Str(String urlStr){
        InputStream in = null;
        try{
            in = webView.getContext().getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        return null;
    }
}
