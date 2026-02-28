package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/21 18:20
 * @Content
 */

public class ContractOrderStatus {

    //最终态
    public static final String FILLED = "Filled"; //完全成交
    public static final String CANCELLED = "Cancelled"; //已取消
    public static final String REJECTED = "Rejected"; //已拒绝
    public static final String TRIGGERED = "Triggered"; //已触发
    public static final String DEACTIVATED = "Deactivated"; //触发前已取消
    public static final String PARTIALLY_FILLED_CANCELED = "PartiallyFilledCanceled"; //订单部分成交且已取消

    //active
    public static final String PARTIALLY_FILLED = "PartiallyFilled"; //部分成交
    public static final String NEW = "New"; //刚创建
    public static final String LIVE = "live"; //等待成交
    public static final String NOT_ENDING_UNKNOW = "unknow"; //未知状态，但
}
