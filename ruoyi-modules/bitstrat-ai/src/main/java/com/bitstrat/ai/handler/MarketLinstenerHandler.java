package com.bitstrat.ai.handler;

import org.springframework.stereotype.Component;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 16:22
 * @Content
 */
@Component
public class MarketLinstenerHandler {

//    @EventListener
//    public void okxHandleMarketLinstener(OkxWsMarketMsgData wsMarketMsgData) {
//        /**
//         * {
//         *     "arg": {
//         *         "channel": "mark-price",
//         *         "instId": "DOGE-USDT-SWAP"
//         *     },
//         *     "data": [
//         *         {
//         *             "instId": "DOGE-USDT-SWAP",
//         *             "instType": "SWAP",
//         *             "markPx": "0.22403",
//         *             "ts": "1747383470270"
//         *         }
//         *     ]
//         * }
//         */
//
//        OkxArg arg = wsMarketMsgData.getArg();
//        MarketPriceDisruptor disruptorByExAndSymbol = MarketStore.getDisruptorByExAndSymbol(ExchangeType.OKX.getName(), arg.getInstId());
//        JSONArray data = wsMarketMsgData.getData();
//        for (Object datum : data) {
//            MarketPriceDataItem marketPriceDataItem = JSONObject.from(datum).to(MarketPriceDataItem.class);
//            MarketPrice marketPrice = new MarketPrice(marketPriceDataItem.getTs(),marketPriceDataItem.getMarkPx().doubleValue());
//            marketPrice.setSymbol(arg.getInstId());
//            disruptorByExAndSymbol.publishPrice(marketPrice);
//        }
//
//
//
//    }
}
