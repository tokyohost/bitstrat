package com.bitstrat.domain.Event;

import com.bitstrat.domain.abOrder.OrderTask;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/9 18:10
 * @Content 下单异常！停止任务
 */

@Data
public class AckOrderPlaceErrorEvent {
    OrderTask orderTask;
}
