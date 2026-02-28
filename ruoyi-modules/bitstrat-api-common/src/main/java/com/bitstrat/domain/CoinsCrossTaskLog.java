package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 跨交易所任务日志对象 coins_cross_task_log
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_cross_task_log")
public class CoinsCrossTaskLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 日志
     */
    private String msg;


}
