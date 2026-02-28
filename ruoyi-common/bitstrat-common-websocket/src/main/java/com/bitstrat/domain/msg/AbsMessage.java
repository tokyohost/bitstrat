package com.bitstrat.domain.msg;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

@Data
public class AbsMessage {
    String type;

    public String toJSONString() {
        return JSON.toJSONString(this, JSONWriter.Feature.BrowserCompatible);
    }
}
