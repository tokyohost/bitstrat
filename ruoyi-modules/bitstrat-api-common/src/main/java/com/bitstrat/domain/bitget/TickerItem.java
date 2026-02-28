// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class TickerItem {
    private BigDecimal lastPr;
    private String symbol;
    private String indexPrice;
    private String open24H;
    private String bidPr;
    @JSONField(name = "change24h")
    private BigDecimal change24H;
    private String quoteVolume;
    private String askSz;
    private String low24H;
    private String usdtVolume;
    private String openUtc;
    private String bidSz;
    private String markPrice;
    @JSONField(name = "changeUtc24h")
    private BigDecimal changeUtc24H;
    private String high24H;
    private String askPr;
    private String holdingAmount;
    private String baseVolume;
    private String deliveryStatus;
    private String ts;
    private String fundingRate;
}
