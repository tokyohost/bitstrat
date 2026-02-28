package com.bitstrat.ai.listener;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.distuptor.ABDisruptor;
import com.bitstrat.ai.handler.MarketABOrderStore;
import com.bitstrat.domain.Event.AckOrderPlaceErrorEvent;
import com.bitstrat.domain.abOrder.OrderTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/9 18:34
 * @Content
 */
@Slf4j
@Component
public class AckOrderErrorEventListener {


    /**
     * 下单/平仓异常
     * @param ackAccountEvent
     */
    @EventListener(AckOrderPlaceErrorEvent.class)
    public void ackOrderErrorEvent(AckOrderPlaceErrorEvent ackAccountEvent) {
        log.info("AckOrderPlaceErrorEvent {}", JSONObject.toJSONString(ackAccountEvent));
        OrderTask orderTask = ackAccountEvent.getOrderTask();
        String taskId = orderTask.getTaskId();
        Long userId = orderTask.getUserId();
        List<ABDisruptor> byUserId = MarketABOrderStore.getByUserId(userId);
        for (ABDisruptor disruptor : byUserId) {
            if(disruptor.getTaskId().equalsIgnoreCase(taskId)){
                disruptor.getAbOrderTask().getOperate().setReduceOnly("1");
                log.warn("下单平仓异常，状态修改为只允许平仓");
            }
        }
    }
}
