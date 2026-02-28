package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 任务管理视图对象 coins_task
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsTask.class)
public class CoinsTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 任务名称
     */
    @ExcelProperty(value = "任务名称")
    private String name;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种")
    private String symbol;

    /**
     * 持仓
     */
    @ExcelProperty(value = "持仓")
    private Double balance;

    /**
     * 单次下单数量
     */
    @ExcelProperty(value = "单次下单数量")
    private Double singleOrder;

    /**
     * 每次下单冷却时间
     */
    @ExcelProperty(value = "每次下单冷却时间")
    private Long coldSec;

    /**
     * 可用额度
     */
    @ExcelProperty(value = "可用额度")
    private Double totalBalance;

    /**
     * 上次下单时间
     */
    @ExcelProperty(value = "上次下单时间")
    private Date lastOrderTime;

    /**
     * 使用策略类型  AI/normal
     */
    @ExcelProperty(value = "使用策略类型  AI/normal")
    private String taskType;

    private String strategyConfig;
    private String tenantId;

    private Long scale;

    private String lastSellRole;
    private String lastBuyRole;
    private Long sellRoleId;
    private Long buyRoleId;

    private String buyRoleParams;
    private String sellRoleParams;

    /**
     * 时间粒度
     */
    private String interval;

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
     * ai 流水线id
     */
    @ExcelProperty(value = "ai 流水线id")
    private Long aiWorkflowId;

    /**
     * 普通策略id
     */
    @ExcelProperty(value = "普通策略id")
    private Long roleId;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private Long createUserId;

    private Long createBy;

    @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "createBy")
    private String createUserName;

    /**
     * 任务状态 1-已创建 2-正在运行 3-已终止
     */
    @ExcelProperty(value = "任务状态 1-已创建 2-正在运行 3-已终止")
    private Long status;


}
