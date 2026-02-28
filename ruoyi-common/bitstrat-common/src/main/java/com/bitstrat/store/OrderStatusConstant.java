package com.bitstrat.store;

import java.util.Arrays;
import java.util.List;

public class OrderStatusConstant {
    public static List<String> bybitCancelStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Cancelled", "Deactivated");
    //中间态
    public static List<String> bybitProcessStatus = Arrays.asList("New", "PartiallyFilled", "Untriggered");
    //最终态
    public static List<String> bybitEndStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Cancelled", "Deactivated","Filled","Triggered");
    //中间态
    public static List<String> okxProcessStatus = Arrays.asList("live", "partially_filled");
    //最终态
    public static List<String> okxEndStatus = Arrays.asList("canceled", "filled","mmp_canceled");
    //中间态
    public static List<String> bitgetProcessStatus = Arrays.asList("live", "partially_filled");
    //最终态
    public static List<String> bitgetEndStatus = Arrays.asList("canceled", "filled");

    /**
     * NEW 新建订单
     * PARTIALLY_FILLED 部分成交
     * FILLED 全部成交
     * CANCELED 已撤销
     * REJECTED 订单被拒绝
     * EXPIRED 订单过期(根据timeInForce参数规则)
     */
    //中间态
    public static List<String> binanceProcessStatus = Arrays.asList("NEW", "PARTIALLY_FILLED");
    //最终态
    public static List<String> binanceEndStatus = Arrays.asList("CANCELED", "FILLED","REJECTED","EXPIRED");
}
