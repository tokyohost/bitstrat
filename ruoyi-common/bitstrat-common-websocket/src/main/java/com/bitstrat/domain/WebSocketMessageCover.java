package com.bitstrat.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.bitstrat.domain.msg.AbsMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WebSocketMessageCover<T> extends AbsMessage {

    T data;

    public String toJSONString() {
        return JSON.toJSONString(this, JSONWriter.Feature.BrowserCompatible);
    }
}
