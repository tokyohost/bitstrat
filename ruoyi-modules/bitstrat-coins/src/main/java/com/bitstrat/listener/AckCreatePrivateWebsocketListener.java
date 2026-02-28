package com.bitstrat.listener;

import com.bitstrat.ai.config.ConnectionManager;
import com.bitstrat.config.ExchangeWebsocketProperties;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.Event.AckCreatePrivateWebsocketEvent;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.SubscriptMsgType;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/6 20:22
 * @Content
 */

@Component
@Slf4j
public class AckCreatePrivateWebsocketListener {


    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;

    @Autowired
    ExchangeWebsocketProperties exchangeWebsocketProperties;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    ICoinsApiService coinsApiService;
//
//    @EventListener(AckCreatePrivateWebsocketEvent.class)
//    public void ackPosition(AckCreatePrivateWebsocketEvent ackCreatePrivateWebsocketEvent) {
//
//        Account cacheAccount = ackCreatePrivateWebsocketEvent.getAccount();
//        CoinsApiVo coinsApiVo = coinsApiService.queryById(cacheAccount.getId());
//        if (Objects.isNull(coinsApiVo)) {
//            log.error("coinsApiVo is null for accountId: {}", cacheAccount.getId());
//            return;
//        }
//        Account account = AccountUtils.coverToAccount(coinsApiVo);
//        APITypeHelper.set(account.getType());
//        try{
//            String ex = ackCreatePrivateWebsocketEvent.getExchangeName();
//            Long userId = account.getUserId();
//            String urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(ex, WebSocketType.PRIVATE);
//            if (StringUtils.isEmpty(urlByExAndType)) {
//                log.error("urlByExAndType is empty userid{} ex {} type: {}", userId, ex, WebSocketType.PRIVATE);
//                return;
//            }
//            try {
//                ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
//                SubscriptMsgs wsSubscriptMsgs = exchangeService.getWsSubscriptMsgs();
//                exchangeConnectionManager.createConnection(account, userId + "", ex, WebSocketType.PRIVATE, URI.create(urlByExAndType),null,false,(channel)->{
//                    //监听仓位变动
//                    exchangeConnectionManager.sendSubscriptMessage(userId + "", account.getId(), ex, WebSocketType.PRIVATE, wsSubscriptMsgs.createSubscriptPositionMsg(), SubscriptMsgType.POSITION,channel);
//                    //监听订单成交
//                    exchangeConnectionManager.sendSubscriptMessage(userId + "", account.getId(), ex, WebSocketType.PRIVATE, wsSubscriptMsgs.createSubscriptOrderMsg(),null,channel);
//                    //监听余额变动
//                    exchangeConnectionManager.sendSubscriptMessage(userId + "", account.getId(), ex, WebSocketType.PRIVATE, wsSubscriptMsgs.createSubscriptAccountMsg(),null,channel);
//                });
//
//
//            } catch (Exception e) {
//                log.error("ws 监听仓位失败 {} userid {} ex {} type {} url:{}", e.getMessage(), userId, ex, WebSocketType.PRIVATE, urlByExAndType, e);
//            }
//        }finally {
//            APITypeHelper.clear();
//        }


//    }

}
