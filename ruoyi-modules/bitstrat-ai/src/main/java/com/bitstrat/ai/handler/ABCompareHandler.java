package com.bitstrat.ai.handler;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.ai.constant.SocketConstant;
import com.bitstrat.ai.constant.WsType;
import com.bitstrat.ai.distuptor.ABDisruptor;
import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.ai.distuptor.MarketPriceDisruptor;
import com.bitstrat.ai.domain.AIWebsocketMsgData;
import com.bitstrat.ai.domain.CompareItem;
import com.bitstrat.ai.domain.ExtConfig;
import com.bitstrat.ai.domain.StartCompareContext;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.ai.domain.serverWatch.ServerWatchContext;
import com.bitstrat.ai.domain.serverWatch.StopWatchContext;
import com.bitstrat.ai.domain.vo.CompareWindowRecord;
import com.bitstrat.ai.handler.marketPriceCover.MarketPriceCover;
import com.bitstrat.config.ExchangeWebsocketProperties;
import com.bitstrat.config.wsClient.ConnectionOtherConfig;
import com.bitstrat.constant.ApiTypeConstant;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.Event.AckCreatePrivateWebsocketEvent;
import com.bitstrat.domain.WebsocketMsgData;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.SubscriptMsgType;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.websocket.dto.AccountAutoSend;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.bitstrat.ai.constant.AbTaskStatus.AB_STOP;
import static com.bitstrat.ai.constant.WsType.marketPrice;
import static com.bitstrat.ai.constant.WsType.marketPriceAnalysis;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 18:33
 * @Content
 */

@Slf4j
@Component
public class ABCompareHandler {

    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;
    @Autowired
    ExchangeWebsocketProperties exchangeWebsocketProperties;
    List<SubscriptMsgs> subscriptMsgs;
    Map<String, MarketPriceCover> marketPriceCoverMap;
    /**
     * 这个线程池主要给用户端定时更新AB任务状态
     */
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);


    Map<String, SubscriptMsgs> msgsMap;

    public ABCompareHandler(List<SubscriptMsgs> subscriptMsgs, List<MarketPriceCover> marketPriceCovers) {
        this.subscriptMsgs = subscriptMsgs;
        msgsMap = this.subscriptMsgs.stream().collect(Collectors.toMap(item -> item.exchangeName().toLowerCase(), item -> item));
        marketPriceCoverMap = marketPriceCovers.stream().collect(Collectors.toMap(item -> item.exchangeName().toLowerCase(), item -> item));
    }

    /**
     * 发送当前活跃的任务列表
     * @param autoSend
     */
//    @Async
//    @EventListener(AccountAutoSend.class)
//    public void accountAutoSendTaskList(AccountAutoSend autoSend) {
//        List<ABDisruptor> priceListenerList = MarketABOrderStore.getPriceListenerList(autoSend.getUserId());
//        WebsocketMsgData<List<ABOrderTask>> listWebsocketMsgData = new WebsocketMsgData<>();
//        listWebsocketMsgData.setType(WsType.AB_TASK_ALL);
//        listWebsocketMsgData.setUserId(autoSend.getUserId());
//        listWebsocketMsgData.setData(priceListenerList.stream().map(ABDisruptor::getAbOrderTask).collect(Collectors.toList()));
//        WebSocketUtils.sendMessage(autoSend.getUserId(),listWebsocketMsgData.toJSONString());
//    }


    /**
     * 用户一建立连接触发定时任务
     *
     * @param autoSend
     */
    @Async
    @EventListener(AccountAutoSend.class)
    public void accountAutoSend(AccountAutoSend autoSend) {
        List<ABDisruptor> allTask = MarketABOrderStore.getByUserId(autoSend.getUserId());
        //发送监听状态
        List<ABOrderTask> tasks = new ArrayList<>();
        for (ABDisruptor disruptor : allTask) {
            ABOrderTask currTasl = disruptor.getAbOrderTask();
            if (Objects.isNull(currTasl)) {
                log.warn("ABOrderTask is null, cannot send compare status");
                return;
            }
            currTasl.setDelyA(disruptor.getDisruptorA().getConnectionOtherConfig().getDely());
            currTasl.setDelyB(disruptor.getDisruptorB().getConnectionOtherConfig().getDely());
            currTasl.setServerTime(new Date());
            tasks.add(currTasl);
        }

        AIWebsocketMsgData<List<ABOrderTask>> socketData = new AIWebsocketMsgData<>();
        socketData.setType(WsType.AB_TASK_CALLBACK);
        socketData.setData(tasks);
        WebSocketUtils.sendMessage(autoSend.getUserId(), socketData.toJSONString());

    }

    @Async
    @EventListener(StopWatchContext.class)
    public void handleStop(StopWatchContext stopWatchContext) throws Exception {
        //关闭
        log.info("Stop serverWatch Context: {}", stopWatchContext);
        ABDisruptor priceListener = MarketABOrderStore.stopPriceListener(stopWatchContext.getAbOrderTask());
        if (Objects.nonNull(priceListener)) {
            ABOrderTask abOrderTask = priceListener.getAbOrderTask();
            abOrderTask.getOperate().setStatus(AB_STOP);
            priceListener.close();
        }

    }

