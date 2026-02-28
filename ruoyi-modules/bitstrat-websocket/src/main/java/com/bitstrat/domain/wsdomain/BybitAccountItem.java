// BybitAccountItem.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BybitAccountItem {
    private BigDecimal totalEquity;
    private String accountIMRate;
    private String totalMarginBalance;
    private String totalInitialMargin;
    private String totalAvailableBalance;
    private String accountType;
    private String accountMMRate;
    private String totalPerpUPL;
    private String totalWalletBalance;
    private String accountLTV;
    private String totalMaintenanceMargin;
    private List<Coin> coin;
}

