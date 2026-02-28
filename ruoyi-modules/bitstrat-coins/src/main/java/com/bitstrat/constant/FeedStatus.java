package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/12 17:42
 * @Content状态（1发布 0草稿 2隐藏 3删除）
 */

public enum FeedStatus {
    DRIFT(0L, "发布"),
    PUBLISH(1L, "发布"),
    HIDLE(2L, "隐藏"),
    DELETED(3L, "删除");

    private Long status;
    private String desc;

    FeedStatus(Long status, String desc) {
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
