package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinAiTaskRequest;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * AI 用户请求提示词业务对象 coin_test_ai_request
 *
 * @author Lion Li
 * @date 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinAiTaskRequest.class, reverseConvertGenerate = true)
public class CoinAiTaskRequestBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
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
     * 1-等待响应 2-已完成 3-已超时 4-异常
     */
    private Long status;

    private String sysContent;

    private Long aiId;

    private Long taskId;

    private String errorMsg;

    private String sysPrompt;
}