//    @Async
//    @EventListener(ServerWatchContext.class)
    @Deprecated()
    public void handleStart(ServerWatchContext serverWatchContext) throws Exception {
        log.info("Start serverWatch Context: {}", serverWatchContext);
        //创建市价监听websocket
        ABDisruptor priceListener = MarketABOrderStore.getPriceListener(serverWatchContext.getAbOrderTask());
        ABOrderTask abOrderTask = serverWatchContext.getAbOrderTask();
        priceListener.setAbOrderTask(abOrderTask);
        //A

        CompareItem compareItemA = new CompareItem();
        compareItemA.setExchange(abOrderTask.getExchangeA());
        compareItemA.setSymbol(abOrderTask.getSymbolA());
        compareItemA.setType(abOrderTask.getTypeA());
        compareItemA.setAccount(abOrderTask.getAccountA());

        initWatch(compareItemA, serverWatchContext.getUserId(), priceListener.getDisruptorA(), 1,priceListener);
        //B

        CompareItem compareItemB = new CompareItem();
        compareItemB.setExchange(abOrderTask.getExchangeB());
        compareItemB.setSymbol(abOrderTask.getSymbolB());
        compareItemB.setType(abOrderTask.getTypeB());
        compareItemB.setAccount(abOrderTask.getAccountB());
        initWatch(compareItemB, serverWatchContext.getUserId(), priceListener.getDisruptorB(), 2,priceListener);
        log.info("End serverWatch Context: {}", serverWatchContext);

        //发送私有websocket 监听事件
        AckCreatePrivateWebsocketEvent ackCreatePrivateWebsocketEventA = new AckCreatePrivateWebsocketEvent();
        ackCreatePrivateWebsocketEventA.setAccount(abOrderTask.getAccountA());
        ackCreatePrivateWebsocketEventA.setExchangeName(abOrderTask.getExchangeA());
        SpringUtils.getApplicationContext().publishEvent(ackCreatePrivateWebsocketEventA);
        AckCreatePrivateWebsocketEvent ackCreatePrivateWebsocketEventB = new AckCreatePrivateWebsocketEvent();
        ackCreatePrivateWebsocketEventB.setAccount(abOrderTask.getAccountB());
        ackCreatePrivateWebsocketEventB.setExchangeName(abOrderTask.getExchangeB());
        SpringUtils.getApplicationContext().publishEvent(ackCreatePrivateWebsocketEventB);

    }


    private void initWatch(CompareItem compareItem, Long userId, MarketPriceDisruptor disruptor, int side,ABDisruptor abDisruptor) throws Exception {
        Account account = compareItem.getAccount();
        APITypeHelper.set(account.getType());
        try{
            if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SWAP)) {
                Channel channel = exchangeConnectionManager.getChannel(userId + "", compareItem.getAccount().getId(), compareItem.getExchange(), WebSocketType.LINER);
                if (channel != null && channel.isActive()) {
                    return;
                }
            } else if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SPOT)) {
                Channel channel = exchangeConnectionManager.getChannel(userId + "", compareItem.getAccount().getId(), compareItem.getExchange(), WebSocketType.LINER);
                if (channel != null && channel.isActive()) {
                    return;
                }
            }


            String urlByExAndType;
            ConnectionOtherConfig<ExtConfig> connectionOtherConfig = new ConnectionOtherConfig<ExtConfig>();
            connectionOtherConfig.setSymbol(compareItem.getSymbol());
            connectionOtherConfig.setExchange(compareItem.getExchange());
            connectionOtherConfig.setType(compareItem.getType());
            connectionOtherConfig.setServerWatch(true);
            connectionOtherConfig.setSide(side);
            connectionOtherConfig.setClientId(null);
            ExtConfig extConfig = new ExtConfig();
            extConfig.setMarketPriceDisruptor(disruptor);
            extConfig.setAbDisruptor(abDisruptor);
            connectionOtherConfig.setExtendConfig(extConfig);
            disruptor.setConnectionOtherConfig(connectionOtherConfig);


            if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SWAP)) {
                //合约
                //发送订阅币对行情消息
                SubscriptMsgs exSubscriptMsg = msgsMap.get(compareItem.getExchange().toLowerCase());
                urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(compareItem.getExchange(), WebSocketType.LINER);
                String url = exSubscriptMsg.createSwapMarketMsg(compareItem.getSymbol(), urlByExAndType);
                exchangeConnectionManager.createConnection(account, userId + "", compareItem.getExchange(), WebSocketType.LINER, URI.create(url), null, connectionOtherConfig,(channel)->{
                    String swapMarketMsg = exSubscriptMsg.createSwapMarketMsg(compareItem.getSymbol());
                    if (StringUtils.isNotEmpty(swapMarketMsg)) {
                        exchangeConnectionManager.sendSubscriptMessage(userId + "", account.getId(), compareItem.getExchange(), WebSocketType.LINER, swapMarketMsg,null,channel);
                    }
                });



            } else if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SPOT)) {
                //现货
                urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(compareItem.getExchange(), WebSocketType.LINER);
                exchangeConnectionManager.createConnection(account, userId + "", compareItem.getExchange(), WebSocketType.LINER, URI.create(urlByExAndType), null, connectionOtherConfig,(channel)->{});
            }
        }finally {
            APITypeHelper.clear();
        }

    }


    @Async
    @EventListener(LinerReceiveMsg.class)
    public void handleServerWatchLinerMsg(LinerReceiveMsg linerReceiveMsg) {
//        log.info("Liner Receive Msg: {}", linerReceiveMsg);

        String exCover = linerReceiveMsg.getEx().toLowerCase();
        MarketPriceCover marketPriceCover = marketPriceCoverMap.get(exCover);
        if (Objects.isNull(marketPriceCover)) {
            log.warn("No MarketPriceCover found for exchange: {}", exCover);
            return;
        }
        List<MarketPrice> marketPrices = marketPriceCover.coverLinerMsg(linerReceiveMsg);
        ConnectionConfig connectionConfig = linerReceiveMsg.getConnectionConfig();
        ConnectionOtherConfig<ExtConfig> otherConfig = (ConnectionOtherConfig<ExtConfig>) connectionConfig.getOtherConfig();
        if (otherConfig.isServerWatch()) {
            ExtConfig extendConfig = otherConfig.getExtendConfig();
            MarketPriceDisruptor marketPriceDisruptor = extendConfig.getMarketPriceDisruptor();
            int side = marketPriceDisruptor.getSide();
            for (MarketPrice marketPrice : marketPrices) {
                marketPrice.setSide(side);
                marketPrice.setAbDisruptor(extendConfig.getAbDisruptor());
                marketPriceDisruptor.publishPrice(marketPrice);
            }
        }


    }
}
