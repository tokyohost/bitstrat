package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsAccountBalanceRecord;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 账户余额记录业务对象 coins_account_balance_record
 *
 * @author Lion Li
 * @date 2025-05-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsAccountBalanceRecord.class, reverseConvertGenerate = false)
public class CoinsAccountBalanceRecordBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date recordTime;

    /**
     *
     */
    private Date recordDate;

    /**
     * 查询最近N天
     */
    private Integer days;


}
