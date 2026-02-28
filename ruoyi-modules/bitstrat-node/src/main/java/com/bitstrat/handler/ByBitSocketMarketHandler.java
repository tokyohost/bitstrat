package com.bitstrat.handler;

import cn.hutool.core.util.IdUtil;
import com.bitstrat.cache.MarketPriceCache;
import com.bitstrat.config.Monitor;
import com.bitstrat.config.SymbolBufferConfig;
import com.bitstrat.config.SymbolConfigNode;
import com.bitstrat.constant.*;
import com.bitstrat.domain.OrderInfo;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.BybitOrder;
import com.bitstrat.domain.server.Message;
import com.bitstrat.orderPressStragy.OrderPressCenter;
import com.bitstrat.orderPressStragy.OrderPressStragy;
import com.bitstrat.service.BybitService;
import com.bitstrat.service.ExchangeOptCenter;
import com.bitstrat.service.ExchangeService;
import com.bitstrat.store.OrderStore;
import com.bitstrat.store.RealTimePriceStore;
import com.bitstrat.store.RoleCenter;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.websocket_message.public_channel.WebSocketTickerMessage;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:36
 * @Content
 */

@Component
@Slf4j
public class ByBitSocketMarketHandler implements WebSocketMessageCallback {

    @Autowired
    RoleCenter roleCenter;


    @Autowired
    BybitService bybitService;
    @Value("${loss-point.stop-loss-cooling-time}")
    Long stopLossCooldingTime;
    @Value("${loss-point.order-cooling-time}")
    Long orderCoolingTime;


    @Autowired
    SymbolBufferConfig symbolBufferConfig;
    @Value("${loss-point.order-press-stragy:orderBook}")
    String orderPressStragy;

    Long lastOrderTime;
    @Autowired
    ExchangeOptCenter exchangeOptCenter;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    OrderPressCenter orderPressCenter;

    @Autowired
    MarketPriceCache marketPriceCache;

    @Override
    public void onMessage(String message) throws JsonProcessingException {

        var tickerData = (new ObjectMapper()).readValue(message, WebSocketTickerMessage.class);
        if (tickerData.getTopic() == null) {
            return;
        }
        if (tickerData.getTopic().startsWith("tickers.")) {
            long l = System.currentTimeMillis();
//            log.info("lastPrice: {}", JSONObject.toJSONString(tickerData));
            String lastPrice = tickerData.getData().getLastPrice();
            String symbol = tickerData.getData().getSymbol();
            if (StringUtils.isNotEmpty(lastPrice)) {
//                    callbackToServer(symbol, lastPrice);
                long delay = l - tickerData.getTs();
                log.info("symbol {} lastPrice: {} 延迟 {}ms", symbol, lastPrice, delay);
                Monitor.delay.set(delay);
                RealTimePriceStore.getRealTimePrice().put(symbol, Double.valueOf(lastPrice));
                marketPriceCache.updatePrice(ExchangeType.BYBIT.getName(), symbol, lastPrice);
                calcPrice(symbol, lastPrice);


            }
        }
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

        ActiveLossPoint traggerPrice = roleCenter.getTraggerPrice(ExchangeType.BYBIT.getName(), lprice, symbol);
        if (traggerPrice == null) {
            return;
        }
        double pricePrice = traggerPrice.getPrice().doubleValue();
        log.info("获取最近的预设滑点 {}", traggerPrice.getPrice());
        double range = traggerPrice.getTriggerPrice1().doubleValue();


        OrderPressStragy pressStragy = orderPressCenter.getOrderPressStragy(orderPressStragy);
        pressStragy.pressOrder(symbol, Double.valueOf(lastPrice), traggerPrice, (orderSide) -> {
            if (SideType.BUY.equalsIgnoreCase(orderSide)) {
                //买入
                placeBuyOrder(lprice, symbol, traggerPrice);
            } else if (SideType.SELL.equalsIgnoreCase(orderSide)) {
                //卖出
                placeSellOrder(lprice, symbol, traggerPrice);
            } else {
                log.warn("不满足买入卖出条件");
            }
        });

    }


