package com.bitstrat.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI 用户请求提示词对象 coin_test_ai_request
 *
 * @author Lion Li
 * @date 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coin_ai_task_request")
public class CoinAiTaskRequest extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 请求key
     */
    private String requestKey;

    /**
     * userPrompt
     */
    private String content;
    private String result;

    private Date responseTime;

    private Long token;

    private BigDecimal price;

    /**
     * 1-等待响应 2-已完成 3-已超时
     */
    private Long status;
    private String sysContent;

    private Long aiId;

    private Long taskId;
    private String errorMsg;

    private Date createTime;
    private Date updateTime;

    private String sysPrompt;
}
