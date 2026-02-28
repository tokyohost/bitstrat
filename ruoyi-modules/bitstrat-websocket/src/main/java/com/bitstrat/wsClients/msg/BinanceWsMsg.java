package com.bitstrat.wsClients.msg;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
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
public class BinanceWsMsg implements SubscriptMsgs {
    @Override
    public String exchangeName() {
        return ExchangeType.BINANCE.getName().toLowerCase();
    }

    @Override
    public String createSubscriptPositionMsg() {
        return null;
    }

    @Override
    public String createSubscriptOrderMsg() {
       return null;
    }

    @Override
    public String createSubscriptAccountMsg() {
        return null;
    }

    @Override
    public String createSwapMarketMsg(String symbol) {
        //wss://fstream.binance.com/ws/btcusdt@markPrice@1s
        return null;
    }

    @Override
    public String createSwapMarketMsg(String symbol, String url) {
//        String subscript = checkSymbolLinerV2(symbol) + "@markPrice@1s";
//        String subscript = checkSymbolLinerV2(symbol) + "@miniTicker";
        String subscript = checkSymbolLinerV2(symbol) + "@aggTrade";
        if(url.endsWith("/")) {
           return url + subscript;
        }else{
            return url + "/" + subscript;
        }
    }

    private static String checkSymbolLinerV2(String symbol) {
        symbol = symbol.toLowerCase();
        if (!symbol.endsWith("usdt")) {
            symbol = symbol + "usdt";
        }
        if (symbol.endsWith("/usdt")) {
            symbol = symbol.replace("/", "_");
        }
        return symbol.toLowerCase();
    }
}
