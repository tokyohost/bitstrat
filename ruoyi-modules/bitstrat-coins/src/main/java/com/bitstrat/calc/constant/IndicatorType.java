package com.bitstrat.calc.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/22 17:53
 * @Content
 */


public enum IndicatorType {
    EMA("EMA","EMA"),
    SMA("SMA","SMA"),
    MACD("MACD","MACD"),
    RSI("RSI","RSI");

    private String type;

    private String desc;

    IndicatorType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }


    public String getDesc() {
        return desc;
    }

    public IndicatorType getType(String type) {
        IndicatorType[] values = IndicatorType.values();
        for (IndicatorType value : values) {
            if (value.getType().equalsIgnoreCase(type)) {
                return value;
            }
        }

        return null;
    }
}
