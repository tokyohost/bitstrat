package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 任务管理对象 coins_task
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_task")
public class CoinsTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 持仓
     */
    private Double balance;

    /**
     * 单次下单数量
     */
    private Double singleOrder;

    /**
     * 每次下单冷却时间
     */
    private Long coldSec;

    /**
     * 可用额度
     */
    private Double totalBalance;

    /**
     * 上次下单时间
     */
    private Date lastOrderTime;

    /**
     * 使用策略类型  AI/normal
     */
    private String taskType;
    private String tenantId;
    private Long scale;
    private String lastSellRole;
    private String lastBuyRole;
    private Long sellRoleId;
    private Long buyRoleId;
    private String buyRoleParams;
    private String sellRoleParams;
    /**
     * 策略配置
     */
    private String strategyConfig;


    /**
     * 平均持仓价格
     */
    private Double avgPrice;

    /**
     * 仓位价值
     */
    private String positionValue;

    /**
     * 市场价
     */
    private String markPrice;


    /**
     * 未结盈亏
     */
    private String unrealisedPnl;
    /**
     * 时间粒度
     */
    @TableField("`interval`")
    private String interval;
    /**
     * ai 流水线id
     */
    private Long aiWorkflowId;

    /**
     * 普通策略id
     */
    private Long roleId;

    /**
     * 创建人
     */
    private Long createUserId;

    /**
     * 任务状态 1-已创建 2-正在运行 3-已终止
     */
    private Long status;


}
