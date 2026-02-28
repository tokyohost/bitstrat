// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.ai.domain.abOrder;
import java.math.BigDecimal;
import java.util.List;

import com.bitstrat.ai.constant.ABOrderSideType;
import lombok.Data;

@Data
public class ABOperate {
    //每次下单数量
    private BigDecimal size;
    // 1-仅减仓 0-正常
    private volatile String reduceOnly;
    private Long gap;
    private BigDecimal openGap;
    //最大持仓数量
    private BigDecimal maxSize;

    /**
     * 'plusAminB' | 'plusBminA'
     * see {@link ABOrderSideType#PlusAminB}
     */
    private String type;
    private BigDecimal closeGap;
    private Long coldSec;
    // stop/start

    /**
     * see{@link com.bitstrat.ai.constant.AbTaskStatus}
     */

    private String status;
}
