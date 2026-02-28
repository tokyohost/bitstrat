package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinTestAiResult;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * AI 操作日志业务对象 coin_test_ai_result
 *
 * @author Lion Li
 * @date 2025-10-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinTestAiResult.class, reverseConvertGenerate = false)
public class CoinTestAiResultBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 操作
     */
    private String action;

    /**
     * 杠杆
     */
    private Long leverage;

    /**
     * 数量
     */
    private String size;

    /**
     * 币对
     */
    private String symbol;

    /**
     * 止盈
     */
    private String takeProfit;

    /**
     * 止损
     */
    private String stopLoss;

    /**
     * 分析EN
     */
    private String reasoningEn;

    /**
     * 分析zh
     */
    private String reasoningZh;

    private String think;

    private String requestKey;
    /**
     * 任务ID
     */
    private Long taskId;

    private String result;

}
