package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 用户配置套利警告对象 coins_arbitrage_warning_config
 *
 * @author Lion Li
 * @date 2025-05-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_arbitrage_warning_config")
public class CoinsArbitrageWarningConfig extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 跨交易所套利任务ID
     */
    private Long taskId;

    /**
     * 套利类型：1:跨交易所套利，2:。。。详见枚举类
     */
    private Long arbitrageType;

    /**
     * 警告阈值
     */
    private BigDecimal warningThreshold;

    /**
     * 告警配置名称
     */
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
     * 状态
     */
    private Integer status;

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
