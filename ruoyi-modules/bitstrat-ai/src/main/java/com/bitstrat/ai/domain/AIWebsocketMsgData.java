package com.bitstrat.ai.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 21:26
 * @Content
 */
@Data
public class AIWebsocketMsgData<T>{
    /**
     * see {@link com.bitstrat.ai.constant.WsType}
     */
    String type;
    T data;

    public String toJSONString() {
        return JSON.toJSONString(this, JSONWriter.Feature.BrowserCompatible);
    }
}
