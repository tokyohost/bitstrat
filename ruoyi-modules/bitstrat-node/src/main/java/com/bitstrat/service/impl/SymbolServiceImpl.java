package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.MessageType;
import com.bitstrat.constant.SymbolType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.SubscribeOrder;
import com.bitstrat.domain.msg.SubscribeSymbol;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.server.MessageData;
import com.bitstrat.service.MessageProcess;
import com.bitstrat.service.SymbolService;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.store.RoleCenter;
import com.bitstrat.store.TaskCenter;
import com.bitstrat.store.listener.SymbolWatch;
import com.bitstrat.task.OrderTask;
import com.bitstrat.task.TaskManager;
import com.bitstrat.utils.ProxyUtils;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.bitstrat.constant.MessageType.SUBSCRIPTION_ORDER;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 17:09
 * @Content
 */

@Slf4j
@Service
public class SymbolServiceImpl implements SymbolService {
    @Autowired
    SymbolWatch symbolWatch;
    @Autowired
    MessageProcess messageProcess;

    @Autowired
    RoleCenter roleCenter;

    @Autowired
    ByBitClientCenter byBitClientCenter;

    @Autowired
    TaskManager taskManager;

    @Override
    public void traggerWatchSymbol(String exchangeName) {
        this.traggerWatchSymbol(exchangeName, null);

    }

    @Override
    public void traggerWatchSymbol(String exchangeName, String symbol) {

        Message message = new Message();
        message.setType(MessageType.SUBSCRIPTION_SYMBOL);
        message.setExchangeName(exchangeName);

        SubscribeSymbol subscribeSymbol = new SubscribeSymbol();
        subscribeSymbol.setSymbolType(SymbolType.LINER);

        if (StringUtils.isEmpty(symbol)) {
            //监听所有的symbol
            HashSet<String> symbols = roleCenter.getSymbols();
            subscribeSymbol.setSymbols(new ArrayList<>(symbols));

        }else{
            subscribeSymbol.setSymbols(List.of(symbol));

        }
        message.setData(subscribeSymbol);
        message.setTimestamp(System.currentTimeMillis());
        messageProcess.subscription(message);
    }

    @Override
    public void traggerWatchOrder(String exchangeName, MessageData messageData) {
        Message message = new Message();
        message.setType(MessageType.SUBSCRIPTION_ORDER);
        message.setExchangeName(exchangeName);
        message.setTimestamp(System.currentTimeMillis());
        message.setData(messageData);
        messageProcess.subscription(message);
    }

    @Override
    public void initWebsocketClient() {
        ConcurrentHashMap<String, ActiveLossPoint> symbolLossPointMap = roleCenter.getSymbolLossPointMap();
        HashMap<String, ByBitAccount> accountHashMap = new HashMap<>();
        for (ActiveLossPoint value : symbolLossPointMap.values()) {
            ByBitAccount account = value.getAccount();
            accountHashMap.put(account.getApiSecurity(), account);

        }
        for (ByBitAccount account : accountHashMap.values()) {
            if (Objects.nonNull(account)) {
                //初始化构建websocket 长链接
                WebsocketStreamClient bybitApiSocketStream = byBitClientCenter.getBybitApiSocketStream(account.getApiSecurity(), account.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
                bybitApiSocketStream.setClientName("用户 "+account.getApiSecurity()+"-ORDER-Socket");
                if(Objects.nonNull(bybitApiSocketStream)){
                    log.info("用户 {} ws 长连接已建立",account.getApiSecurity());
                }
                //订阅订单
                SubscribeOrder subscribeOrder = new SubscribeOrder();
                subscribeOrder.setAccount(account);
                JSONObject from = JSONObject.from(subscribeOrder);
                ConcurrentHashMap<String, OrderTask> orderTaskMap = TaskCenter.getOrderTaskMap();

                OrderTask orderTask = orderTaskMap.get(subscribeOrder.getAccount().getApiSecurity());
                if (orderTask == null) {
                    OrderTask orderTaskStrategy = taskManager.getOrderTaskStrategy(ExchangeType.BYBIT.getName());
                    if(orderTaskStrategy != null) {
                        orderTaskStrategy.run(from);
                    }
                    log.info("已订阅订单");
                }


            }
        }


    }
}
