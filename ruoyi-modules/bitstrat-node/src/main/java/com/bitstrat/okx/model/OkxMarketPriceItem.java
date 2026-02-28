// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.okx.model;
import java.util.List;
import lombok.Data;

@Data
public class OkxMarketPriceItem {
    private String instType;
    private String instId;
    private String markPx;
    private Long ts;
}
