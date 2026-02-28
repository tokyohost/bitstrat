package com.bitstrat.ai.handler;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.ai.constant.SocketConstant;
import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.ai.distuptor.MarketPriceDisruptor;
import com.bitstrat.ai.domain.CompareItem;
import com.bitstrat.ai.domain.StartCompareContext;
import com.bitstrat.ai.domain.AIWebsocketMsgData;
import com.bitstrat.ai.domain.vo.CompareWindowRecord;
import com.bitstrat.ai.handler.marketPriceCover.MarketPriceCover;
import com.bitstrat.config.ExchangeWebsocketProperties;
import com.bitstrat.config.wsClient.ConnectionOtherConfig;
import com.bitstrat.domain.Account;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.bitstrat.ai.constant.WsType.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 18:33
 * @Content
 */

@Slf4j
@Component
public class CompareHandler {

    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;
    @Autowired
    ExchangeWebsocketProperties exchangeWebsocketProperties;
    List<SubscriptMsgs> subscriptMsgs;
    Map<String, MarketPriceCover> marketPriceCoverMap;



    Map<String, SubscriptMsgs> msgsMap;
    public CompareHandler(List<SubscriptMsgs> subscriptMsgs,List<MarketPriceCover> marketPriceCovers) {
        this.subscriptMsgs = subscriptMsgs;
        msgsMap= this.subscriptMsgs.stream().collect(Collectors.toMap(item -> item.exchangeName().toLowerCase(), item -> item));
        marketPriceCoverMap = marketPriceCovers.stream().collect(Collectors.toMap(item -> item.exchangeName().toLowerCase(), item -> item));
    }

