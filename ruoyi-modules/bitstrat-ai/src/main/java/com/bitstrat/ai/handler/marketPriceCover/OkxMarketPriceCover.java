package com.bitstrat.ai.handler.marketPriceCover;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.ai.domain.vo.OkxTickerVo;
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
public class OkxMarketPriceCover implements MarketPriceCover {
    @Override
    public String exchangeName() {
        return ExchangeType.OKX.getName();
    }

    /**
     *   {
     *   "arg": {
     *     "channel": "mark-price",
     *     "instId": "NMR-USDT-SWAP"
     *   },
     *   "data": [
     *     {
     *       "instId": "NMR-USDT-SWAP",
     *       "instType": "SWAP",
     *       "markPx": "10.106",
     *       "ts": "1748601427508"
     *     }
     *   ]
     * }
     *{
     *     "arg": {
     *         "channel": "trades",
     *         "instId": "BTC-USDT-SWAP"
     *     },
     *     "data": [
     *         {
     *             "instId": "BTC-USDT-SWAP",
     *             "tradeId": "1563994577",
     *             "px": "105868.4",
     *             "sz": "0.49",
     *             "side": "sell",
     *             "ts": "1748603981724",
     *             "count": "1"
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
        JSONObject okxMarketData = JSONObject.parseObject(msg);
        JSONObject arg = okxMarketData.getJSONObject("arg");
        ArrayList<MarketPrice> marketPrices = new ArrayList<>();
        if(!okxMarketData.containsKey("event") && Objects.nonNull(arg)) {
            if(arg.getString("channel").contains("trades")) {
                List<OkxTradesVo> list = okxMarketData.getJSONArray("data").toList(OkxTradesVo.class);
                for (OkxTradesVo okxTickerVo : list) {
                    //处理mark-price数据
                    MarketPrice marketPrice = new MarketPrice(okxTickerVo.getTs(),
                        okxTickerVo.getPx());
                    marketPrice.setMarkPrice(okxTickerVo.getPx());
                    marketPrice.setLastPrice(okxTickerVo.getPx());
                    marketPrice.setSymbol(linerMsg.getConnectionConfig().getOtherConfig().getSymbol());
                    marketPrice.setExchange(ExchangeType.OKX.getName());
                    marketPrices.add(marketPrice);
                }
            }
        }
        return marketPrices;
    }
}
