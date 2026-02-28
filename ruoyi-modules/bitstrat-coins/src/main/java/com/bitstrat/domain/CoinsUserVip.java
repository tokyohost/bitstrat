package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 用户VIP 状态对象 coins_user_vip
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_user_vip")
public class CoinsUserVip extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * VIP ID
     */
    private Long vipId;

    /**
     * 购买时间
     */
    private Date buyTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 会员状态 1-正常 2-禁用 3-过期
     */
    private Integer status;

    /**
     * 是否是续费
     */
    private Integer isRenew;

    /**
     * 续费id
     */
    private Long renewId;
}
