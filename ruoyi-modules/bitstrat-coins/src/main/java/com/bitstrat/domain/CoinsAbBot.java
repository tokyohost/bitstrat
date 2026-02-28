package com.bitstrat.domain;

import com.bitstrat.constant.OrderType;
import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 套利机器人对象 coins_ab_bot
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_ab_bot")
public class CoinsAbBot extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 机器人名称
     */
    private String botName;

    /**
     * 套利币对百分比阈值(高于此阈值触发建仓)
     */
    private BigDecimal abPercentThreshold;

    /**
     * 距离资金费结算时间触发阈值(满足此时间内允许建仓/平仓)
     */
    private Long triggerMinutes;

    /**
     * 币对最低要求持仓量(高于此阈值触发建仓)（单位万美元）
     */
    private BigDecimal minVolume;

    /**
     * 杠杆倍数
     */
    private Long leverage;

    /**
     * 开仓最低USDT
     */
    private BigDecimal minSize;

    /**
     * 开仓最高USDT
     */
    private BigDecimal maxSize;

    /**
     * 分批每批最低下单USDT
     */
    private BigDecimal batchSize;

    /**
     * 状态  1-已创建 2-正在运行 3-已持仓
     */
    private Long status;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 允许最低收益率，超出触发平仓
     */
    private BigDecimal minAllowPercent;
    /**
     * 下单类型  {@link OrderType}
     */
    private String orderType;


    /**
     * 持仓任务ID
     */
    private Long avaliableTaskId;
}
