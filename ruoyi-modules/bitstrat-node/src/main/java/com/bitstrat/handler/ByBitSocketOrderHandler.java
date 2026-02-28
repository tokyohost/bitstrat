package com.bitstrat.handler;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.OrderInfo;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.service.ExchangeOptCenter;
import com.bitstrat.service.ExchangeService;
import com.bitstrat.store.*;
import com.bitstrat.utils.BitStratThreadFactory;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.websocket_message.private_channel.OrderData;
import com.bybit.api.client.domain.websocket_message.private_channel.WebSocketOrderMessage;
import com.bybit.api.client.domain.websocket_message.public_channel.WebSocketTickerMessage;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.bitstrat.constant.SideType.BUY;
import static com.bitstrat.constant.SideType.SELL;
import static com.bybit.api.client.config.BybitApiConfig.V5_TRADE;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:36
 * @Content
 */

@Service
@Slf4j
public class ByBitSocketOrderHandler implements WebSocketMessageCallback {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, BitStratThreadFactory.forName("task-scheduler"));
    @Autowired
        @Lazy
    ByBitClientCenter byBitClientCenter;
    @Autowired
    RoleCenter roleCenter;
    @Override
    public void onMessage(String message) throws JsonProcessingException {
        log.warn("订单监听 {}",message);
        JSONObject preCheck = JSONObject.parseObject(message);
        if (preCheck.getString("topic") == null) {
            return;
        }
        var tickerData = (new ObjectMapper()).readValue(message, WebSocketOrderMessage.class);
        log.warn("接收订单数据 {}",message);
//        if ("order".equalsIgnoreCase(tickerData.getTopic())) {
//            //处理订单
//            WebSocketOrderMessage webSocketOrderMessage = (new ObjectMapper()).readValue(message, WebSocketOrderMessage.class);
////            webSocketOrderMessage.get
//            List<OrderData> data = webSocketOrderMessage.getData();
//            for (OrderData orderData : data) {
//                synchronized (OrderStore.class) {
//                    if ("stopOrderType".equalsIgnoreCase(orderData.getStopOrderType())) {
//                        //是止损单不处理
//                        continue;
//                    }
//                    String orderLinkId = orderData.getOrderLinkId();
//                    OrderInfo orderByLinkId = OrderStore.getOrderByLinkId(orderLinkId);
//                    if(orderByLinkId == null) {
//                        log.error("没有找到订单！orderLinkId:{}", orderLinkId);
//                    }else{
//                        if(orderByLinkId.getCallbackTime() == null) {
//                            orderByLinkId.setCallbackTime(System.currentTimeMillis());
//                            log.info("下单到成交耗时 {}ms",orderByLinkId.getCallbackTime() - orderByLinkId.getCreateTime());
//                        }
//                        List<String> cancelStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Cancelled", "Deactivated");
//                        if (cancelStatus.contains(orderData.getOrderStatus())) {
//
//                        }else{
//                            orderByLinkId.setAvgPrice(Double.parseDouble(orderData.getAvgPrice()));
//                            orderByLinkId.setSide(orderData.getSide());
//                            orderByLinkId.setLastUpdateTime(System.currentTimeMillis());
////                            calceStopLoss(orderByLinkId);
//                        }
//
//                    }
//                }
//            }
//
//        }
    }

    /**
     * 计算并更新止损线
     * @param orderByLinkId
     */
    private void calceStopLoss(OrderInfo order) {
        ScheduledFuture<?> schedule = scheduler.scheduleWithFixedDelay(() -> {
            try{
                log.info("开始同步更新订单 {} 止损线",order.getOrderId());
                OrderInfo orderByLinkId = OrderStore.getOrderByLinkId(order.getOrderId());
                Double currentPrice = RealTimePriceStore.getRealTimePrice().get(order.getSymbol());
                if(currentPrice == null) {
                    log.error("暂无最新价格");
                    return;
                }else{
                    String side = orderByLinkId.getSide();
                    double avgPrice = orderByLinkId.getAvgPrice();
                    if (avgPrice == 0d) {
                        log.error("订单未响应");
                        return;
                    }
                    double retread = orderByLinkId.getRetread();
                    double oldStopLoss = orderByLinkId.getStopLoss();
                    if(BUY.equalsIgnoreCase(side)) {
                        //判断是否盈利，计算止损线，防止亏损时止损线一直下移
                        //没有盈利，不动原来的止损线
                        // 当前浮盈
                        if (currentPrice > avgPrice) {
                            double profit = currentPrice - avgPrice;
                            double newStopLoss = currentPrice - profit * retread;

                            // 只有止损线上移才更新，防止回撤拖亏
                            if (newStopLoss > oldStopLoss) {
                                updateStopLoss(orderByLinkId,newStopLoss);
                                log.info("BUY单浮盈，更新止损线为 {}", newStopLoss);
                            }
                        } else {
                            log.info("BUY单当前未盈利，止损线保持不变");
                        }
                    } else if (SELL.equalsIgnoreCase(side)) {
                        //判断是否盈利，计算止损线，防止亏损时止损线一直下移
                        //没有盈利，不动原来的止损线
                        if (currentPrice < avgPrice) {
                            double profit = avgPrice - currentPrice;
                            double newStopLoss = currentPrice + profit * retread;

                            if (newStopLoss < oldStopLoss) {
                                updateStopLoss(orderByLinkId,newStopLoss);
                                log.info("SELL单浮盈，更新止损线为 {}", newStopLoss);
                            }
                        } else {
                            log.info("SELL单当前未盈利，止损线保持不变");
                        }
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        },0, order.getCoolingTime(), TimeUnit.SECONDS);
        ScheduleStopLoss.getStopLossScheduled().put(order.getOrderId(), schedule);

    }

    private void updateStopLoss(OrderInfo orderByLinkId, double newStopLoss) {
        Map<String, Object> orderPrarms = new HashMap<>();
        ByBitAccount byBitAccount = orderByLinkId.getByBitAccount();
        WebsocketStreamClient bybitApiSocketStream = byBitClientCenter.getBybitApiSocketStream(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.STREAM_MAINNET_DOMAIN, false);
        bybitApiSocketStream.setClientName("用户 "+byBitAccount.getApiSecurity()+" TRADE Socket");
        orderPrarms.put("category", CategoryType.LINEAR);
        orderPrarms.put("orderLinkId", orderByLinkId.getOrderId());
        orderPrarms.put("stopLoss", newStopLoss+"");
        orderPrarms.put("op", "order.amend");
        bybitApiSocketStream.getTradeChannelStream(orderPrarms,V5_TRADE);

    }

    /**
     * 计算滑点
     *
     * @param symbol
     * @param lastPrice
     */
    private void calcPrice(String symbol, String lastPrice) {
        double lprice = Double.parseDouble(lastPrice);
        ConcurrentHashMap<String, OrderInfo> orderHolder = OrderStore.getOrderHolder();
        if(orderHolder.containsKey(symbol)) {
            //存在指定币对的订单，先计算回撤止损线
            OrderInfo orderInfo = orderHolder.get(symbol);

        }
        ActiveLossPoint traggerPrice = roleCenter.getTraggerPrice(ExchangeType.BYBIT.getName(), lprice, symbol);
        double pricePrice = traggerPrice.getPrice().doubleValue();
        log.info("获取最近的预设滑点 {}", traggerPrice.getPrice());
        double range = traggerPrice.getTriggerPrice1().doubleValue();

        //如果 imbalance ≫ 0：买单比卖单多 → 很可能有人要大买
        //
        // 如果 imbalance ≪ 0：卖压大 → 可能有人要大卖
        Double imbalance = OrderBooksStore.direction(symbol);

        //满足预设滑点和+ -range 范围时根据lprice 和 imbalance 进行挂单
        // 判断是否挂单
        if (imbalance > 0) {
            // 买单较多，可能大买
            // 判断滑点和范围是否满足挂单条件
            if (lprice >= pricePrice - range && lprice <= pricePrice + range) {
                // 在范围内进行挂多单
//                placeBuyOrder(lprice, symbol);
            }
        } else if (imbalance < 0) {
            // 卖单较多，可能大卖
            // 判断滑点和范围是否满足挂单条件
            if (lprice >= pricePrice - range && lprice <= pricePrice + range) {
                // 在范围内进行挂空单
//                placeSellOrder(lprice, symbol);
            }
        } else {
            log.info("市场无明显方向，暂不进行挂单操作");
        }

    }
}
