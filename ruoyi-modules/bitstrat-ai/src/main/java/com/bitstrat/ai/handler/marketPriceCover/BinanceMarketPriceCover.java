package com.bitstrat.ai.handler.marketPriceCover;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/29 19:44
 * @Content
 */

@Component
public class BinanceMarketPriceCover implements MarketPriceCover {
    @Override
    public String exchangeName() {
        return ExchangeType.BINANCE.getName();
    }

    /**
     *   {
     *     "e": "markPriceUpdate",  	// 事件类型
     *     "E": 1562305380000,      	// 事件时间
     *     "s": "BTCUSDT",          	// 交易对
     *     "p": "11794.15000000",   	// 标记价格
     *     "i": "11784.62659091",		// 现货指数价格
     *     "P": "11784.25641265",		// 预估结算价,仅在结算前最后一小时有参考价值
     *     "r": "0.00038167",       	// 资金费率
     *     "T": 1562306400000       	// 下次资金时间
     *   }
     *
     *    {
     *     "e": "24hrMiniTicker",  // 事件类型
     *     "E": 123456789,         // 事件时间(毫秒)
     *     "s": "BNBUSDT",          // 交易对
     *     "c": "0.0025",          // 最新成交价格
     *     "o": "0.0010",          // 24小时前开始第一笔成交价格
     *     "h": "0.0025",          // 24小时内最高成交价
     *     "l": "0.0010",          // 24小时内最低成交价
     *     "v": "10000",           // 成交量
     *     "q": "18"               // 成交额
     *   }
     *
     *   {
     *   "e": "aggTrade",  // 事件类型
     *   "E": 123456789,   // 事件时间
     *   "s": "BNBUSDT",    // 交易对
     *   "a": 5933014,		// 归集成交 ID
     *   "p": "0.001",     // 成交价格
     *   "q": "100",       // 成交量
     *   "f": 100,         // 被归集的首个交易ID
     *   "l": 105,         // 被归集的末次交易ID
     *   "T": 123456785,   // 成交时间
     *   "m": true         // 买方是否是做市方。如true，则此次成交是一个主动卖出单，否则是一个主动买入单。
     * }
     * @param linerMsg
     * @return
     */
    @Override
    public List<MarketPrice> coverLinerMsg(LinerReceiveMsg linerMsg) {
        String msg = linerMsg.getMsg();
        JSONObject binanceMarketMsg = JSONObject.parseObject(msg);
        if("aggTrade".equalsIgnoreCase(binanceMarketMsg.getString("e"))){
            MarketPrice marketPrice = new MarketPrice(binanceMarketMsg.getLong("T"),
                binanceMarketMsg.getBigDecimal("p"));
            marketPrice.setLastPrice(binanceMarketMsg.getBigDecimal("p"));
            marketPrice.setSymbol(linerMsg.getConnectionConfig().getOtherConfig().getSymbol());
            marketPrice.setExchange(ExchangeType.BINANCE.getName());
            return List.of(marketPrice);
        }


        return List.of();
    }
}
