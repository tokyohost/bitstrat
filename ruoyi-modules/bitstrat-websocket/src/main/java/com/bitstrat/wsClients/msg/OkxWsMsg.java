package com.bitstrat.wsClients.msg;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bitstrat.wsClients.utils.BybitAuthUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:50
 * @Content
 */
@Component
public class OkxWsMsg implements SubscriptMsgs {
    @Override
    public String exchangeName() {
        return ExchangeType.OKX.getName().toLowerCase();
    }

    @Override
    public String createSubscriptPositionMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","positions","instType","ANY","extraParams",JSONObject.toJSONString(Map.of("updateInterval",3000)))));
        return subscript.toJSONString();
    }

    @Override
    public String createSubscriptOrderMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","orders","instType","ANY")));
        return subscript.toJSONString();
    }

    @Override
    public String createSubscriptAccountMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","account","ccy","USDT","extraParams",JSONObject.toJSONString(Map.of("updateInterval",3000)))));
        return subscript.toJSONString();
    }

    /**
     * {
     *     "op": "subscribe",
     *     "args": [{
     *         "channel": "mark-price",
     *         "instId": "BTC-USDT"
     *     }]
     * }
     *
     * {
     *   "op": "subscribe",
     *   "args": [
     *     {
     *       "channel": "trades",
     *       "instId": "BTC-USDT-SWAP"
     *     }
     *   ]
     * }
     * @param symbol
     * @return
     */
    @Override
    public String createSwapMarketMsg(String symbol) {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","trades","instId",checkSwapSymbol(symbol))));
        return subscript.toJSONString();
    }

    private static String checkSwapSymbol(String symbol) {
        if(symbol.endsWith("/USDT")){
            symbol = symbol.replace("/", "");
        }
        symbol = symbol + "-USDT-SWAP";
        return symbol;
    }

    @Override
    public String createSwapMarketMsg(String symbol, String url) {
        return url;
    }
}
