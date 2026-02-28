package com.bitstrat.orderPressStragy.impl;

import com.bitstrat.constant.OrderPressStragyType;
import com.bitstrat.constant.SideType;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.locks.SymbolLock;
import com.bitstrat.orderPressStragy.OrderCallback;
import com.bitstrat.orderPressStragy.OrderPressStragy;
import com.bitstrat.orderPressStragy.vo.HistoricalPriceSnapshot;
import com.bitstrat.store.OrderBooksStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


/**
 * 根据订单表做下单判断
 */
@Component
@Slf4j
public class HistoryPriceStragy implements OrderPressStragy {
    ConcurrentHashMap<String, HistoricalPriceSnapshot> historyPriceMap = new ConcurrentHashMap<>();


    @Override
    public void pressOrder(String symbol, Double lastPrice, ActiveLossPoint traggerPrice, OrderCallback orderCallback) {
        HistoricalPriceSnapshot historicalPriceSnapshot = getHistoryPrice(symbol);
        Double last30sPrice = historicalPriceSnapshot.getPrice30s();
        Double last60sPrice = historicalPriceSnapshot.getPrice60s();

        historicalPriceSnapshot.update(lastPrice, System.currentTimeMillis());
        double taggerPrice = traggerPrice.getPrice().doubleValue();
        double lprice = lastPrice;
//        log.info("获取最近的预设滑点 {}", traggerPrice.getPrice());
        double range = traggerPrice.getTriggerPrice1().doubleValue();
        //如果当前价格在指定滑点价格 +- range 内，触发下单
        double minPrice = taggerPrice - range;
        double maxPrice = taggerPrice + range;

        if (!historicalPriceSnapshot.isReady()) {
            log.info("历史价格未知，暂不下单");
            orderCallback.orderCallback(SideType.NONE);
            return;
        }
        log.info("30s 之前 {} 60s 之前 {} 当前 {} ,触发 {}-{}", last30sPrice, last60sPrice, lastPrice, minPrice, maxPrice);
        //8000 - 1 = 7999 minprice
        //8000 + 1 = 8001 maxprice
        //7999.5 >= 7999 && 7999.5 <= 8001
        if (lastPrice >= minPrice && lastPrice <= maxPrice) {

            if (last30sPrice <= lastPrice && last60sPrice <= lastPrice) {
                //之前价格都低于，现在做多
                Object lock = SymbolLock.getlock(symbol);
                synchronized (lock) {
                    orderCallback.orderCallback(SideType.BUY);
                }

            } else if (last30sPrice >= lastPrice && last60sPrice >= lastPrice) {
                Object lock = SymbolLock.getlock(symbol);
                //之前价格都高于，现在做空
                synchronized (lock) {
                    orderCallback.orderCallback(SideType.SELL);
                }

            } else {
                log.info("市场无明显方向，暂不进行挂单操作");
                orderCallback.orderCallback(SideType.NONE);
            }
        }

        orderCallback.orderCallback(SideType.NONE);
    }

    private HistoricalPriceSnapshot getHistoryPrice(String symbol) {
        if (historyPriceMap.containsKey(symbol)) {
            return historyPriceMap.get(symbol);
        }else {
            synchronized (historyPriceMap) {
                if (historyPriceMap.containsKey(symbol)) {
                    return historyPriceMap.get(symbol);
                }else{
                    HistoricalPriceSnapshot historicalPriceSnapshot = new HistoricalPriceSnapshot();
                    historyPriceMap.put(symbol, historicalPriceSnapshot);
                    return historicalPriceSnapshot;
                }
            }
        }

    }

    @Override
    public String stragyName() {
        return OrderPressStragyType.HISTORY_PRICE;
    }
}
