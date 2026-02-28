// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BitgetAccountItem {
    private BigDecimal maxOpenPosAvailable;
    private String unrealizedPL;
    private String usdtEquity;
    private BigDecimal available;
    private String maxTransferOut;
    private String marginCoin;
    private String frozen;
    private String crossedRiskRate;
    private String equity;
}
