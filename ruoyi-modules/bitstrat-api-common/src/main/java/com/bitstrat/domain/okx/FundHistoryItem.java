// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.okx;
import java.util.List;
import lombok.Data;

@Data
public class FundHistoryItem {
    private String instType;
    private String instId;
    private String realizedRate;
    private String formulaType;
    private String method;
    private Long fundingTime;
    private String fundingRate;
}
