package com.bitstrat.domain.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsArbitrageWarningConfig;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 用户配置套利警告业务对象 coins_arbitrage_warning_config
 *
 * @author Lion Li
 * @date 2025-05-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsArbitrageWarningConfig.class, reverseConvertGenerate = false)
public class CoinsArbitrageWarningConfigBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 跨交易所套利任务ID
     */
    @NotNull(message = "跨交易所套利任务ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long taskId;

    /**
     * 套利类型：1:跨交易所套利，2:。。。详见枚举类
     */
    @NotNull(message = "套利类型：1:跨交易所套利，2:。。。详见枚举类不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer arbitrageType = 1;

    /**
     * 警告阈值
     */
    @NotNull(message = "警告阈值不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal warningThreshold;

    /**
     * 告警配置名称
     */
//    @NotBlank(message = "告警配置名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String configName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 状态，1-正常，0-停用
     */
    private Integer status;

    /**
     * 交易币对
     */
    private String symbol;


    /**
     * 爆仓预警状态
     */
    private Integer liquidationConfigStatus;

    /**
     * 做多爆仓预警阈值
     */
    private BigDecimal longLiquidationThreshold;

    /**
     * 做空爆仓预警阈值
     */
    private BigDecimal shortLiquidationThreshold;
}
