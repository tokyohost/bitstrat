package com.bitstrat.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

import java.io.Serial;

/**
 * AI 余额日志对象 coin_ai_task_balance
 *
 * @author Lion Li
 * @date 2025-10-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coin_ai_task_balance")
public class CoinAITaskBalance extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 金额
     */
    private BigDecimal equity;

    /**
     * 可用
     */
    private BigDecimal freeBalance;

    /**
     * 时间戳
     */
    private Date time;

    private Long taskId;

}
