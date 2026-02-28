package com.bitstrat.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.bitstrat.constant.WebsocketMsgType;
import lombok.Data;

import java.math.BigInteger;
import java.util.LinkedHashMap;

@Data
public class WebsocketMsgData<T> {

    /**
     * see {@link WebsocketMsgType}
     */
    private String type;

    private Long accountId;
    private Long userId;

    private String exchangeName;

    private T data;

    public String toJSONString() {
        return JSON.toJSONString(this, JSONWriter.Feature.BrowserCompatible);
    }
}
