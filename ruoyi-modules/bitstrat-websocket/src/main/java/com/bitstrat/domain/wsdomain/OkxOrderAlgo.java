// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * okx 仓位止盈止损配置
 */
@Data
public class OkxOrderAlgo {
    private String slTriggerPxType;
    private String algoId;
    private String closeFraction;
    private BigDecimal slTriggerPx;
    private String tpTriggerPxType;
    private BigDecimal tpTriggerPx;
}
