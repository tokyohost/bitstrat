package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.OrderInfo;
import com.bitstrat.domain.msg.AccountData;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.ActiveLossPointData;
import com.bitstrat.domain.msg.SubscribeOrder;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.server.MessageData;
import com.bitstrat.handler.WebSocketClientHandler;
import com.bitstrat.service.*;
import com.bitstrat.store.OrderStore;
import com.bitstrat.store.RoleCenter;
import com.bitstrat.store.ScheduleStopLoss;
import com.bitstrat.store.TaskCenter;
import com.bitstrat.task.MarketTask;
import com.bitstrat.task.OrderBookTask;
import com.bitstrat.task.OrderTask;
import com.bitstrat.task.TaskManager;
import com.bitstrat.utils.ProxyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static com.bitstrat.constant.MessageType.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 18:16
 * @Content
 */
@Slf4j
@Service
public class MessageProcessImpl implements MessageProcess {
    @Value("${node.exchange-name}")
    private List<String> exchangeName;
    @Value("${node.auth}")
    private String auth;

    @Autowired
    TaskManager taskManager;

    @Autowired
    WebSocketClientHandler webSocketClientHandler;

    @Autowired
    ExchangeOptCenter exchangeOptCenter;

    @Autowired
    RoleCenter roleCenter;


    @Autowired
        @Lazy
    SymbolService symbolService;

    @Override
    public void processMessage(Message message, ChannelHandlerContext ctx) {
        switch (message.getType()) {
            case SUBSCRIPTION_SYMBOL:
                log.info("处理订阅");
                subscription(message);
                break;
            case INIT_EXCHANGE:
                log.info("处理初始化");
                initExchange(message);
                break;
            case SUBSCRIPTION_ORDER:
                log.info("处理订单订阅");
                subscriptionOrder(message);
                break;
            case CREATE_ORDER:
                log.info("创建订单");
                createOrder(message);
                break;
            case UPDATE_LOSS_POINT:
                log.info("更新滑点");
                updateLossPoint(message);
                webSocketClientHandler.triggerReportInfo();
                break;
            case AUTH:
                log.info("重新上送auth 消息");
                sendAuth(message,ctx);
                break;
        }



    }

