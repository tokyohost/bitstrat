// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bybit;
import lombok.Data;

@Data
public class BybitSymbol {
    private boolean innovation;
    private String maxTradeQuantity;
    private String minTradeQuantity;
    private String maxTradeAmount;
    private boolean showStatus;
    private String minPricePrecision;
    private String baseCurrency;
    private String quoteCurrency;
    private String minTradeAmount;
    private String basePrecision;
    private String quotePrecision;
    private String name;
    private String alias;
    private long category;
}
