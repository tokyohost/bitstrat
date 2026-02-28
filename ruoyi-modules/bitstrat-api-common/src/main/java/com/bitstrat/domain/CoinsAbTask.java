package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 价差套利任务对象 coins_ab_task
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_ab_task")
public class CoinsAbTask extends TenantEntity {

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
    private String taskId;

    /**
     * 任务体
     */
    private String body;

    /**
     * 用户id
     */
    private Long userId;


}
