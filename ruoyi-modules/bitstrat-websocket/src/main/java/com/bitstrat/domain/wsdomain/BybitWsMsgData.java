// BybitWsMsgData.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;

@Data
public class BybitWsMsgData {
    private Long creationTime;
    private JSONArray data;
    private String topic;
    private String id;
}

