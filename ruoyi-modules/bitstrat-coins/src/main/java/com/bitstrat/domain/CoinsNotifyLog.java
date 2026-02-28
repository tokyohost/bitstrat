package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 通知日志对象 coins_notify_log
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_notify_log")
public class CoinsNotifyLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 通知类型 1-钉钉机器人通知 2-TG通知
     */
    private String notifyType;

    /**
     * 通知内容
     */
    private String notifyContent;

    /**
     * 通知状态 1-成功 2-失败
     */
    private String notifyStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 用户ID
     */
    private Long userId;


}
