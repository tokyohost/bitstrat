package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/4 20:45
 * @Content
 * 1-已创建 2-正在运行 3-已终止
 */
public enum AITaskStatus {
    CREATED(1L, "已创建"),
    RUNNING(2L, "正在运行"),
    STOPD(3L, "已终止");

    private Long code;
    private String desc;
    AITaskStatus(Long code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Long getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
