package cn.xianging.jsbridge;

import android.os.Looper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoz on 16/3/10.
 */
public class WebViewJsBridgeBase {

    private static final String CUSTOM_PROTOCOL_SCHEME = "wvjbscheme";
    private static final String QUEUE_HAS_MESSAGE = "__WVJB_QUEUE_MESSAGE__";
    private static final String URL_WITH_RETURN_MESSAGE = "__URL_WITH_RETURN_MESSAGE__";
    private static final String BRIDGE_LOADED = "__BRIDGE_LOADED__";

    private WebViewJsBridgeBaseProxy proxy;

    List<WVJBMessage> startupMessageQueue;
    Map<String, WVJBResponseCallback> responseCallbacks;
    Map<String, WVJBHandler> messageHandlers;
    private WVJBHandler messageHandler;

    private long uniqueId;

    public static WebViewJsBridgeBase instance(WebViewJsBridgeBaseProxy proxy) {
        WebViewJsBridgeBase base = new WebViewJsBridgeBase();
        base.setProxy(proxy);
        return base;
    }

    private WebViewJsBridgeBase() {
        messageHandlers = new HashMap<>();
        startupMessageQueue = new ArrayList<>();
        responseCallbacks = new HashMap<>();
        uniqueId = 0;
    }

    public void reset() {
        startupMessageQueue = Collections.emptyList();
        responseCallbacks.clear();
        uniqueId = 0;
    }

    public void sendData(String data, WVJBResponseCallback callback, String handlerName) {
        WVJBMessage message = new WVJBMessage();
        if (!TextUtils.isEmpty(data)) {
            message.setData(data);
        }
        if (callback != null) {
            String callbackId = String.format("java_cb_%s", ++uniqueId);
            responseCallbacks.put(callbackId, callback);
            message.setCallbackId(callbackId);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            message.setHandlerName(handlerName);
        }
        queueMessage(message);
    }

    public void injectJavascriptFile() {
        String jsContent = proxy.getJavascriptFileString();
        proxy.evaluateJavascript(jsContent);

        if (startupMessageQueue != null) {
            for (WVJBMessage message : startupMessageQueue) {
                dispatchMessage(message);
            }
            startupMessageQueue = null;
        }
    }

    public boolean isCorrectProtocolScheme(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith(CUSTOM_PROTOCOL_SCHEME + "://");
    }

    public boolean isQueueMessageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith(CUSTOM_PROTOCOL_SCHEME + "://" + QUEUE_HAS_MESSAGE);
    }

    public boolean isReturnMessageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith(CUSTOM_PROTOCOL_SCHEME + "://" + URL_WITH_RETURN_MESSAGE);
    }

    public boolean isBridgeLoadedUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith(CUSTOM_PROTOCOL_SCHEME + "://" + BRIDGE_LOADED);
    }

    public String javascriptFetchQueryCommand() {
        return "WebViewJavascriptBridge._fetchQueue();";
    }

    public String messageQueueStringFromReturnUrl(String url) {
        String prefix = CUSTOM_PROTOCOL_SCHEME + "://" + URL_WITH_RETURN_MESSAGE + "/";
        return url.substring(prefix.length());
    }

    public void flushMessageQueue(String messageQueueString) {
        if (TextUtils.isEmpty(messageQueueString)) {
            return;
        }
        List<WVJBMessage> messages = WVJBMessage.listFromString(messageQueueString);
        int size = messages.size();
        for (int i = 0; i < size; i++) {
            WVJBMessage message = messages.get(i);
            String responseId = message.getResponseId();
            if (!TextUtils.isEmpty(responseId)) {
                WVJBResponseCallback callback = responseCallbacks.get(responseId);
                callback.callback(message.getResponseData());
                responseCallbacks.remove(responseId);
            } else {
                WVJBResponseCallback callback = null;

                // 获取js的callbackid
                final String callbackId = message.getCallbackId();
                if (!TextUtils.isEmpty(callbackId)) {
                    callback = new WVJBResponseCallback() {
                        @Override
                        public void callback(String responseData) {
                            if (responseData == null) {
                                responseData = "";
                            }
                            WVJBMessage responseMessage = new WVJBMessage();
                            responseMessage.setResponseId(callbackId);
                            responseMessage.setResponseData(responseData);
                            queueMessage(responseMessage);
                        }
                    };
                } else {
                    callback = new WVJBResponseCallback() {
                        @Override
                        public void callback(String responseData) {

                        }
                    };
                }

                WVJBHandler handler = messageHandlers.get(message.getHandlerName());
                if (handler == null) {
                    continue;
                }
                handler.handle(message.getData(), callback);
            }
        }
    }

    private void queueMessage(WVJBMessage message) {
        if (startupMessageQueue != null) {
            startupMessageQueue.add(message);
        } else {
            dispatchMessage(message);
        }
    }

    public void dispatchMessage(WVJBMessage message) {
        String messageJson = message.toJson();
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format("WebViewJavascriptBridge._handleMessageFromJava('%s');", messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            evaluateJavascript(javascriptCommand);
        }
    }

    private void evaluateJavascript(String javascriptCommand) {
        if (proxy != null) {
            proxy.evaluateJavascript(javascriptCommand);
        }
    }

    public void setProxy(WebViewJsBridgeBaseProxy proxy) {
        this.proxy = proxy;
    }

    public interface WebViewJsBridgeBaseProxy {
        void evaluateJavascript(String javascriptCommand);
        String getJavascriptFileString();
    }

}
