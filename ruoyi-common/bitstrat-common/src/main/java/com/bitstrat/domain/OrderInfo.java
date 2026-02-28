package com.bitstrat.domain;

import com.bitstrat.domain.bybit.ByBitAccount;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 19:43
 * @Content
 */

@Data
public class OrderInfo {

    String exchange;
    ByBitAccount byBitAccount;
    String orderId;

    String symbol;

    /**
     * Buy,Sell
     */
    String side;

    String apiKey;

    //持仓价
    Double avgPrice;
    //止损价
    double stopLoss;

    //上次更新时间
    Long lastUpdateTime;

    //创建时间
    Long createTime;

    //响应时间
    Long callbackTime;

    //冷却时间
    Long coolingTime;

    //回撤率
    double retread;

    //订单是否已确认
    boolean orderChecked = false;

    //止损计算起点
    double stopLossCalcLimit;
}