    @Async
    @EventListener(StartCompareContext.class)
    public void handleCompare(StartCompareContext startCompareContext) throws Exception {
        log.info("Start Compare Context: {}", startCompareContext);
        //市场数据不需要鉴权，创建一个随机id account
        Account account = new Account();
        account.setId(IdUtil.getSnowflakeNextId());
        startCompareContext.setAccount(account);
        Channel channel = startCompareContext.getChannel();
        channel.attr(SocketConstant.COMPARE_CONTEXT).set(startCompareContext);
        ScheduledFuture<?> scheduledCallbackMarketPriceFuture = startCompareContext.getChannelHandlerContext().executor().scheduleAtFixedRate(() -> {
            //每隔1s推流数据
            callbackMarketPrice(startCompareContext);

        }, 200, 200, TimeUnit.MILLISECONDS);
        channel.attr(SocketConstant.COMPARE_MARKET_PRICE_SCHEDULE).set(scheduledCallbackMarketPriceFuture);
        ScheduledFuture<?> scheduledspreadWindowMarketPriceFuture = startCompareContext.getChannelHandlerContext().executor().scheduleAtFixedRate(() -> {
            //每隔1s采样数据
            try{
                spreadWindowMarketPrice(startCompareContext);
            }catch (Exception e){
                e.printStackTrace();
            }

        }, 1, 1, TimeUnit.SECONDS);
        channel.attr(SocketConstant.COMPARE_SPREAD_RECORD_PRICE_SCHEDULE).set(scheduledspreadWindowMarketPriceFuture);
        //创建市价监听websocket
        List<CompareItem> compareList = startCompareContext.getCompareList();
        for (CompareItem compareItem : compareList) {
            String urlByExAndType;
            ConnectionOtherConfig connectionOtherConfig = new ConnectionOtherConfig();
            connectionOtherConfig.setSymbol(compareItem.getSymbol());
            connectionOtherConfig.setExchange(compareItem.getExchange());
            connectionOtherConfig.setType(compareItem.getType());
            connectionOtherConfig.setClientId(startCompareContext.getClientId());


            if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SWAP)) {
                //合约
                //发送订阅币对行情消息
                SubscriptMsgs exSubscriptMsg = msgsMap.get(compareItem.getExchange().toLowerCase());
                urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(compareItem.getExchange(), WebSocketType.LINER);
                String url = exSubscriptMsg.createSwapMarketMsg(compareItem.getSymbol(), urlByExAndType);
                exchangeConnectionManager.createConnection(account,startCompareContext.getClientId(),compareItem.getExchange(),WebSocketType.LINER, URI.create(url),channel,connectionOtherConfig,(channel1)->{
                    String swapMarketMsg = exSubscriptMsg.createSwapMarketMsg(compareItem.getSymbol());
                    if (StringUtils.isNotEmpty(swapMarketMsg)) {
                        exchangeConnectionManager.sendSubscriptMessage(startCompareContext.getClientId(),account.getId(), compareItem.getExchange(), WebSocketType.LINER, swapMarketMsg,null,channel1);
                    }
                });



            } else if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SPOT)) {
                //现货
                urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(compareItem.getExchange(), WebSocketType.LINER);
                exchangeConnectionManager.createConnection(account,startCompareContext.getClientId(),compareItem.getExchange(),WebSocketType.LINER, URI.create(urlByExAndType),channel,connectionOtherConfig,(channel1)->{});
            }

        }




    }

    private void spreadWindowMarketPrice(StartCompareContext startCompareContext) {
        CompareWindowRecord compareWindowRecord = startCompareContext.getCompareWindowRecord();

        List<CompareItem> compareList = startCompareContext.getCompareList();
        long t = System.currentTimeMillis();
        if (compareList.size() >= 2) {
            CompareItem compareItemA = compareList.get(0);
            MarketPriceDisruptor disruptorByExAndSymbolA = MarketCompareStore.getDisruptorByExAndSymbol(startCompareContext.getClientId(),
                compareItemA.getSymbol(), compareItemA.getExchange(), compareItemA.getType());
            MarketPrice latestPriceA = disruptorByExAndSymbolA.getLatestPrice();

            CompareItem compareItemB = compareList.get(1);
            MarketPriceDisruptor disruptorByExAndSymbolB = MarketCompareStore.getDisruptorByExAndSymbol(startCompareContext.getClientId(),
                compareItemB.getSymbol(), compareItemB.getExchange(), compareItemB.getType());
            MarketPrice latestPriceB = disruptorByExAndSymbolB.getLatestPrice();
            if(Objects.isNull(latestPriceA) || Objects.isNull(latestPriceB)){
                return;
            }


            compareWindowRecord.handlePriceUpdate(latestPriceA.getPrice(), latestPriceB.getPrice(), t);
            Double[] maxMinAB = compareWindowRecord.getMaxMin(5);
            Double[] maxMin10AB = compareWindowRecord.getMaxMin(10);
            Double[] maxMin20AB = compareWindowRecord.getMaxMin(20);
            Double[] maxMin30AB = compareWindowRecord.getMaxMin(30);
            AIWebsocketMsgData<List<Double[]>> marketPriceWebsocketMsgData = new AIWebsocketMsgData<>();
            marketPriceWebsocketMsgData.setType(marketPriceAnalysis);
            marketPriceWebsocketMsgData.setData(List.of(maxMinAB, maxMin10AB, maxMin20AB, maxMin30AB));
            startCompareContext.getChannel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(marketPriceWebsocketMsgData)));
        }

    }

    /**
     * 从distuptor 中获取最新的价格回调发送
     * @param startCompareContext
     */
    private void callbackMarketPrice(StartCompareContext startCompareContext) {
        List<CompareItem> compareList = startCompareContext.getCompareList();
        long t = System.currentTimeMillis();
        for (CompareItem compareItem : compareList) {
            MarketPriceDisruptor disruptorByExAndSymbol = MarketCompareStore.getDisruptorByExAndSymbol(startCompareContext.getClientId(),
                compareItem.getSymbol(), compareItem.getExchange(), compareItem.getType());
            MarketPrice latestPrice = disruptorByExAndSymbol.getLatestPrice();
            if(Objects.nonNull(latestPrice)){
                if (startCompareContext.getChannel().isActive()) {
                    latestPrice.setServerTimestamp(t);
                    AIWebsocketMsgData<MarketPrice> marketPriceWebsocketMsgData = new AIWebsocketMsgData<>();
                    marketPriceWebsocketMsgData.setType(marketPrice);
                    marketPriceWebsocketMsgData.setData(latestPrice);
                    startCompareContext.getChannel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(marketPriceWebsocketMsgData)));
                }
            }
        }

    }


    @Async
    @EventListener(LinerReceiveMsg.class)
    public void handleLinerMsg(LinerReceiveMsg linerReceiveMsg) {
//        log.info("Liner Receive Msg: {}", linerReceiveMsg);
        Channel bridgChannel = linerReceiveMsg.getConnectionConfig().getBridgChannel();

        String exCover = linerReceiveMsg.getEx().toLowerCase();
        MarketPriceCover marketPriceCover = marketPriceCoverMap.get(exCover);
        if(Objects.isNull(marketPriceCover)){
            log.warn("No MarketPriceCover found for exchange: {}", exCover);
            return;
        }
        List<MarketPrice> marketPrices = marketPriceCover.coverLinerMsg(linerReceiveMsg);
        ConnectionConfig connectionConfig = linerReceiveMsg.getConnectionConfig();
        ConnectionOtherConfig otherConfig = connectionConfig.getOtherConfig();
        if (!otherConfig.isServerWatch()) {
            for (MarketPrice marketPrice : marketPrices) {
                MarketPriceDisruptor disruptorByExAndSymbol = MarketCompareStore.getDisruptorByExAndSymbol(otherConfig.getClientId(), marketPrice.getSymbol(), otherConfig.getExchange(), otherConfig.getType());
                if(Objects.nonNull(disruptorByExAndSymbol)){
                    disruptorByExAndSymbol.publishPrice(marketPrice,otherConfig.getSide());
                }
            }
        }



//        //如果桥接的Channel 不为空，直接发给这个channel
//        if (Objects.nonNull(bridgChannel)) {
//            if (bridgChannel.isActive()) {
//                bridgChannel.writeAndFlush(new TextWebSocketFrame(linerReceiveMsg.getMsg()));
//            }
//        }

    }
}
