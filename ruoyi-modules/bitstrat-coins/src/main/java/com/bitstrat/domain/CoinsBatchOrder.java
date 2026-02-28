package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 分批任务订单记录对象 coins_batch_order
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_batch_order")
public class CoinsBatchOrder extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 批次id
     */
    private Long batchId;

    /**
     * 当前执行批次
     */
    private Long currBatch;

    /**
     * 下单数量
     */
    private Long orderSize;

    /**
     * 状态 10-已下单 20-已成交 30-异常
     */
    private Long status;

    /**
     * 异常信息
     */
    private String msg;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 用户id
     */
    private Long userId;


}
