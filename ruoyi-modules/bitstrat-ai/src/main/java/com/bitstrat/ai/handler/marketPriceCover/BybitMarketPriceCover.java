package com.bitstrat.ai.handler.marketPriceCover;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.ai.domain.vo.OkxTradesVo;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/29 19:44
 * @Content
 */

@Component
public class BybitMarketPriceCover implements MarketPriceCover {
    @Override
    public String exchangeName() {
        return ExchangeType.BYBIT.getName();
    }

    /**
     *  {
     *     "topic": "publicTrade.BTCUSDT",
     *     "type": "snapshot",
     *     "ts": 1672304486868,
     *     "data": [
     *         {
     *             "T": 1672304486865,
     *             "s": "BTCUSDT",
     *             "S": "Buy",
     *             "v": "0.001",
     *             "p": "16578.50",
     *             "L": "PlusTick",
     *             "i": "20f43950-d8dd-5b31-9112-a178eb6023af",
     *             "BT": false
     *         }
     *     ]
     * }
     *
     * @param linerMsg
     * @return
     */
    @Override
    public List<MarketPrice> coverLinerMsg(LinerReceiveMsg linerMsg) {
        String msg = linerMsg.getMsg();
        JSONObject bybitMarketData = JSONObject.parseObject(msg);
        String type = bybitMarketData.getString("type");
        ArrayList<MarketPrice> marketPrices = new ArrayList<>();
        if(Objects.isNull(type)) {
            return List.of();
        }
        if(type.equalsIgnoreCase("snapshot")) {
            JSONArray datas = bybitMarketData.getJSONArray("data");
            for (Object from : datas) {
                JSONObject trade = JSONObject.from(from);
                MarketPrice marketPrice = new MarketPrice(trade.getLong("T"), trade.getBigDecimal("p"));
                marketPrice.setSymbol(linerMsg.getConnectionConfig().getOtherConfig().getSymbol());
                marketPrice.setExchange(ExchangeType.BYBIT.getName());
                marketPrice.setLastPrice(trade.getBigDecimal("p"));
                marketPrices.add(marketPrice);
            }
        }
        return marketPrices;
    }
}
