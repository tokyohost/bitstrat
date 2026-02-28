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
public class BitgetWsMsg implements SubscriptMsgs {
    @Override
    public String exchangeName() {
        return ExchangeType.BITGET.getName().toLowerCase();
    }

    @Override
    public String createSubscriptPositionMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","positions","instType","USDT-FUTURES","instId","default")));
        return subscript.toJSONString();
    }

    @Override
    public String createSubscriptOrderMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","orders","instType","USDT-FUTURES","instId","default")));
        return subscript.toJSONString();
    }

    @Override
    public String createSubscriptAccountMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","account","instType","USDT-FUTURES","coin","default")));
        return subscript.toJSONString();
    }

    @Override
    public String createSwapMarketMsg(String symbol) {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("args",List.of(Map.of("channel","ticker","instType","USDT-FUTURES","instId",checkSymbolLinerV2(symbol))));
        return subscript.toJSONString();
    }

    private static String checkSymbolLinerV2(String symbol) {
        if (!symbol.endsWith("USDT")) {
            symbol = symbol + "USDT";
        }
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/", "_");
        }
        return symbol.toUpperCase();
    }

    @Override
    public String createSwapMarketMsg(String symbol, String url) {
        return url;
    }
}
