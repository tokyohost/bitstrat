package com.bitstrat.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.OrderInfo;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.store.ByBitClientCenter;
import com.bitstrat.store.OrderStore;
import com.bitstrat.store.ScheduleStopLoss;
import com.bitstrat.utils.BitStratThreadFactory;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiPositionRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class BybitService {
    @Autowired
        @Lazy
    ScheduleStopLoss scheduleStopLoss;
    @Autowired
    ByBitClientCenter byBitClientCenter;

    public static ConcurrentHashMap<String, ScheduledFuture<?>> getQueryOrderScheduledMap() {
        return queryOrderScheduledMap;
    }
    private  static ConcurrentHashMap<String, ScheduledFuture<?>> queryOrderScheduledMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, BitStratThreadFactory.forName("queryOrder-Schedule-scheduler"));
    private final ExecutorService executorService = Executors.newFixedThreadPool(1, BitStratThreadFactory.forName("queryOrder-scheduler"));


    //    public void queryOrderStatus(OrderInfo orderByApiKey) {
//        long suffTime = System.currentTimeMillis() - orderByApiKey.getCreateTime();
//        //5秒后开始查询
//        if (suffTime >= 5 * 1000) {
//            executorService.submit(()->{
//                ByBitAccount account = orderByApiKey.getByBitAccount();
//                BybitApiClientFactory bybitApiClientFactory = byBitClientCenter.getBybitApiClientFactory(account.getApiSecurity(), account.getApiPwd(), BybitApiConfig.MAINNET_DOMAIN, false);
//                BybitApiTradeRestClient client = bybitApiClientFactory.newTradeRestClient();
//                TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
//                    .category(CategoryType.LINEAR)
//                    .orderLinkId(orderByApiKey.getOrderId()).build();
//                Object openOrders = client.getOpenOrders(tradeOrderRequest);
//                JSONObject from = JSONObject.from(openOrders);
//                log.info("查询订单状态 {} {}", orderByApiKey.getOrderId(), from);
//                if (from.getInteger("retCode") == 0) {
//                    JSONObject result = from.getJSONObject("result");
//                    JSONArray list = result.getJSONArray("list");
//                    if (list.isEmpty()) {
//                        OrderStore.remove(orderByApiKey);
//                    }else{
//                        for (Object o : list) {
//                            JSONObject ordered = JSONObject.from(o);
//                            String orderStatus = ordered.getString("orderStatus");
//                            List<String> cancelStatus = OrderStore.getCancelStatus();
//                            if (cancelStatus.contains(orderStatus)) {
//                                //已取消
//                                log.error("订单已取消，删除持仓状态 {}", ordered);
//                                OrderStore.remove(orderByApiKey);
//                            }else{
//                                //正常
//
//                            }
//                        }
//                    }
//                }
//
//
//            });
//        }
//
//    }
    public void queryOrderStatus(OrderInfo orderByApiKey) {
        long suffTime = System.currentTimeMillis() - orderByApiKey.getCreateTime();
        //3秒后开始查询
//        if (suffTime >= 3 * 1000) {

        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            try{
                synchronized (orderByApiKey) {
                    ByBitAccount account = orderByApiKey.getByBitAccount();
                    BybitApiClientFactory bybitApiClientFactory = byBitClientCenter.getBybitApiClientFactory(account.getApiSecurity(), account.getApiPwd(), BybitApiConfig.MAINNET_DOMAIN, false);

                    if (orderByApiKey.isOrderChecked() == false) {
                        //先查订单状态
                       BybitApiTradeRestClient tradeClient = bybitApiClientFactory.newTradeRestClient();
                        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
                            .category(CategoryType.LINEAR)
                            .orderLinkId(orderByApiKey.getOrderId()).build();
                        Object openOrders = tradeClient.getOpenOrders(tradeOrderRequest);
                        JSONObject from = JSONObject.from(openOrders);
                        log.info("查询订单状态 {} {}", orderByApiKey.getOrderId(), from);
                        if (from.getInteger("retCode") == 0) {
                            JSONObject result = from.getJSONObject("result");
                            JSONArray list = result.getJSONArray("list");
                            if (list.isEmpty()) {

                            }else{
                                for (Object o : list) {
                                    JSONObject ordered = JSONObject.from(o);
                                    String orderStatus = ordered.getString("orderStatus");
                                    List<String> cancelStatus = OrderStore.getCancelStatus();
                                    if (cancelStatus.contains(orderStatus)) {
                                        //已取消
//                                        log.error("订单已取消，删除持仓状态 {}", ordered);
//                                        OrderStore.remove(orderByApiKey);
//                                        return;
                                    }else if(orderStatus.equalsIgnoreCase("Filled")){
                                        //成交
                                        //继续往下
                                        orderByApiKey.setOrderChecked(true);

                                    }else{
                                        return;
                                    }
                                }
                            }
                        }
                    }


                    BybitApiPositionRestClient client = bybitApiClientFactory.newPositionRestClient();
                    PositionDataRequest positionData = PositionDataRequest.builder()
                        .category(CategoryType.LINEAR)
                        .symbol(orderByApiKey.getSymbol()).build();
                    Object positions = client.getPositionInfo(positionData);
                    JSONObject positionObject = JSONObject.from(positions);
                    log.info("查询持仓状态 {} {}", orderByApiKey.getOrderId(), positionObject);
                    if (positionObject.getInteger("retCode") == 0) {
                        JSONObject result = positionObject.getJSONObject("result");
                        JSONArray list = result.getJSONArray("list");
                        if (list.isEmpty()) {

                        } else {
                            for (Object o : list) {
                                JSONObject ordered = JSONObject.from(o);
                                String symbol = ordered.getString("symbol");
                                if(symbol.equals(orderByApiKey.getSymbol())) {
                                    if (ordered.getBigDecimal("size").doubleValue() == 0d) {
                                        log.error("无持仓，删除持仓状态 {}", ordered);
                                        OrderStore.remove(orderByApiKey);
                                        //没下单成功
                                        ScheduledFuture<?> remove = ScheduleStopLoss.getStopLossScheduled().remove(orderByApiKey.getOrderId());
                                        if (remove != null) {
                                            log.error("停止计算止损线任务");
                                            remove.cancel(true);
                                        }

                                        ScheduledFuture<?> queryOrderTast = queryOrderScheduledMap.remove(orderByApiKey.getOrderId());
                                        if(queryOrderTast != null) {
                                            log.info("取消查询持仓状态任务");
                                            queryOrderTast.cancel(true);
                                        }
                                    }else{
                                        //正常
                                        orderByApiKey.setAvgPrice(ordered.getDouble("avgPrice"));
                                        orderByApiKey.setSide(ordered.getString("side"));
                                        OrderStore.put(orderByApiKey);
                                        ScheduledFuture<?> task = ScheduleStopLoss.getStopLossScheduled().get(orderByApiKey.getOrderId());
                                        if (task == null) {
                                            scheduleStopLoss.calceStopLoss(orderByApiKey);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                log.error("查询订单出错！ {}", orderByApiKey.getOrderId());
            }




        }, 1, 3, TimeUnit.SECONDS);
        queryOrderScheduledMap.put(orderByApiKey.getOrderId(), scheduledFuture);

//        }

    }
}
