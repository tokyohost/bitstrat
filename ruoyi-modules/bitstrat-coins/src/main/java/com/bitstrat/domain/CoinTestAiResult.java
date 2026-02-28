package com.bitstrat.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * AI 操作日志对象 coin_test_ai_result
 *
 * @author Lion Li
 * @date 2025-10-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coin_test_ai_result")
public class CoinTestAiResult extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
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

    private Date createTime;
    private String think;
    private String requestKey;

    /**
     * 任务ID
     */
    private Long taskId;


    private String result;
}
