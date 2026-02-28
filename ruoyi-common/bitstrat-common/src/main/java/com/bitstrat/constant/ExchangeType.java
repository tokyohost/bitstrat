package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 14:53
 * @Content
 */

public enum ExchangeType {

    BYBIT("bybit", "bybit",true,"Bybit"),
//    BIANCE("binance", "binance"),
    BITGET("bitget", "bitget",false,"Bitget"),
    OKX("okx", "okx",false,"OKX"),
    BINANCE("binance", "binance",true,"Binance");

    private String name;
    private String desc;
    private Boolean disabled;
    private String coinsGlassQuery;

    ExchangeType(String name, String desc, Boolean disabled, String coinsGlassQuery) {
        this.name = name;
        this.desc = desc;
        this.disabled = disabled;
        this.coinsGlassQuery = coinsGlassQuery;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getCoinsGlassQuery() {
        return coinsGlassQuery;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public static ExchangeType getExchangeType(String name) {
        ExchangeType[] values = ExchangeType.values();
        for (ExchangeType value : values) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
