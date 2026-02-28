// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BinanceBalanceItem {
    private String accountAlias;
    private BigDecimal maxWithdrawAmount;
    private BigDecimal balance;
    private BigDecimal crossWalletBalance;
    private BigDecimal crossUnPnl;
    private Long updateTime;
    private String asset;
    private Boolean marginAvailable;
    private BigDecimal availableBalance;
}
