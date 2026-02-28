// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.util.List;
import lombok.Data;

@Data
public class FundingTimeItem {
    private String symbol;
    private Long nextFundingTime;
    private String ratePeriod;
}
