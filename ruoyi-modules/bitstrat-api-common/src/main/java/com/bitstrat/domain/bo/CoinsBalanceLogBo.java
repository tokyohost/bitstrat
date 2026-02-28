package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsBalanceLog;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 账户余额变动日志业务对象 coins_balance_log
 *
 * @author Lion Li
 * @date 2025-11-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsBalanceLog.class, reverseConvertGenerate = false)
public class CoinsBalanceLogBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 变动前余额
     */
    @NotNull(message = "变动前余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal beforeBalance;

    /**
     * 变动金额（正为增加，负为扣减）
     */
    @NotNull(message = "变动金额（正为增加，负为扣减）不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal changeAmount;

    /**
     * 变动后余额
     */
    @NotNull(message = "变动后余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal afterBalance;

    /**
     * 变动类型：1=充值，2=消费，3=退款，4=赠送
     */
    @NotNull(message = "变动类型：1=充值，2=消费，3=退款，4=赠送不能为空", groups = { AddGroup.class, EditGroup.class })
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
