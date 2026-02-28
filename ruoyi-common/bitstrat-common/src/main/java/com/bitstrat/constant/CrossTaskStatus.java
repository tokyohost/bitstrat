package com.bitstrat.constant;


public class CrossTaskStatus {

//    1-正在运行 2-已停止 3-未启动
    //未启动
    public static final long CREATED = 3;
    //等待建仓
    public static final long WAIT_ORDER_DEAL = 2;
    //正在运行
    public static final long RUNNING = 1;
    //已停止
    public static final long STOPED = 20;
    //已平仓
    public static final long CLOSED = 30;
}
