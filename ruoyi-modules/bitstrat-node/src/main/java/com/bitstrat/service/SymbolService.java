package com.bitstrat.service;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.server.MessageData;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 17:08
 * @Content
 */
public interface SymbolService {

    /**
     *  同步行情
     * @param exchangeName 监听哪家交易所
     */
    public void traggerWatchSymbol(String exchangeName);
    public void traggerWatchSymbol(String exchangeName, String symbol);

    public void traggerWatchOrder(String exchangeName, MessageData account);

    /**
     * 初始化构建用户操作websocket
     */
    public void initWebsocketClient();
}
