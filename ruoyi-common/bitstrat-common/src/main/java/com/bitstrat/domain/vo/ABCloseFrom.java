package com.bitstrat.domain.vo;

import com.bitstrat.domain.abOrder.OrderTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/9 17:41
 * @Content
 */

@EqualsAndHashCode(callSuper = false)
@Data
public class ABCloseFrom extends AbTaskFrom{
    Long userId;
    String abTaskId;
    boolean reduceOnly;

    OrderTask abOrderTask;
}
