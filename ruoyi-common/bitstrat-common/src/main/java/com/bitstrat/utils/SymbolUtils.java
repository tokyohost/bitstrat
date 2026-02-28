package com.bitstrat.utils;

import com.bitstrat.constant.ExchangeType;

public class SymbolUtils {
    public static String formateLinerSymbolByEx(String ex, String symbol) {
        if (ExchangeType.BYBIT.getName().equalsIgnoreCase(ex)) {
            if(!symbol.endsWith("USDT")) {
                return symbol + "USDT";
            }
            return symbol;
        } else if (ExchangeType.OKX.getName().equalsIgnoreCase(ex)) {
            if(!symbol.endsWith("-USDT-SWAP")) {
                return symbol + "-USDT-SWAP";
            }else {
                return symbol;
            }
        }
        return symbol;
    }
}
