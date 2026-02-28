package com.bitstrat.domain.vo;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.ApiSettingVo;
import com.bitstrat.domain.ApiVO;
import com.bitstrat.domain.CoinsAiTask;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * AI任务视图对象 coins_ai_task
 *
 * @author Lion Li
 * @date 2025-11-24
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAiTask.class)
public class CoinsAiTaskVo implements Serializable {

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
    private String symbols;

    /**
     * 开始资金USDT
     */
    @ExcelProperty(value = "开始资金USDT")
    private BigDecimal startBalance;

    /**
     * 可用额度
     */
    @ExcelProperty(value = "可用额度")
    private BigDecimal totalBalance;

    /**
     * ai 流水线id
     */
    @ExcelProperty(value = "ai 流水线id")
    private Long aiWorkflowId;

    /**
     * 系统提示词
     */
    @ExcelProperty(value = "系统提示词")
    private String systemPrompt;

    /**
     * 用户提示词
     */
    @ExcelProperty(value = "用户提示词")
    private String userPrompt;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private Long createUserId;

    /**
     * 任务状态 1-已创建 2-正在运行 3-已终止
     */
    @ExcelProperty(value = "任务状态 1-已创建 2-正在运行 3-已终止")
    private Long status;

    /**
     * 时间粒度
     */
    @ExcelProperty(value = "时间粒度")
    private String interval;
    private String exchange;

    private Long apiId;

    private ApiVO account;
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

    private Long shareStatus;
    private Long shareId;

    /**
     * 其它配置项
     */
    private String extConfig;
}
