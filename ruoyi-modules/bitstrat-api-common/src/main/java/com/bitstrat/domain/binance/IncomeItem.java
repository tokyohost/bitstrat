// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class IncomeItem {
    private BigDecimal income;
    private String tranId;
    private String symbol;
    private String incomeType;
    private Long time;
    private String asset;
    private String tradeId;
    private String info;
}
