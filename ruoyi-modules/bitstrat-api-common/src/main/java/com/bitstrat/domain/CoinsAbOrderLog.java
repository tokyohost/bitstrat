package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 价差套利日志对象 coins_ab_order_log
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_ab_order_log")
public class CoinsAbOrderLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * account a
     */
    private Long accountA;

    /**
     * account b
     */
    private Long accountB;

    /**
     * exchangea
     */
    private String exchangeA;

    /**
     * exchangeb
     */
    private String exchangeB;

    /**
     * TaskId
     */
    private String taskId;

    /**
     * 日志
     */
    private String log;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
