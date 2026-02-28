package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户通知设置对象 coins_notify_config
 *
 * @author Lion Li
 * @date 2025-04-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_notify_config")
public class CoinsNotifyConfig extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 配置类型 1-钉钉机器人通知 2-TG通知
     */
    private String type;

    /**
     * 钉钉 token
     */
    private String dingToken;

    /**
     * 钉钉 secret
     */
    private String dingSecret;

    /**
     * tg chart id
     */
    private String telegramChatId;

    /**
     * 用户id
     */
    private Long userId;


}
