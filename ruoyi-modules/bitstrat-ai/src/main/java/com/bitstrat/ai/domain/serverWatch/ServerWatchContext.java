package com.bitstrat.ai.domain.serverWatch;

import com.bitstrat.ai.distuptor.MarketABPriceEventHandler;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.domain.Account;
import lombok.Data;
import lombok.ToString;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 18:00
 * @Content 服务器监听市价上下文
 */

@Data
@ToString
public class ServerWatchContext {

    private Long userId;


    private ABOrderTask abOrderTask;

    private MarketABPriceEventHandler marketABPriceEventHandler;



}
