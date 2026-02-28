package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 20:23
 * @Content
 */
public enum APITypeEnum {
    AI_CONTROL("1", "ai操盘"),
    AI_SUMMARY("2", "ai分析");

    private String type;
    private String desc;

    APITypeEnum(String type, String desc) {
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
