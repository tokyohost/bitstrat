package com.bitstrat.constant;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 14:50
 * @Content
 */

@Data
public class MessageType {
    public static final int AUTH = 0;
    public static final int PING = 1;

    public static final int DISCONNECT = 2;

    //市场最新价格
    public static final int MARKET_LAST_PRICE = 101;
    //创建订单
    public static final int CREATE_ORDER = 201;
    //更新滑点触发数据
    public static final int UPDATE_LOSS_POINT = 301;
    //订阅市场数据
    public static final int SUBSCRIPTION_SYMBOL = 102;
    //订阅订单数据
    public static final int SUBSCRIPTION_ORDER = 402;
    //初始化交易所
    public static final int INIT_EXCHANGE = 502;

}
