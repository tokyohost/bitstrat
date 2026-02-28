package com.bitstrat.domain;

import com.bitstrat.domain.binance.BinanceOrderDetail;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderOptStatus {
    String symbol;
    String orderId;
    String status;
    //持仓反向  long 做多/ short 做空
    String side;

    //订单成交手续费
    BigDecimal fee;

    BigDecimal avgPrice;

    /**
     * 是否是同步订单，也就是说调用创建订单之后，马上就能收到订单信息 binance 是同步
     */
    Boolean syncOrder = false;

    SyncOrderDetail syncOrderDetail;

    Account account;

    Long userId;

}
