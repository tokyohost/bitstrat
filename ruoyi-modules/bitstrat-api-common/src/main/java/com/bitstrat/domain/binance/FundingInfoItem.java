// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class FundingInfoItem {
    private String symbol;
    private Long fundingIntervalHours;
    private String adjustedFundingRateFloor;
    private BigDecimal adjustedFundingRateCap;
    private Boolean disclaimer;
}
