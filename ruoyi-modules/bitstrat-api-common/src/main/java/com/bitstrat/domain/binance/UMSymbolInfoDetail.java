// UMSymbolInfoDetail.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class UMSymbolInfoDetail {
    private Integer quantityPrecision;
    private String symbol;
    private BigDecimal pricePrecision;
    private String requiredMarginPercent;
    private String contractType;
    private Long onboardDate;
    private String baseAsset;
    private List<Filter> filters;
    private Long baseAssetPrecision;
    private Long settlePlan;
    private String pair;
    private String triggerProtect;
    private String marginAsset;
    private List<String> orderType;
    private BigDecimal quotePrecision;
    private String underlyingType;
    private String liquidationFee;
    private String maintMarginPercent;
    private String marketTakeBound;
    private Long deliveryDate;
    private List<String> timeInForce;
    private String quoteAsset;
    /**
     * PENDING_TRADING 待上市
     * TRADING 交易中
     * PRE_DELIVERING 预交割
     * DELIVERING 交割中
     * DELIVERED 已交割
     * PRE_SETTLE 预结算
     * SETTLING 结算中
     * CLOSE 已下架
     */
    private String status;
    private List<String> underlyingSubType;
}
