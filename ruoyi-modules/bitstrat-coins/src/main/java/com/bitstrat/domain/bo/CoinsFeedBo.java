package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsFeed;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 策略广场业务对象 coins_feed
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsFeed.class, reverseConvertGenerate = false)
public class CoinsFeedBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 策略ID
     */
    @NotNull(message = "策略不能为空", groups = { AddGroup.class })
    private Long strategyId;

    /**
     * 标题
     */
    @NotEmpty(message = "策略标题不能为空", groups = { AddGroup.class })
    @Min(4)
    @Max(12)
    private String title;

    /**
     * 标签（JSON数组）
     */
    @NotEmpty(message = "标签不能为空", groups = { AddGroup.class })
    private String tags;

    /**
     * 最近三个月收益
     */
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
     * 状态（1发布 0草稿 2隐藏）
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
    @NotEmpty(message = "内容不能为空", groups = { AddGroup.class })
    @Min(5)
    @Max(200)
    private String content;

}
