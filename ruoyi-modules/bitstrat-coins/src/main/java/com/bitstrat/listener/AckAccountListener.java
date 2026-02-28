package com.bitstrat.listener;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.handler.MarketABOrderStore;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.Event.AckAccountEvent;
import com.bitstrat.service.ICoinsApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AckAccountListener {

    @Autowired
    ICoinsApiService coinsApiService;

    /**
     * 异步更新账户信息
     * @param ackAccountEvent
     */
    @Async
    @EventListener(AckAccountEvent.class)
    public void ackAccountEvent(AckAccountEvent ackAccountEvent) {
        log.info("ackAccountEvent {}", JSONObject.toJSONString(ackAccountEvent));
        coinsApiService.updateBalanceAndFreeById(ackAccountEvent.getAccountId(), ackAccountEvent.getBalance(),
            ackAccountEvent.getFreeBalance());

        //更新到内存缓存中
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setBalance(ackAccountEvent.getBalance());
        accountBalance.setFreeBalance(ackAccountEvent.getFreeBalance());
        accountBalance.setApiId(ackAccountEvent.getAccountId());
        MarketABOrderStore.updateAccountByAccountId(ackAccountEvent.getAccountId(), accountBalance);

    }
}
