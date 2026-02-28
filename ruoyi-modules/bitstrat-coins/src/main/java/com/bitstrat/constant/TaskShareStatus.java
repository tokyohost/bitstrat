package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/12 17:42
 * @Content状态（1发布 0草稿 2隐藏 3删除）
 */

public enum TaskShareStatus {
    NOT_SHARE(1L, "未分享"),
    SHARED(2L, "已分享");

    private Long status;
    private String desc;

    TaskShareStatus(Long status, String desc) {
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
