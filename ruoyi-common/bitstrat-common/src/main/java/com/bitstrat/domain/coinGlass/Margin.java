// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.coinGlass;
import lombok.Data;

@Data
public class Margin {
    private Long fundingIntervalHours;
    private Double rate;
    private Long nextFundingTime;
    private String exchangeName;
    private String exchangeLogo;
    private Long status;
}
