package com.bitstrat.ai.handler.marketPriceCover;

import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/29 18:41
 * @Content
 */
public interface MarketPriceCover {

    public String exchangeName();

    public List<MarketPrice> coverLinerMsg(LinerReceiveMsg linerMsg);
}
