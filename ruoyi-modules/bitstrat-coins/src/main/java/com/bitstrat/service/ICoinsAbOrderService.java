package com.bitstrat.service;

import com.bitstrat.ai.domain.abOrder.ABOrderTask;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/4 17:10
 * @Content
 */
public interface ICoinsAbOrderService {
    ABOrderTask updateOrCreateABOrderTask(ABOrderTask abOrder,Long userId);
    ABOrderTask stopABOrderTask(ABOrderTask abOrder,Long userId);
}
