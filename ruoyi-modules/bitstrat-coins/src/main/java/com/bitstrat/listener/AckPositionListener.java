package com.bitstrat.listener;

import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.WebsocketMsgType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.Event.AckPositionSyncEvent;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/6 19:20
 * @Content
 */

@Component
@Slf4j
public class AckPositionListener {
    @Autowired
    ExchangeApiManager exchangeApiManager;
    @Autowired
    ICoinsApiService coinsApiService;
    /**
     * 监听同步仓位事件
     * @param ackPositionSyncEvent
     */
    @EventListener(AckPositionSyncEvent.class)
    @Async
    public void ackPosition(AckPositionSyncEvent ackPositionSyncEvent) {
        Account cacheAccount = ackPositionSyncEvent.getAccount();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(cacheAccount.getId());
        if (Objects.isNull(coinsApiVo)) {
            log.error("coinsApiVo is null for accountId: {}", cacheAccount.getId());
            return;
        }
        Account account = AccountUtils.coverToAccount(coinsApiVo);

        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ackPositionSyncEvent.getExchangeName());
        if (Objects.nonNull(exchangeService)) {
            List<PositionWsData> positionWsData = exchangeService.queryContractPositionDetail(account, new PositionParams());
//            WebsocketMsgData<List<PositionWsData>> msg = new WebsocketMsgData<>();
//            msg.setData(positionWsData);
//            msg.setType(WebsocketMsgType.POSITION);
//            msg.setAccountId(account.getId());
//            WebSocketUtils.sendMessage(account.getUserId(), msg.toJSONString());
            PositionWebsocketMsgData<List<PositionWsData>> websocketMsgData = new PositionWebsocketMsgData<>();
            websocketMsgData.setData(positionWsData);
            websocketMsgData.setType(WebsocketMsgType.POSITION);
            websocketMsgData.setAccountId(account.getId());
            websocketMsgData.setUserId(account.getUserId());
            websocketMsgData.setExchangeName(ackPositionSyncEvent.getExchangeName());
            SpringUtils.getApplicationContext().publishEvent(websocketMsgData);
        }
    }
}
