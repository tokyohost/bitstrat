package com.bitstrat.ai.domain.serverWatch;

import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import lombok.Data;
import lombok.ToString;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/4 18:56
 * @Content 停止
 */
@Data
@ToString
public class StopWatchContext{
    private Long userId;


    private ABOrderTask abOrderTask;
}
