// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class FundingFeeItem {
    private String symbol;
    private BigDecimal minFundingRate;
    private String fundingRateInterval;
    private BigDecimal fundingRate;
    private String nextUpdate;
    private BigDecimal maxFundingRate;
}
