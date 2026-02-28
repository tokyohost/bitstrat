package com.bitstrat.domain;

import com.bitstrat.constant.OrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderVo {
    Account account;
    String orderId;
    /**
     * 币对
     */
    String symbol;
    /**
     * 下单单价
     */
    BigDecimal price;
    /**
     * 下单数量
     */
    BigDecimal size;
    /**
     * bitget / binance 修改价格需要上送数量和价格
     */
    BigDecimal orderSize;
    /**
     * 订单类型 see {@link OrderType}
     */
    String orderType;
    /**
     * 是否只减仓
     */
    Boolean reduceOnly;
    /**
     * 杠杆
     */
    BigDecimal leverage;

    /**
     * 止盈价格
     */
    BigDecimal takeProfitPrice;
    /**
     * 止损价格
     */
    BigDecimal stopLossPrice;

    /**
     * 订单方向 long 做多 short 做空
     */
    String side;
}
