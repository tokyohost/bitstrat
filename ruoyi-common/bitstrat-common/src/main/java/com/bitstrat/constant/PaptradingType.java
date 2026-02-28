package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/5 10:30
 * @Content
 */

public enum PaptradingType {

    TEST("1", "模拟盘"),
    PRO("0", "实盘");

    private String type;
    private String desc;

    PaptradingType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
