// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.coinGlass;
import java.util.List;
import lombok.Data;

@Data
public class CoinFundingInfo {
    private ExchangeItem buy;
    private ExchangeItem sell;
    private Double buyPrice;
    private String symbol;
    private Long apr;
    private Double funding;
    private Double fee;
    private Long nextFundingTime;
    private Double sellPrice;
    private String symbolLogo;
    private Double spread;

    private Long taskId;
}
