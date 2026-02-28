package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsAiTask;
import org.dromara.common.core.validate.StartGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * AI任务业务对象 coins_ai_task
 *
 * @author Lion Li
 * @date 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsAiTask.class, reverseConvertGenerate = true)
public class CoinsAiTaskBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    @NotNull(message = "id不能为空", groups = { StartGroup.class })
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
    private String interval;

    private String exchange;

    private Long apiId;
    private Date startTime;

    private Date lastRunTime;

    private Date createTime;
    private Long leverageMin;
    private Long leverageMax;
    private List<Long> leverage;

    private String shortTermInterval;
    private Integer needMiddleTerm;
    private String middleTermInterval;
    private String longTermInterval;

    /**
     * 其它配置项
     */
    private String extConfig;
}
