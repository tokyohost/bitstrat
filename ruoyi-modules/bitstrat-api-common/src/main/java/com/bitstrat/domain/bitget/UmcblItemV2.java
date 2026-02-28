// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class UmcblItemV2 {
    private String symbol;
    private String maintainTime;
    private String deliveryTime;
    private Integer maxLever;
    private Integer volumePlace;
    private String maxPositionNum;
    private BigDecimal pricePlace;
    private String maxSymbolOrderNum;
    private List<String> supportMarginCoins;
    private String openCostUpRatio;
    private BigDecimal priceEndStep;
    private String deliveryPeriod;
    private BigDecimal takerFeeRate;
    private BigDecimal sizeMultiplier;
    private Integer fundInterval;
    private String buyLimitPriceRatio;
    private String feeRateUpRatio;
    private String openTime;
    private String maxProductOrderNum;
    private String baseCoin;
    private String sellLimitPriceRatio;
    private BigDecimal makerFeeRate;
    private String symbolType;
    private BigDecimal minTradeUSDT;
    private String launchTime;
    private BigDecimal minTradeNum;
    private String posLimit;
    private String symbolStatus;
    private String limitOpenTime;
    private Integer minLever;
    private String offTime;
    private String deliveryStartTime;
    private String quoteCoin;
}
