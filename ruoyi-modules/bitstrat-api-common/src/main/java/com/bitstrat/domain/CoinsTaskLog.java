package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 任务买入卖出日志对象 coins_task_log
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_task_log")
public class CoinsTaskLog extends BaseEntity {

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
     * 价格
     */
    private Double price;
    private String express;
    private String orderId;
    private String msg;
    private Integer status;


    /**
     * 订单状态
     * 活动态
     *
     * New订单成功下达
     * PartiallyFilled部分成交
     * Untriggered条件单未触发
     * 终态
     *
     * Rejected订单被拒绝
     * PartiallyFilledCanceled仅现货存在该枚举值, 订单部分成交且已取消
     * Filled完全成交
     * Cancelled期货交易，当订单是该状态时，是可能存在部分成交的; 经典帐户的现货盈止损单、条件单、OCO订单触发前取消
     * Triggered已触发, 条件单从未触发到变成New的一个中间态
     * Deactivated统一帐户下期货、现货的盈止损单、条件单、OCO订单触发前取消
     */
    private String orderStatus;
    /**
     * 平均成交价格
     */
    private Double avgPrice;
    /**
     * 下单时市场价
     */
    private Double basePrice;

    private String tenantId;
    /**
     * 数量
     */
    private Double count;

    /**
     * 类型 1-买入  2-卖出
     */
    private Long type;

    /**
     * 总金额
     */
    private Double total;

    private String log;


}
