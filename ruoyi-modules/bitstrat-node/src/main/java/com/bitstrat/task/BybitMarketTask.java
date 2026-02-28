package com.bitstrat.task;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.bybit.ByBitReconnectSocketClient;
import com.bitstrat.cache.NodeSymbolService;
import com.bitstrat.client.WebSocketClient;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.MessageType;
import com.bitstrat.constant.SymbolType;
import com.bitstrat.domain.OrderInfo;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.MarketPrice;
import com.bitstrat.domain.msg.SubscribeSymbol;
import com.bitstrat.domain.server.Message;
import com.bitstrat.handler.ByBitSocketMarketHandler;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.store.OrderBooksStore;
import com.bitstrat.store.OrderStore;
import com.bitstrat.store.RoleCenter;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.log.LogOption;
import com.bybit.api.client.service.BybitApiClientFactory;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
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
public class BybitMarketTask implements MarketTask {
    Thread thread = null;
    WebSocket socket = null;
    @Autowired
    WebSocketClient webSocketClient;
    @Autowired
    ByBitClientCenter byBitClientCenter;
    @Value("${node.auth}")
    private String auth;
    @Value("${loss-point.order-cooling-time}")
    Long orderCoolingTime;
    @Autowired
    RoleCenter roleCenter;
    ByBitReconnectSocketClient client;
    @Autowired
    ByBitSocketMarketHandler marketHandler;
    @Autowired
    NodeSymbolService nodeSymbolService;
    @Autowired
    RedissonClient redissonClient;


    public void run(JSONObject params) {

        SubscribeSymbol subscribeSymbol = params.to(SubscribeSymbol.class);
        List<String> symbolsSource = subscribeSymbol.getSymbols();
        if (symbolsSource == null || symbolsSource.size() == 0) {
            return;
        }
        List<String> symbols = symbolsSource.stream().map(item->{
            if(!item.endsWith("USDT")){
                return item + "USDT";
            }
            return item;
        }).collect(Collectors.toList());
        RLock exchangeLock = redissonClient.getLock(this.getExchangeName());
        if (exchangeLock.tryLock()) {
            try {
                final List<String> symbolTickers = symbols.stream().map(item -> "tickers." + item).collect(Collectors.toList());
                Set<String> symbolsByNode = nodeSymbolService.getSymbolsByNode(auth);
                for (String s : symbolsByNode) {
                    if (s.startsWith(ExchangeType.BYBIT.getName())) {
                        nodeSymbolService.deleteSymbol(s);
                    }
                }
                //处理订阅
                for (String symbol : symbols) {
                    nodeSymbolService.addSymbolToNode(auth,ExchangeType.BYBIT.getName()+":"+symbol);
                }

                    //订阅订单成交
//        symbolTickers.add("order");
                thread = new Thread(() -> {
//            var client = BybitApiClientFactory.newInstance(BybitApiConfig.STREAM_MAINNET_DOMAIN, true).newWebsocketClient();
                    if (client == null) {
                        client = new ByBitReconnectSocketClient((String) null, (String) "null", BybitApiConfig.STREAM_MAINNET_DOMAIN, 20, "-1", true, LogOption.SLF4J.getLogOptionType(), marketHandler);

                    }
                    client.setClientName("币对 " + symbols.stream().collect(Collectors.joining(",")) + " 实时价格监听Socket", true);

//            client.setMessageHandler(marketHandler);

                    // Ticker
                    if (subscribeSymbol.getSymbolType().equalsIgnoreCase(SymbolType.LINER)) {
                        socket = client.getPublicChannelStream(symbolTickers, BybitApiConfig.V5_PUBLIC_LINEAR);
                        client.sendSubscribeMessage(socket, symbolTickers);
                    } else {
                        log.error("不支持的币种类型");
                    }
                });
                thread.setDaemon(true);
                thread.start();
            } finally {
                exchangeLock.unlock();
            }
        }


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
        if (socket != null) {
            client.close(1000, "Normal Closure");
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
