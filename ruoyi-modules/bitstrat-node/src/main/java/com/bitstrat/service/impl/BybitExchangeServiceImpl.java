package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.msg.BybitOrder;
import com.bitstrat.domain.server.Message;
import com.bitstrat.handler.ByBitSocketOrderHandler;
import com.bitstrat.service.ExchangeService;
import com.bitstrat.service.SymbolService;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.utils.ProxyUtils;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.bybit.api.client.config.BybitApiConfig.V5_TRADE;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:12
 * @Content
 */

@Slf4j
@Service
public class BybitExchangeServiceImpl implements ExchangeService {
    @Autowired
    ByBitClientCenter byBitClientCenter;
    @Autowired
        @Lazy
    SymbolService symbolService;
    @Autowired
    ByBitSocketOrderHandler byBitSocketClientHandler;

    @Override
    public void buy(Message message) {
        try {
            JSONObject from = JSONObject.from(ProxyUtils.getRealObjectFromProxy(message.getData()));
            BybitOrder bybitOrder = from.toJavaObject(BybitOrder.class);
            Map<String, Object> orderPrarms = pressOrder(bybitOrder);
            ByBitAccount byBitAccount = bybitOrder.getByBitAccount();
            WebsocketStreamClient bybitApiSocketStream = byBitClientCenter.getBybitApiSocketStream(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
            bybitApiSocketStream.setClientName("用户"+byBitAccount.getApiSecurity()+"-TRADE-Socket");
            orderPrarms.put("side", "Buy");
            WebSocket tradeChannelStream = bybitApiSocketStream.getTradeChannelStream(
                orderPrarms, V5_TRADE);
            bybitApiSocketStream.sendSubscribeMessage(tradeChannelStream,orderPrarms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void amend(Message message) {

    }

    private Map<String, Object> pressOrder(BybitOrder bybitOrder) throws Exception {

        Map<String, Object> orderPrarms = new HashMap<>();
        if (bybitOrder.getType().equalsIgnoreCase(OrderType.LIMIT)) {
            orderPrarms.put("orderType", OrderType.LIMIT);
        }else{
            orderPrarms.put("orderType", OrderType.MARKET);
        }
        orderPrarms.put("qty",bybitOrder.getQuantity().toPlainString());
        orderPrarms.put("price",bybitOrder.getPrice().toPlainString());
//        orderPrarms.put("side", "Buy");
        orderPrarms.put("symbol", bybitOrder.getSymbol());
        orderPrarms.put("op", "order.create");
        orderPrarms.put("orderLinkId", bybitOrder.getOrderLinkId());

        orderPrarms.put("timeInForce", bybitOrder.getTimeInForce());
        orderPrarms.put("category", bybitOrder.getCategory());
        orderPrarms.put("stopLoss", bybitOrder.getStopLoss());

//            bybitApiSocketStream.getTradeChannelStream(
//                Map.of("category", "spot","symbol", "XRPUSDT", "side", "Buy",
//                    "orderType", "Market", "qty", "10"), V5_TRADE);

        return orderPrarms;
    }

    @Override
    public void sell(Message message) {
        try {
            JSONObject from = JSONObject.from(ProxyUtils.getRealObjectFromProxy(message.getData()));
            BybitOrder bybitOrder = from.toJavaObject(BybitOrder.class);
            Map<String, Object> orderPrarms = pressOrder(bybitOrder);
            ByBitAccount byBitAccount = bybitOrder.getByBitAccount();
            WebsocketStreamClient bybitApiSocketStream = byBitClientCenter.getBybitApiSocketStream(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
            bybitApiSocketStream.setClientName("用户"+byBitAccount.getApiSecurity()+"-TRADE-Socket");
            orderPrarms.put("side", "Sell");
            WebSocket tradeChannelStream = bybitApiSocketStream.getTradeChannelStream(
                orderPrarms, V5_TRADE);
            bybitApiSocketStream.sendSubscribeMessage(tradeChannelStream,orderPrarms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancel(Message message) {

        JSONObject from = null;
        try {
            from = JSONObject.from(ProxyUtils.getRealObjectFromProxy(message.getData()));
            BybitOrder bybitOrder = from.toJavaObject(BybitOrder.class);
            Map<String, Object> orderPrarms = new HashMap<>();
            ByBitAccount byBitAccount = bybitOrder.getByBitAccount();
            WebsocketStreamClient bybitApiSocketStream = byBitClientCenter.getBybitApiSocketStream(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
            bybitApiSocketStream.setClientName("用户"+byBitAccount.getApiSecurity()+"-TRADE-Socket");
            orderPrarms.put("category", bybitOrder.getCategory());
            orderPrarms.put("orderLinkId", bybitOrder.getOrderLinkId());
            orderPrarms.put("op", "order.cancel");
            WebSocket tradeChannelStream = bybitApiSocketStream.getTradeChannelStream(orderPrarms, V5_TRADE);
            bybitApiSocketStream.sendSubscribeMessage(tradeChannelStream,orderPrarms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public String getExchangeName() {
        return ExchangeType.BYBIT.getName();
    }

    @SneakyThrows
    @Override
    public void initSocket(Message account) {
        JSONObject from = JSONObject.from(ProxyUtils.getRealObjectFromProxy(account.getData()));
        JSONObject account1 = from.getJSONObject("account");
        ByBitAccount byBitAccount = account1.to(ByBitAccount.class);
        WebsocketStreamClient bybitApiSocketStream = byBitClientCenter.getBybitApiSocketStream(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
        bybitApiSocketStream.setClientName("用户"+byBitAccount.getApiSecurity()+"-TRADE-Socket");
        bybitApiSocketStream.getTradeChannelStream(new HashMap<>(), V5_TRADE);
        log.info("已初始化 Bybit socket api {}",byBitAccount.getApiSecurity());

        WebsocketStreamClient bybitApiSocketStream1 = byBitClientCenter.getBybitApiSocketStream(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
        bybitApiSocketStream.setClientName("用户"+byBitAccount.getApiSecurity()+"-TRADE-Socket");
        bybitApiSocketStream1.getTradeChannelStream(new HashMap<>(), V5_TRADE);
    }
}
