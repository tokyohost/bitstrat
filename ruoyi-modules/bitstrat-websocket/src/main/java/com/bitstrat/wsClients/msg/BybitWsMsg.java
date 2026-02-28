package com.bitstrat.wsClients.msg;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bitstrat.wsClients.utils.BybitAuthUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:50
 * @Content
 */
@Component
public class BybitWsMsg implements SubscriptMsgs {
    @Override
    public String exchangeName() {
        return ExchangeType.BYBIT.getName().toLowerCase();
    }

    @Override
    public String createSubscriptPositionMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("req_id", BybitAuthUtils.generateTransferID());
        subscript.put("args", List.of("position"));
        return subscript.toJSONString();
    }

    @Override
    public String createSubscriptOrderMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("req_id", BybitAuthUtils.generateTransferID());
        subscript.put("args", List.of("execution"));
        return subscript.toJSONString();
    }

    @Override
    public String createSubscriptAccountMsg() {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("req_id", BybitAuthUtils.generateTransferID());
        subscript.put("args", List.of("wallet"));
        return subscript.toJSONString();
    }

    @Override
    public String createSwapMarketMsg(String symbol) {
        JSONObject subscript = new JSONObject();
        subscript.put("op", "subscribe");
        subscript.put("req_id", BybitAuthUtils.generateTransferID());
        subscript.put("args", List.of("publicTrade."+checkSymbolLiner(symbol)));
        return subscript.toJSONString();
    }
    private static String checkSymbolLiner(String symbol) {
        if (!symbol.endsWith("USDT")) {
            symbol = symbol + "USDT";
        }
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/", "");
        }
        return symbol;
    }

    @Override
    public String createSwapMarketMsg(String symbol, String url) {
        return url;
    }
}
