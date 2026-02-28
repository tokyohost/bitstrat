package com.bitstrat.ai.domain.serverWatch;

import com.bitstrat.ai.constant.ABOrderSideType;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 20:26
 * @Content
 */

@Data
public class ABOrderWatchContext {
    ABOrderWatch sideA;
    ABOrderWatch sideB;

    /**
     * 操作方向
     * see {@link ABOrderSideType}
     */
    String abType;
}
