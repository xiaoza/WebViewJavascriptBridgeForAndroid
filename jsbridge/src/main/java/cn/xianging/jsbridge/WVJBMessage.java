package cn.xianging.jsbridge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * Created by xiaoz on 16/3/10.
 */
public class WVJBMessage {
    private String data;
    private String callbackId;
    private String handlerName;
    private String responseId;
    private String responseData;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

    public static WVJBMessage objFromString(String string) {
        return JSON.parseObject(string, WVJBMessage.class);
    }

    public static List<WVJBMessage> listFromString(String jsonString) {
        return JSON.parseObject(jsonString, new TypeReference<List<WVJBMessage>>(){});
    }
}
