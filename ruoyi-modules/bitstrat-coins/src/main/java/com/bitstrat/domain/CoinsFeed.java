package com.bitstrat.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 策略广场对象 coins_feed
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_feed")
public class CoinsFeed extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签（JSON数组）
     */
    private String tags;

    /**
     * 最近三个月收益
     */
    @TableField(value = "profit_3m")
    private BigDecimal profit3m;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 状态（1发布 0草稿 2隐藏 3删除）
     */
    private Long status;

    /**
     * 排序（越大越靠前）
     */
    private Long sort;

    /**
     * 分享时间
     */
    private ZonedDateTime shareTime;

    /**
     * 用户id
     */
    private Long userId;

    private String content;


}
