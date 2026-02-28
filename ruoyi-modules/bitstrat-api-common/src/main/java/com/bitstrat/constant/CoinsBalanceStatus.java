package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/4 11:41
 * @Content
 */
public enum CoinsBalanceStatus {
    PROCESS(1L,"处理中"),
    SUCCESS(2L,"已完成"),
    ERROR(3L,"异常"),
    CANCEL(4L,"已取消");

    private Long status;
    private String desc;

    CoinsBalanceStatus(Long status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Long getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
