package com.bitstrat.wsClients.msg;

import com.bitstrat.constant.ExchangeType;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:44
 * @Content
 */

public interface SubscriptMsgs {

    /**
     *
     * @return {@link ExchangeType}
     */
    public String exchangeName();

    /**
     * 创建仓位监听消息
     */
    public String createSubscriptPositionMsg();
    /**
     * 创建订单监听消息
     */
    public String createSubscriptOrderMsg();

    /**
     * 创建监听账户变动消息
     * @return
     */
    public String createSubscriptAccountMsg();

    /**
     * 监听合约市场数据最新成交价
     * @return
     */
    public String createSwapMarketMsg(String symbol);
    /**
     * 监听合约市场最新成交价
     * @return
     */
    public String createSwapMarketMsg(String symbol,String url);
}
