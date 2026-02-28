// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpotItemV2 {
    private String quantityPrecision;
    private String symbol;
    private String pricePrecision;
    private String sellLimitPriceRatio;
    private BigDecimal makerFeeRate;
    private String maxTradeAmount;
    private String orderQuantity;
    private String minTradeUSDT;
    private String minTradeAmount;
    private String quotePrecision;
    private BigDecimal takerFeeRate;
    private String areaSymbol;
    private String offTime;
    private String buyLimitPriceRatio;
    private String openTime;
    private String baseCoin;
    private String quoteCoin;
    private String status;
}
