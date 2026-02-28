// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BitgetHistoryPositionItem {
    private String openAvgPrice;
    private String symbol;
    private String openFee;
    private String marginCoin;
    private String cTime;
    private String marginMode;
    private String totalFunding;
    private String closeFee;
    private BigDecimal netProfit;
    private String pnl;
    private String openTotalPos;
    private String positionId;
    private String holdSide;
    private String uTime;
    private String closeTotalPos;
    private String closeAvgPrice;
}
