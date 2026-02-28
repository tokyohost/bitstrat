package com.bitstrat.ai.handler;

import com.bitstrat.domain.PositionWebsocketMsgData;
import com.bitstrat.domain.PositionWsData;
import com.bitstrat.domain.WebsocketMsgData;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/5 18:27
 * @Content
 */

@Component
public class PositionHandler {


    @Async
    @EventListener(PositionWebsocketMsgData.class)
    public void handlePosition(PositionWebsocketMsgData<List<PositionWsData>> msgData) {
        //更新持仓
        MarketABOrderStore.updatePositionByAccountId(msgData.getAccountId(), new HashSet<>(msgData.getData()));

        // 处理持仓逻辑
        WebSocketUtils.sendMessage(msgData.getUserId(), msgData.toJSONString());
    }
}
