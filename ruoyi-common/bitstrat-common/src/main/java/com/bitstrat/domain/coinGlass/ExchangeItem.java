// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.coinGlass;
import java.util.List;
import lombok.Data;

@Data
public class ExchangeItem {
    private String symbol;
    private Double fundingIntervalHours;
    private Double openInterest;
    private Long rate;
    private String exchangeName;
    private Long predictedRate;
    private String exchangeLogo;
    private String url;
    private Long status;
}
