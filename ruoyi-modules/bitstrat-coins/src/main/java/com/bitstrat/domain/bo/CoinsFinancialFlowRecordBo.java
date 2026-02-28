package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsFinancialFlowRecord;
import lombok.Builder;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 交易所资金流水记录业务对象 coins_financial_flow_record
 *
 * @author Lion Li
 * @date 2025-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsFinancialFlowRecord.class, reverseConvertGenerate = false)
public class CoinsFinancialFlowRecordBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 交易所原始流水ID
     */
    @NotBlank(message = "交易所原始流水ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String exchangeRecordId;

    /**
     * 交易所名称
     */
    @NotBlank(message = "交易所名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String exchange;

    /**
     * 资金流水类型
     */
    @NotBlank(message = "资金流水类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String flowType;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 流水发生时间
     */
    @NotNull(message = "流水发生时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private LocalDateTime timestamp;

    /**
     * 金额(正数为收入/负数为支出)
     */
    @NotNull(message = "金额(正数为收入/负数为支出)不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * 资产类型
     */
    @NotBlank(message = "资产类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String asset;


}
