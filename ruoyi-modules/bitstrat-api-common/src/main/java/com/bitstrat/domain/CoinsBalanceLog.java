package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 账户余额变动日志对象 coins_balance_log
 *
 * @author Lion Li
 * @date 2025-11-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_balance_log")
public class CoinsBalanceLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 变动前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 变动金额（正为增加，负为扣减）
     */
    private BigDecimal changeAmount;

    /**
     * 变动后余额
     */
    private BigDecimal afterBalance;

    /**
     * 变动类型：1=充值，2=消费，3=退款，4=赠送
     */
    private Long type;

    /**
     * 状态 1-处理中 2-已完成 3-异常
     */
    private Long status;

    /**
     * 备注信息，例如订单号/充值方式
     */
    private String remark;

    private String tradeNo;

    private String tradeStatus;


}