    private void sendAuth(Message receive, ChannelHandlerContext ctx) {
        log.info("收到服务器发送的auth 请求，正在发送auth");
        Channel channel = ctx.channel();
        // 发送AUTH
        for (String exchange : exchangeName) {
            Message message = new Message();
            message.setExchangeName(exchange);
            message.setAuth(auth);
            message.setType(MessageType.AUTH);
            message.setTimestamp(System.currentTimeMillis());
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
            }
        }
        log.info("auth 已发送完毕");

    }

    private void subscriptionOrder(Message message) {


    }

    @SneakyThrows
    private void initExchange(Message message) {
        String exchangeName = message.getExchangeName();
        if (message.getType().equals(INIT_EXCHANGE)) {
             ExchangeService exchangeService = exchangeOptCenter.getExchangeService(exchangeName);

            exchangeService.initSocket(message);
        }

    }

    @SneakyThrows
    private void updateLossPoint(Message message) {
        Object realObjectFromProxy = ProxyUtils.getRealObjectFromProxy(message.getData());
        JSONObject from = JSONObject.from(realObjectFromProxy);
        ActiveLossPointData activeLossPointData = from.to(ActiveLossPointData.class);
        List<ActiveLossPoint> activeLossPoints = activeLossPointData.getActiveLossPoints();
        HashSet<String> exchangeNames = new HashSet<>();
        if (activeLossPointData.isClearAll()) {
            roleCenter.getSymbolLossPointMap().clear();
            //停止持仓监控
            for (String orderId : OrderStore.getOrderHolder().keySet()) {
                ScheduledFuture<?> remove = BybitService.getQueryOrderScheduledMap().remove(orderId);
                if(remove != null) {
                    log.info("已停止订单持仓监控 {}",orderId);
                    remove.cancel(true);
                }
                ScheduledFuture<?> stoplossTask = ScheduleStopLoss.getStopLossScheduled().remove(orderId);
                if(stoplossTask != null) {
                    log.info("已停止实时止损监控 {}",orderId);
                    stoplossTask.cancel(true);
                }
//                OrderInfo orderByLinkId = OrderStore.getOrderByLinkId(orderId);
//                String apiSecurity = orderByLinkId.getByBitAccount().getApiSecurity();
                //停止订单监控
//                ConcurrentHashMap<String, OrderTask> orderTaskMap = TaskCenter.getOrderTaskMap();
//                OrderTask orderTask = orderTaskMap.get(apiSecurity);
//                if (orderTask != null) {
//                    orderTask.stop();
//                }



            }
            OrderStore.getOrderHolder().clear();
            roleCenter.clear();

            log.info("已清空所有");
            return;
        }
        if (activeLossPointData.isDelete()) {
            for (ActiveLossPoint activeLossPoint : activeLossPoints) {
                String key = roleCenter.getKey(activeLossPoint.getExchangeName(), activeLossPoint.getSymbol(), activeLossPoint.getId());
                ActiveLossPoint remove = roleCenter.getSymbolLossPointMap().remove(key);
                log.info("已移除滑点 {}", JSONObject.toJSONString(remove));

            }
            return;
        }
        if (activeLossPoints.isEmpty()) {
//            roleCenter.getSymbolLossPointMap().clear();
        }else{
//            roleCenter.getSymbolLossPointMap().clear();
            for (ActiveLossPoint activeLossPoint : activeLossPoints) {
                if(this.exchangeName.contains(activeLossPoint.getExchangeName())){
                    log.info("更新滑点配置 {}",JSONObject.toJSONString(activeLossPoint));
                    roleCenter.put(activeLossPoint.getExchangeName(),activeLossPoint.getSymbol(),activeLossPoint.getId(),auth,activeLossPoint);

                    exchangeNames.add(activeLossPoint.getExchangeName());

                }
            }
        }
        //开始监听
        for (String name : exchangeNames) {
            symbolService.traggerWatchSymbol(name);
        }

        symbolService.initWebsocketClient();
    }

    private void createOrder(Message message) {
        String exchangeName = message.getExchangeName();
        ExchangeService exchangeService = exchangeOptCenter.getExchangeService(exchangeName);
        exchangeService.buy(message);

    }

    @SneakyThrows
    public synchronized void subscription(Message  message) {
        String exchangeName = message.getExchangeName().toLowerCase();

        ConcurrentHashMap<String, MarketTask> marketTaskMap = TaskCenter.getMarketTaskMap();

        MarketTask remove = marketTaskMap.remove(exchangeName);
        MarketTask marketTaskStrategy = taskManager.getMarketTaskStrategy(exchangeName);
        if (remove != null) {
            remove.stop();
        }
        if(marketTaskStrategy != null) {
            try {
//                marketTaskStrategy.run(JSONObject.from(ProxyUtils.getRealObjectFromProxy(message.getData())));
                marketTaskStrategy.run(JSONObject.from(ProxyUtils.getRealObjectFromProxy(message.getData())));
                marketTaskMap.put(exchangeName, marketTaskStrategy);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
//        // 开始拉orderbook
        ConcurrentHashMap<String, OrderBookTask> orderBookTaskMap = TaskCenter.getOrderBookTaskMap();

        OrderBookTask removeOrderBook = orderBookTaskMap.remove(exchangeName);
        if (removeOrderBook != null) {
            removeOrderBook.stop();
        }
        OrderBookTask orderBookTaskStrategy = taskManager.getOrderBookTaskStrategy(exchangeName);
        if(orderBookTaskStrategy != null) {
            try {
                orderBookTaskStrategy.run(JSONObject.from(ProxyUtils.getRealObjectFromProxy(message.getData())));
                orderBookTaskMap.put(exchangeName, orderBookTaskStrategy);
            } catch (Exception e) {
                throw new RuntimeException(e);

            }
        }


        log.info("已订阅完成");
    }
}
