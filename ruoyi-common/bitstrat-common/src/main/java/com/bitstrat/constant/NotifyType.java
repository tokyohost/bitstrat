package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 14:53
 * @Content
 */

public enum NotifyType {

    TELEGRAM("telegram", "telegram"),
    DING_TALK("dingtalk", "dingtalk");

    private String name;
    private String desc;

    NotifyType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
