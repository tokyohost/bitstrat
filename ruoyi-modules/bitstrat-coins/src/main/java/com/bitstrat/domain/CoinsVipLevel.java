package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * VIP 权限对象 coins_vip_level
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_vip_level")
public class CoinsVipLevel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * VIP名称
     */
    private String name;

    /**
     * VIP等级
     */
    private Long level;

    /**
     * 最大套利金额 USDT
     */
    private Long maxAbAmount;

    /**
     * 最大同时允许运行中状态的任务数量
     */
    private Long maxActiveTask;

    /**
     * VIP状态，1-正常 2-禁用 3-不可购买
     */
    private Long status;

    /**
     * VIP 开通金额 单位USDT
     */
    private Long price;

    /**
     * 可用时长 单位-天
     */
    private Integer avaliableDay;


}
