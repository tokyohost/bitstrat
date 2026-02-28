// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.coinGlass;
import java.util.List;
import lombok.Data;

@Data
public class SportFundingRateItem {
    private String quoteCurrency;
    private String symbol;
    private String futuresType;
    private Double threeDayFundingRate;
    private Long yearFundingRate;
    private String currencyLog;
    private String spotType;
    private String currency;
    private String exchangeName;
    private Long fundingRatePositive;
    private Long updateTime;
    private Long fundingRate;
}
