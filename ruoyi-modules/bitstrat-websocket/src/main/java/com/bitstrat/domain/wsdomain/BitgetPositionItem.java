// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BitgetPositionItem {
    private BigDecimal leverage;
    private BigDecimal keepMarginRate;
    private String breakEvenPrice;
    private String available;
    private String marginCoin;
    private String frozen;
    private BigDecimal liquidationPrice;
    private BigDecimal deductedFee;
    private String posMode;
    private String cTime;
    private String marginMode;
    private BigDecimal achievedProfits;
    private String posId;
    private String instId;
    private BigDecimal margin;
    private BigDecimal marginSize;
    private BigDecimal total;
    private BigDecimal marginRate;
    private BigDecimal unrealizedPL;
    private BigDecimal totalFee;
    private String autoMargin;
    private String holdSide;
    private BigDecimal openPriceAvg;
    private Long uTime;
    private String unrealizedPLR;
}
