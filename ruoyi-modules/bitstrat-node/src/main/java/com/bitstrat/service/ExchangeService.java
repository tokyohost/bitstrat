package com.bitstrat.service;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.server.Message;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:09
 * @Content
 */
public interface ExchangeService {

    /**
     * 买入挂单
     * @param message
     */
    public void buy(Message message);

    /**
     * 修改订单
     * @param message
     */
    public void amend(Message message);

    /**
     * 卖出挂单
     * @param message
     */
    public void sell(Message message);

    /**
     * 取消挂单
     * @param message
     */
    public void cancel(Message message);

    public String getExchangeName();

    /**
     * 初始化创建好长连接
     */
    public void initSocket(Message account);
}
