package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/4 11:41
 * @Content
 * 变动类型：1=充值，2=消费，3=退款，4=赠送
 */
public enum CoinsBalanceType {
    RE_CHARGE(1L,"充值"),
    USED(2L,"消费"),
    REFUND(3L,"退款"),
    GIFT(4L,"赠送");

    private Long status;
    private String desc;

    CoinsBalanceType(Long status, String desc) {
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
