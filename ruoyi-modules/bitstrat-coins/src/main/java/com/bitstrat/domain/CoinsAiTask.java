package com.bitstrat.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * AI任务对象 coins_ai_task
 *
 * @author Lion Li
 * @date 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_ai_task")
public class CoinsAiTask extends TenantEntity {

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
    private String symbols;

    /**
     * 开始资金USDT
     */
    private BigDecimal startBalance;

    /**
     * 可用额度
     */
    private BigDecimal totalBalance;

    /**
     * ai 流水线id
     */
    private Long aiWorkflowId;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户提示词
     */
    private String userPrompt;

    /**
     * 创建人
     */
    private Long createUserId;

    /**
     * 任务状态 1-已创建 2-正在运行 3-已终止
     */
    private Long status;

    /**
     * 时间粒度
     */
    @TableField(value = "`interval`")
    private String interval;

    private String exchange;

    private Long apiId;

    private Date startTime;

    private Date lastRunTime;

    private Date createTime;

    private Long leverageMin;
    private Long leverageMax;

    private String shortTermInterval;
    private Integer needMiddleTerm;
    private String middleTermInterval;
    private String longTermInterval;

    /**
     * 其它配置项
     */
    private String extConfig;

}
