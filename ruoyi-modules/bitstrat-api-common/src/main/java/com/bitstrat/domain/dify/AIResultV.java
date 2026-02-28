// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.dify;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * {
 *   "reason1": "理由1",
 *   "reason2": "理由2",
 *   "inPrice": "入场价格",
 *   "side":"long/short",
 *    "stopProfit": "止盈价格",
 *    "stopLoss": "止损价格",
 *   "score": "下单意愿，0-100分，50分为中立，靠近0分为不推荐入场，靠近100分为推荐入场",
 * }
 */
@Data
public class AIResultV {
    private String reason1;
    private Integer score;
    private String reason2;
    private String side;
    private BigDecimal inPrice;
    private BigDecimal stopLoss;
    private BigDecimal stopProfit;
}
