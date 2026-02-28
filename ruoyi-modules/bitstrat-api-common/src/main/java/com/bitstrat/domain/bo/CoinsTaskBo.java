package com.bitstrat.domain.bo;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.CoinsTask;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * 任务管理业务对象 coins_task
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsTask.class, reverseConvertGenerate = false)
public class CoinsTaskBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
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
    private Long scale;


    private String lastSellRole;
    private String lastBuyRole;
    private Long sellRoleId;
    private Long buyRoleId;
    private String strategyConfig;

    private String buyRoleParams;
    private String sellRoleParams;

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
    private String interval;
    private JSONObject strategyConfigJSON;
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
