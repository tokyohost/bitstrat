package com.bitstrat.listener;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.handler.MarketABOrderStore;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.Event.AckAccountEvent;
import com.bitstrat.domain.Event.AckOrderPlaceErrorEvent;
import com.bitstrat.domain.vo.ABCloseFrom;
import com.bitstrat.domain.vo.ABOrderFrom;
import com.bitstrat.service.ExOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/9 17:46
 * @Content
 */
@Slf4j
@Component
public class AckOrderListener {

    @Autowired
    ExOrderService exOrderService;

    /**
     * 同步下单
     * @param orderFrom
     */
    @EventListener(ABOrderFrom.class)
    public void ackOrderEvent(ABOrderFrom orderFrom) {
        log.info("ABOrderFrom {}", JSONObject.toJSONString(orderFrom));
        exOrderService.oncePlace2ExOrder(orderFrom);

    }

    /**
     * 同步平仓
     * @param closeFrom
     */
    @EventListener(ABCloseFrom.class)
    public void ackCloseEvent(ABCloseFrom closeFrom) {
        log.info("ABCloseFrom {}", JSONObject.toJSONString(closeFrom));
        exOrderService.oncePlace2CloseExOrder(closeFrom);

    }

}