    private synchronized void placeSellOrder(double lprice, String symbol, ActiveLossPoint traggerPrice) {
        log.error("可以下空单 {} {}", lprice, symbol);
        boolean b = checkCoolingTimeCanOrder(symbol);
        if (!b) {
            log.error("没过冷却时间，不允许下单");
            return;
        }

        boolean existsOrder = checkExistsOrder(traggerPrice);
        if (existsOrder) {
            log.error("已存在持仓，不继续下单");
            return;
        }

        RLock lock = redissonClient.getLock(traggerPrice.getAccount().getApiSecurity() + ":" + symbol);
        if (lock.tryLock()) {
            try {     // manipulate protected state
                // 获取锁成功后，检查 是否已经有相关币种的订单在别的节点执行了
                RBucket<Object> bucket = redissonClient.getBucket(traggerPrice.getExchangeName() + ":" + traggerPrice.getAccount().getApiSecurity() + ":" + symbol);
                boolean exists = bucket.isExists();
                if (exists) {
                    log.error("别的节点已存在持仓订单，不处理");
                    return;
                } else {
                    log.info("无持仓订单，继续下单");
                }

                Double orderPriceRedundancy = 0d;
                SymbolConfigNode symbolconfig = symbolBufferConfig.getSymbol(symbol);
                if (symbolconfig != null) {
                    orderPriceRedundancy = symbolconfig.getPrice().doubleValue();
                }

                log.info("orderPriceRedundancy= {} {}", symbol, orderPriceRedundancy);
                lprice = lprice - orderPriceRedundancy;
                String snowflakeNextIdStr = IdUtil.getSnowflakeNextIdStr();
                ExchangeService exchangeService = exchangeOptCenter.getExchangeService(ExchangeType.BYBIT.getName());
                Message message = new Message();
                message.setType(MessageType.CREATE_ORDER);
                BybitOrder bybitOrder = new BybitOrder();
                bybitOrder.setCategory(CategoryType.LINEAR.getCategoryTypeId());
                bybitOrder.setSymbol(symbol);
                bybitOrder.setOrderLinkId(snowflakeNextIdStr);
                bybitOrder.setPrice(BigDecimal.valueOf(lprice));
                //做空的止损是上涨
                double retread = lprice * traggerPrice.getTriggerPrice2().doubleValue();
                double stopLoss = lprice + retread;
                bybitOrder.setStopLoss(BigDecimal.valueOf(stopLoss).toPlainString());
                bybitOrder.setQuantity(traggerPrice.getQuantity());
                bybitOrder.setType(OrderType.MARKET);
                bybitOrder.setByBitAccount(traggerPrice.getAccount());
                bybitOrder.setTimeInForce(TimeInForceType.IOC);
                message.setData(bybitOrder);

                long createTime = System.currentTimeMillis();
                exchangeService.sell(message);

                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOrderId(snowflakeNextIdStr);
                orderInfo.setStopLossCalcLimit(traggerPrice.getStopLossCalcLimit().doubleValue());
                orderInfo.setRetread(traggerPrice.getRetread().doubleValue());
                orderInfo.setStopLoss(stopLoss);
                orderInfo.setByBitAccount(traggerPrice.getAccount());
                orderInfo.setApiKey(traggerPrice.getAccount().getApiSecurity());
                orderInfo.setLastUpdateTime(System.currentTimeMillis());
                orderInfo.setSymbol(symbol);
                orderInfo.setCreateTime(createTime);
                orderInfo.setCoolingTime(stopLossCooldingTime);

                OrderStore.put(orderInfo);
                //查询订单
                bybitService.queryOrderStatus(orderInfo);
            } finally {
                lock.unlock();
            }
        } else {   // perform alternative actions
            log.error("下单失败，获取锁失败！");
        }

    }

