package com.bitstrat.orderPressStragy.impl;

import com.bitstrat.constant.OrderPressStragyType;
import com.bitstrat.constant.OrderType;
import com.bitstrat.constant.SideType;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.locks.SymbolLock;
import com.bitstrat.orderPressStragy.OrderCallback;
import com.bitstrat.orderPressStragy.OrderPressStragy;
import com.bitstrat.store.OrderBooksStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bitstrat.constant.OrderPressStragyType.ORDER_BOOK;


/**
 * 根据订单表做下单判断
 */
@Component
@Slf4j
public class OrderBookStragy  implements OrderPressStragy {

    @Override
    public void pressOrder(String symbol,Double lastPrice,ActiveLossPoint traggerPrice, OrderCallback orderCallback) {
        double pricePrice = traggerPrice.getPrice().doubleValue();
        double lprice = lastPrice;
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
            if(lprice>= pricePrice){
                if (lprice >= pricePrice - range && lprice <= pricePrice + range) {
                    // 在范围内进行挂多单
                    Object lock = SymbolLock.getlock(symbol);
                    synchronized (lock) {
                        orderCallback.orderCallback(SideType.BUY);
//                        placeSellOrder(lprice, symbol,traggerPrice);
//                        placeBuyOrder(lprice, symbol,traggerPrice);
                    }
                }
            }

        } else if (imbalance < 0) {
            // 卖单较多，可能大卖
            // 判断滑点和范围是否满足挂单条件
            if(lprice<= pricePrice){
                if (lprice >= pricePrice - range && lprice <= pricePrice + range) {
                    // 在范围内进行挂空单
                    Object lock = SymbolLock.getlock(symbol);
                    synchronized (lock) {
//                        placeSellOrder(lprice, symbol,traggerPrice);
//                        placeBuyOrder(lprice, symbol,traggerPrice);
                        orderCallback.orderCallback(SideType.SELL);
                    }
                }
            }

        } else {
            log.info("市场无明显方向，暂不进行挂单操作");
            orderCallback.orderCallback(SideType.NONE);
        }
    }

    @Override
    public String stragyName() {
        return OrderPressStragyType.ORDER_BOOK;
    }
}
