package com.bitstrat.task;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.bybit.ByBitReconnectSocketClient;
import com.bitstrat.client.WebSocketClient;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.msg.SubscribeOrder;
import com.bitstrat.handler.ByBitSocketOrderHandler;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.store.RoleCenter;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.log.LogOption;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.ScheduledTaskObservationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 17:34
 * @Content
 */

@Slf4j
@Component
public class BybitOrderTask implements OrderTask {
    Thread thread = null;
    WebSocket socket = null;
    @Autowired
    WebSocketClient webSocketClient;
    @Autowired
    ByBitClientCenter byBitClientCenter;

    @Value("${loss-point.order-cooling-time}")
    Long orderCoolingTime;
    @Autowired
    RoleCenter roleCenter;
    @Autowired
    ByBitSocketOrderHandler byBitSocketClientHandler;


    public void run(JSONObject params) {

        SubscribeOrder subscribeOrder = params.to(SubscribeOrder.class);
        ByBitAccount byBitAccount = subscribeOrder.getAccount();
        final List<String> symbolTickers = new ArrayList<>();
        //订阅订单成交
        symbolTickers.add("order");
        thread = new Thread(() -> {
//            var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(),byBitAccount.getApiPwd(),BybitApiConfig.STREAM_MAINNET_DOMAIN, true)
//                .newWebsocketClient();
            ByBitReconnectSocketClient client
                = new ByBitReconnectSocketClient(byBitAccount.getApiSecurity(),byBitAccount.getApiPwd(),BybitApiConfig.STREAM_MAINNET_DOMAIN,20,"-1",true, LogOption.SLF4J.getLogOptionType(),byBitSocketClientHandler);
            client.setClientName("用户 "+byBitAccount.getApiSecurity()+"-ORDER-Socket");

//            client.setMessageHandler(byBitSocketClientHandler);

            // Ticker
            if(socket != null) {
                socket.close(1000, "Normal Closure");
                socket = client.getPrivateChannelStream(symbolTickers, BybitApiConfig.V5_PRIVATE);
            }else{
                socket = client.getPrivateChannelStream(symbolTickers, BybitApiConfig.V5_PRIVATE);
            }

            log.warn("订阅订单成功");
        });
        thread.setDaemon(true);
        thread.start();


    }





    @Override
    public void stop() {
        if(socket != null) {
            socket.close(1000, "Normal Closure");
        }
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

    }

    @Override
    public String getExchangeName() {
        return ExchangeType.BYBIT.getName();
    }
}
