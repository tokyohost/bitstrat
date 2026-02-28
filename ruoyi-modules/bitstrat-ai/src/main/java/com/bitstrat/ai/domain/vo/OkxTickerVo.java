// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.ai.domain.vo;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OkxTickerVo {
    private String instType;
    private String instId;
    private BigDecimal markPx;
    private Long ts;
}
