package com.bitstrat.store;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.config.SymbolBufferConfig;
import com.bitstrat.config.SymbolConfigNode;
import com.bitstrat.domain.OrderInfo;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.utils.BitStratThreadFactory;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.PositionIdx;
import com.bybit.api.client.restApi.BybitApiPositionRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.*;

import static com.bitstrat.constant.SideType.BUY;
import static com.bitstrat.constant.SideType.SELL;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 22:30
 * @Content
 */

@Component
@Slf4j
public class ScheduleStopLoss {
    private final static ConcurrentHashMap<String, ScheduledFuture<?>> stopLossScheduled = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, BitStratThreadFactory.forName("task-scheduler"));
    @Autowired
    @Lazy
    ByBitClientCenter byBitClientCenter;
    @Autowired
    SymbolBufferConfig symbolBufferConfig;
    public static ConcurrentHashMap<String, ScheduledFuture<?>> getStopLossScheduled() {
        return stopLossScheduled;
    }

    /**
     * 计算并更新止损线
     */
    public synchronized void calceStopLoss(OrderInfo order) {
        if( ScheduleStopLoss.getStopLossScheduled().get(order.getOrderId()) != null) {
            log.info("已有止损线任务");
            return;
        }
        log.info("开始计算止损线");
        ScheduledFuture<?> schedule = scheduler.scheduleWithFixedDelay(() -> {
            try{
                log.info("开始同步更新订单 {} 止损线",order.getOrderId());
                OrderInfo orderByLinkId = OrderStore.getOrderByLinkId(order.getOrderId());
//                if(orderByLinkId == null) {
//                    //订单没有了，结束任务
//                    ScheduledFuture<?> remove = ScheduleStopLoss.getStopLossScheduled().remove(order.getOrderId());
//                    if(remove != null) {
//                        remove.cancel(true);
//                    }
//                }
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
                    Double drawdownLimit = orderByLinkId.getStopLossCalcLimit();
                    if(BUY.equalsIgnoreCase(side)) {
                        //判断是否盈利，计算止损线，防止亏损时止损线一直下移
                        //没有盈利，不动原来的止损线
                        // 当前浮盈
                        if (currentPrice > (avgPrice+drawdownLimit)) {
                            //(持仓价 + (当前价格 - 持仓价) * (1-回撤率))
                            double profit = currentPrice - avgPrice;
//                            double newStopLoss = currentPrice - profit * retread;
                            double newStopLoss = avgPrice + profit * (1 - retread);

                            // 只有止损线上移才更新，防止回撤拖亏
                            if (newStopLoss > oldStopLoss ) {
                                orderByLinkId.setStopLoss(newStopLoss);
                                updateStopLoss(orderByLinkId,newStopLoss);
                                log.info("BUY单浮盈，更新止损线为 {}", newStopLoss);
                            }
                        } else {
                            log.info("BUY单当前未盈利，止损线保持不变");
                        }
                    } else if (SELL.equalsIgnoreCase(side)) {
                        //判断是否盈利，计算止损线，防止亏损时止损线一直下移
                        //没有盈利，不动原来的止损线
                        if (currentPrice < (avgPrice-drawdownLimit)) {
                            double profit = avgPrice - currentPrice;
//                            double newStopLoss = currentPrice + profit * retread;
                            double newStopLoss = avgPrice - profit * (1 - retread);

                            if (newStopLoss < oldStopLoss) {
                                orderByLinkId.setStopLoss(newStopLoss);
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
        ByBitAccount byBitAccount = orderByLinkId.getByBitAccount();
        BybitApiClientFactory bybitApiClientFactory = byBitClientCenter.getBybitApiClientFactory(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd(), BybitApiConfig.MAINNET_DOMAIN, false);
        BybitApiPositionRestClient bybitApiPositionRestClient = bybitApiClientFactory.newPositionRestClient();

        PositionDataRequest request = PositionDataRequest.builder()
            .category(CategoryType.LINEAR)
            .stopLoss(BigDecimal.valueOf(newStopLoss).toPlainString())
            .positionIdx(PositionIdx.ONE_WAY_MODE)
            .symbol(orderByLinkId.getSymbol()).build();

        Object o = bybitApiPositionRestClient.setTradingStop(request);
        log.info("设置止损 结果 {}", JSONObject.from(o).toJSONString());
        JSONObject result = JSONObject.from(o);
        if (result.getInteger("retCode") != 0) {
            throw new RuntimeException("更新止损失败 : "+result.getInteger("retMsg"));
        }else {
            log.info("设置止损成功");
        }
//        orderPrarms.put("category", CategoryType.LINEAR);
//        orderPrarms.put("orderLinkId", orderByLinkId.getOrderId());
//        orderPrarms.put("stopLoss", newStopLoss+"");
//        orderPrarms.put("op", "order.amend");
//        bybitApiSocketStream.getTradeChannelStream(orderPrarms,V5_TRADE);


    }

}
