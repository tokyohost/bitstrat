package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 机器人可使用账户对象 coins_bot_account
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_bot_account")
public class CoinsBotAccount extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 机器人id
     */
    private Long botId;

    /**
     * api account id
     */
    private Long accountId;

    /**
     * 用户id
     */
    private Long userId;


}
