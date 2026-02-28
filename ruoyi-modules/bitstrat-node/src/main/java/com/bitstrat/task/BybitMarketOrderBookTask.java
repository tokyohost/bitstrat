package com.bitstrat.task;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.bybit.ByBitReconnectSocketClient;
import com.bitstrat.client.WebSocketClient;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.MessageType;
import com.bitstrat.constant.SymbolType;
import com.bitstrat.domain.OrderBook;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.MarketPrice;
import com.bitstrat.domain.msg.SubscribeSymbol;
import com.bitstrat.domain.server.Message;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.store.OrderBooksStore;
import com.bitstrat.store.RoleCenter;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.private_channel.WebSocketOrderMessage;
import com.bybit.api.client.domain.websocket_message.public_channel.PublicOrderBookData;
import com.bybit.api.client.domain.websocket_message.public_channel.WebSocketTickerMessage;
import com.bybit.api.client.domain.websocket_message.public_channel.WebsocketOrderbookMessage;
import com.bybit.api.client.log.LogOption;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 17:34
 * @Content
 */

@Slf4j
@Component
public class BybitMarketOrderBookTask implements OrderBookTask {
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




    public void run(JSONObject params) {

        SubscribeSymbol subscribeSymbol = params.to(SubscribeSymbol.class);
        List<String> symbols = subscribeSymbol.getSymbols();
        if(symbols == null || symbols.size() == 0) {
            return;
        }
        final List<String> symbolTickers = symbols.stream().map(item -> "orderbook.50." + item).collect(Collectors.toList());
        thread = new Thread(() -> {
//            var client = BybitApiClientFactory.newInstance(BybitApiConfig.STREAM_MAINNET_DOMAIN, true).newWebsocketClient();

            ByBitReconnectSocketClient client
                = new ByBitReconnectSocketClient((String) null,(String) "null",BybitApiConfig.STREAM_MAINNET_DOMAIN,20,"-1",true, LogOption.SLF4J.getLogOptionType(),null);

            client.setMessageHandler(message -> {
                var orderData = (new ObjectMapper()).readValue(message, WebsocketOrderbookMessage.class);
                // Process message data here
//                log.info("lastPrice: {}", JSONObject.toJSONString(tickerData));
//                JSONObject orderdata = JSONObject.parseObject(message);
                if (orderData.getTopic() == null) {
                    return;
                }
                if(orderData.getTopic().startsWith("orderbook.")) {
                    PublicOrderBookData data = orderData.getData();
                    if (orderData.getType().equals("snapshot")) {
//                    log.debug("snapshot {}",JSONObject.toJSONString(data));

                        OrderBooksStore.initOrderBook(data,data.getS());
                    } else if (orderData.getType().equals("delta")) {
//                    log.debug("delta {}",JSONObject.toJSONString(data));
                        OrderBooksStore.updateOrderBook(data,data.getS());
                    }
                }

            });

            // Ticker
            if (subscribeSymbol.getSymbolType().equalsIgnoreCase(SymbolType.LINER)) {
                //监听订单深度
                if (socket != null) {
                    boolean normalClosure = socket.close(1000, "Normal Closure");
                    socket = client.getPublicChannelStream(symbolTickers, BybitApiConfig.V5_PUBLIC_LINEAR);
                }else{
                    socket = client.getPublicChannelStream(symbolTickers, BybitApiConfig.V5_PUBLIC_LINEAR);
                }

            } else {
                log.error("不支持的币种类型");
            }
        });
        thread.setDaemon(true);
        thread.start();


    }


    private void callbackToServer(String symbol, String lastPrice) {
        Message marketMsg = new Message();
        log.info("symbol {} lastPrice: {}", symbol, lastPrice);
        marketMsg.setExchangeName(ExchangeType.BYBIT.getName());
        marketMsg.setType(MessageType.MARKET_LAST_PRICE);
        marketMsg.setTimestamp(System.currentTimeMillis());
        MarketPrice marketPrice = new MarketPrice();
        marketPrice.setLastPrice(lastPrice);
        marketPrice.setSymbol(symbol);
        marketMsg.setData(marketPrice);
        Channel channel = webSocketClient.getChannel();
        channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(marketMsg)));
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
