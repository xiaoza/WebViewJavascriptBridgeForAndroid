package cn.xianging.webviewjavascriptbridgeforandroid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

import cn.xianging.jsbridge.WVJBHandler;
import cn.xianging.jsbridge.WVJBResponseCallback;
import cn.xianging.jsbridge.WebViewJsBridge;

/**
 * Created by xiaoz on 15/10/20.
 */
public class WebFragment extends Fragment implements
        WebViewWithProgress.OnTitleReadyListener {
    private static final String URL_KEY = "url_tobe_load";

    private WebFragmentDelegate delegate;
    private WebViewWithProgress mWebView;
    private WebViewJsBridge bridge;

    private String urlToLoad;

    public static WebFragment newInstance(String url) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            urlToLoad = args.getString(URL_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mWebView = (WebViewWithProgress) view.findViewById(R.id.web_view);
        mWebView.setOnTitleReadyListener(this);
        mWebView.requestFocusFromTouch();
        bridge = WebViewJsBridge.bridgeForWebView(mWebView);
        bridge.setWebViewClient(new WebViewClientWrapper());
        bridge.registerHandler("auth_handler", new WVJBHandler() {
            @Override
            public void handle(String data, WVJBResponseCallback callback) {
                Map<String, String> authInfo = new HashMap<>();
                authInfo.put("uuid", "abcd");
                authInfo.put("token", "efg");
                callback.callback(JSON.toJSONString(authInfo));
            }
        });

        bridge.registerHandler("share_handler", new WVJBHandler() {
            @Override
            public void handle(String data, WVJBResponseCallback callback) {
                Log.d("share info from js : ", data);
            }
        });

        updateData();
    }

    @Override
    public void onTitleReady(String title) {
        getActivity().setTitle(title);
    }

    public void updateData() {
        if (!TextUtils.isEmpty(urlToLoad)) {
            mWebView.loadUrl(urlToLoad);
        }
    }

    public void setDelegate(WebFragmentDelegate delegate) {
        this.delegate = delegate;
    }

    private class WebViewClientWrapper extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (delegate != null) {
                return delegate.shouldInterceptWebViewRequest(view, url);
            }
            return false;
        }
    }

    /**
     * 对外提供的获取登陆与分享信息的接口
     */
    public interface WebFragmentDelegate {
        boolean shouldInterceptWebViewRequest(WebView view, String url);
    }

}
