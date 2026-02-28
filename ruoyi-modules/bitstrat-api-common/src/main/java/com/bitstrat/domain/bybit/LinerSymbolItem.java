// LinerSymbolItem.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bybit;
import lombok.Data;

@Data
public class LinerSymbolItem {
    private String symbol;
    private String lowerFundingRate;
    private String deliveryTime;
    private Boolean unifiedMarginTrade;
    private String contractType;
    private Boolean isPreListing;
    private LotSizeFilter lotSizeFilter;
    private PriceFilter priceFilter;
    private String copyTrading;
    private String settleCoin;
    private LeverageFilter leverageFilter;
    private String launchTime;
    private String upperFundingRate;
    private RiskParameters riskParameters;
    private String priceScale;
    private String deliveryFeeRate;
    private Integer fundingInterval;
    private String baseCoin;
    private String quoteCoin;
    private String status;
}


