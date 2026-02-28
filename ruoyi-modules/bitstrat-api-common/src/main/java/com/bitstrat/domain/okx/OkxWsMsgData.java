// OkxWsMsgData.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.okx;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;

@Data
public class OkxWsMsgData {
    private Long curPage;
    private String event;
    private Boolean lastPage;
    private JSONArray data;
    private OkxArg arg;
    private String eventType;
}

