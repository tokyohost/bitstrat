package com.bitstrat.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 账户余额记录对象 coins_account_balance_record
 *
 * @author Lion Li
 * @date 2025-05-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_account_balance_record")
public class CoinsAccountBalanceRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id")
    private Long id;

    /**
     *
     */
    private Long userId;

    /**
     *
     */
    private String exchange;

    /**
     *
     */
    private BigDecimal balance;

    /**
     *
     */
    private BigDecimal cashBalance;

    /**
     *
     */
    private BigDecimal usdtBalance;

    /**
     *
     */
    private BigDecimal freeBalance;

    /**
     *
     */
    private Date recordTime;

    /**
     *
     */
    private Date recordDate;


}
