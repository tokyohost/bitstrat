// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.ai.domain.vo;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BitgetTickerVo {
    private BigDecimal lastPr;
    private String symbol;
    private BigDecimal indexPrice;
    private String open24H;
    private String nextFundingTime;
    private BigDecimal bidPr;
    private String change24H;
    private BigDecimal quoteVolume;
    private String deliveryPrice;
    private BigDecimal askSz;
    private String low24H;
    private String symbolType;
    private String openUtc;
    private String instId;
    private BigDecimal bidSz;
    private BigDecimal markPrice;
    private String high24H;
    private BigDecimal askPr;
    private String holdingAmount;
    private String baseVolume;
    private BigDecimal fundingRate;
    private Long ts;
}
