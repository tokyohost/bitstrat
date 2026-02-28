package com.bitstrat.constant;

public class CrossOrderStatus {

    //下单成功
    public static final String SUCCESS = "SUCCESS";
    //下单失败
    public static final String FAIL = "FAIL";
    //未知，等待后继续查
    public static final String UNKNOW = "UNKNOW";
    //已取消
    public static final String CANCEL = "CANCEL";
    //完全成交
    public static final String FILLED = "FILLED";
    //已平仓
    public static final String CLOSE_POSITION = "CLOSE_POSITION";

    //中间态
    public static final String PROCESS = "PROCESS";
    //结束态
    public static final String END = "END";
}
