package com.bitstrat.domain;

import com.bitstrat.constant.OrderType;
import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 分批订单任务对象 coins_batch
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_batch")
public class CoinsBatch extends TenantEntity {

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
     * 买入交易所
     */
    private String buyEx;

    /**
     * 买入总数量
     */
    private Long buyTotal;

    /**
     * 卖出交易所
     */
    private String sellEx;

    /**
     * 卖出总数量
     */
    private Long sellTotal;

    /**
     * 总批次数量
     */
    private Long totalSize;

    /**
     * 总批次
     */
    private Long batchTotal;

    /**
     * 已完成批次
     */
    private Long doneBatch;

    /**
     * 已完成数量
     */
    private Long doneSize;

    /**
     * 状态 10-正在执行 20-执行异常 30-已执行完毕  40-已终止
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
    /**
     *操作类型，1-加仓，2-平仓'
     */
    private Integer side;

    /**
     * 每批次操作比例
     */
    private BigDecimal batchSize;

    private BigDecimal doneBuySize;
    private BigDecimal doneSellSize;

    private String symbol;
    /**
     * 下单类型  {@link OrderType}
     */
    private String buyOrderType;
    private String sellOrderType;

    /**
     * 杠杆倍数
     */
    private Integer buyLeverage;
    private Integer sellLeverage;

    private Date createTime;

    /**
     * 机器人Id
     */
    private Long botId;
}