    private synchronized void placeBuyOrder(double lprice, String symbol, ActiveLossPoint traggerPrice) {
        log.error("可以下多单 {} {}", lprice, symbol);
        boolean b = checkCoolingTimeCanOrder(symbol);
        if (!b) {
            log.error("没过冷却时间，不允许下单");
            return;
        }
        boolean existsOrder = checkExistsOrder(traggerPrice);
        if (existsOrder) {
            log.error("已存在持仓，不继续下单");
            return;
        }
        RLock lock = redissonClient.getLock(traggerPrice.getAccount().getApiSecurity() + ":" + symbol);
        if (lock.tryLock()) {
            try {     // manipulate protected state
                // 获取锁成功后，检查 是否已经有相关币种的订单在别的节点执行了
                RBucket<Object> bucket = redissonClient.getBucket(traggerPrice.getExchangeName() + ":" + traggerPrice.getAccount().getApiSecurity() + ":" + symbol);
                boolean exists = bucket.isExists();
                if (exists) {
                    log.error("别的节点已存在持仓订单，不处理");
                    return;
                } else {
                    log.info("无持仓订单，继续下单");
                }
                Double orderPriceRedundancy = 0d;
                SymbolConfigNode symbolconfig = symbolBufferConfig.getSymbol(symbol);
                if (symbolconfig != null) {
                    orderPriceRedundancy = symbolconfig.getPrice().doubleValue();
                }
                log.info("orderPriceRedundancy= {} {}", symbol, orderPriceRedundancy);
                lprice = lprice + orderPriceRedundancy;
                String snowflakeNextIdStr = IdUtil.getSnowflakeNextIdStr();
                ExchangeService exchangeService = exchangeOptCenter.getExchangeService(ExchangeType.BYBIT.getName());
                Message message = new Message();
                message.setType(MessageType.CREATE_ORDER);
                BybitOrder bybitOrder = new BybitOrder();
                bybitOrder.setCategory(CategoryType.LINEAR.getCategoryTypeId());
                bybitOrder.setSymbol(symbol);
                bybitOrder.setOrderLinkId(snowflakeNextIdStr);
                bybitOrder.setPrice(BigDecimal.valueOf(lprice));
                //做多的止损是下跌
                double retread = lprice * traggerPrice.getTriggerPrice2().doubleValue();
                double stopLoss = lprice - retread;
                bybitOrder.setStopLoss(BigDecimal.valueOf(stopLoss).toPlainString());
                bybitOrder.setQuantity(traggerPrice.getQuantity());
                bybitOrder.setType(OrderType.MARKET);
                bybitOrder.setByBitAccount(traggerPrice.getAccount());
                bybitOrder.setTimeInForce(TimeInForceType.IOC);
                message.setData(bybitOrder);
                long createTime = System.currentTimeMillis();
                exchangeService.buy(message);
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOrderId(snowflakeNextIdStr);
                orderInfo.setStopLossCalcLimit(traggerPrice.getStopLossCalcLimit().doubleValue());
                orderInfo.setRetread(traggerPrice.getRetread().doubleValue());
                orderInfo.setStopLoss(stopLoss);
                orderInfo.setByBitAccount(traggerPrice.getAccount());
                orderInfo.setApiKey(traggerPrice.getAccount().getApiSecurity());
                orderInfo.setLastUpdateTime(System.currentTimeMillis());
                orderInfo.setCreateTime(createTime);
                orderInfo.setSymbol(symbol);
                orderInfo.setCoolingTime(stopLossCooldingTime);

                OrderStore.put(orderInfo);
                //查询订单
                bybitService.queryOrderStatus(orderInfo);
            } finally {
                lock.unlock();
            }
        } else {
            log.error("下单失败，获取锁失败！");
        }

    }

    //判断冷却时间
    private boolean checkCoolingTimeCanOrder(String symbol) {
        if (lastOrderTime == null) {
            return true;
        }

        if (System.currentTimeMillis() - lastOrderTime > stopLossCooldingTime) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkExistsOrder(ActiveLossPoint traggerPrice) {


        OrderInfo orderByApiKey = OrderStore.getOrderByApiKey(traggerPrice.getAccount().getApiSecurity());
        if (orderByApiKey == null) {
            return false;
        } else {

            return true;
        }

    }
}
