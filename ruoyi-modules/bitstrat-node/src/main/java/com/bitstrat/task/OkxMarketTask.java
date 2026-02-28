package com.bitstrat.task;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.bybit.ByBitReconnectSocketClient;
import com.bitstrat.cache.NodeSymbolService;
import com.bitstrat.client.WebSocketClient;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.MessageType;
import com.bitstrat.constant.SymbolType;
import com.bitstrat.domain.msg.MarketPrice;
import com.bitstrat.domain.msg.SubscribeSymbol;
import com.bitstrat.domain.server.Message;
import com.bitstrat.handler.ByBitSocketMarketHandler;
import com.bitstrat.okx.OkxWebSocketClient;
import com.bitstrat.okx.model.Constant.OkxChannelConstant;
import com.bitstrat.okx.model.Constant.SubscriptOp;
import com.bitstrat.okx.model.OkxSubscriptMsg;
import com.bitstrat.okx.model.SubscriptArg;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.store.RoleCenter;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.log.LogOption;
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
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 17:34
 * @Content
 */

@Slf4j
@Component
public class OkxMarketTask implements MarketTask {

    @Value("${node.auth}")
    private String auth;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    OkxWebSocketClient client;
    @Autowired
    ByBitSocketMarketHandler marketHandler;
    @Autowired
    NodeSymbolService nodeSymbolService;

    //okx 有统一封装的netty 客户端
    public void run(JSONObject params) {

        SubscribeSymbol subscribeSymbol = params.to(SubscribeSymbol.class);
        List<String> symbols = subscribeSymbol.getSymbols();
        if(symbols == null || symbols.size() == 0) {
            return;
        }
        //先全部取消订阅
        RLock exchangeLock = redissonClient.getLock(this.getExchangeName());
        if(exchangeLock.tryLock()) {
            try{
                for (String symbol : symbols) {
                    if (SymbolType.LINER.equalsIgnoreCase(subscribeSymbol.getSymbolType())) {
                        //处理合约
                        symbol = symbol.toUpperCase() + "-USDT-SWAP";
                        //处理订阅
                        Set<String> nodeId = nodeSymbolService.getNodesBySymbol(symbol);
                        if (nodeId.contains(auth)) {
                            //本机已订阅，可以重新订阅
                            // 订阅
                            OkxSubscriptMsg okxSubscriptMsg = new OkxSubscriptMsg();
                            okxSubscriptMsg.setOp(SubscriptOp.SUBSCRIBE);
                            SubscriptArg subscriptArg = new SubscriptArg();
                            subscriptArg.setChannel(OkxChannelConstant.MARK_PRICE);
                            //币对
                            subscriptArg.setInstId(symbol);
                            okxSubscriptMsg.setArgs(List.of(subscriptArg));
                            client.send(JSONObject.toJSONString(okxSubscriptMsg));
                            nodeSymbolService.addSymbolToNode(auth, ExchangeType.OKX.getName()+":"+symbol);
                        } else if (nodeId.size() == 0) {
                            //没有订阅，本机订阅
                            // 订阅
                            OkxSubscriptMsg okxSubscriptMsg = new OkxSubscriptMsg();
                            okxSubscriptMsg.setOp(SubscriptOp.SUBSCRIBE);
                            SubscriptArg subscriptArg = new SubscriptArg();
                            subscriptArg.setChannel(OkxChannelConstant.MARK_PRICE);
                            //币对
                            subscriptArg.setInstId(symbol);
                            okxSubscriptMsg.setArgs(List.of(subscriptArg));
                            client.send(JSONObject.toJSONString(okxSubscriptMsg));
                            nodeSymbolService.addSymbolToNode(auth, ExchangeType.OKX.getName()+":"+symbol);
                        }else{

                        }
                    }
                }

            }finally {
                exchangeLock.unlock();
            }
        }


    }


    @Override
    public void stop() {
//        nodeSymbolService.getSymbolsByNode(auth).forEach(symbol -> {
//            // 取消订阅
//            OkxSubscriptMsg okxSubscriptMsg = new OkxSubscriptMsg();
//            okxSubscriptMsg.setOp(SubscriptOp.UNSUBSCRIBE);
//            SubscriptArg subscriptArg = new SubscriptArg();
//            subscriptArg.setChannel(OkxChannelConstant.MARK_PRICE);
//            //币对
//            subscriptArg.setInstId(symbol);
//            okxSubscriptMsg.setArgs(List.of(subscriptArg));
//            client.send(JSONObject.toJSONString(okxSubscriptMsg));
//            log.info("取消 okx {}  订阅", symbol);
//        });
//        nodeSymbolService.deleteAllSymbolsByNode(auth);
//        log.info("已取消所有订阅");
        log.info("okx 不需要清楚");
    }

    @Override
    public String getExchangeName() {
        return ExchangeType.OKX.getName();
    }
}
