package com.bitstrat.domain.vo;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.bitstrat.domain.CoinsFeed;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 策略广场视图对象 coins_feed
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsFeed.class)
public class CoinsFeedVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 策略ID
     */
    @ExcelProperty(value = "策略ID")
    private Long strategyId;

    /**
     * 标题
     */
    @ExcelProperty(value = "标题")
    private String title;

    /**
     * 标签（JSON数组）
     */
    @ExcelProperty(value = "标签", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "J=SON数组")
    private String tags;

    /**
     * 最近三个月收益
     */
    @ExcelProperty(value = "最近三个月收益")
    private BigDecimal profit3m;

    /**
     * 点赞次数
     */
    @ExcelProperty(value = "点赞次数")
    private Long likeCount;

    /**
     * 浏览次数
     */
    @ExcelProperty(value = "浏览次数")
    private Long viewCount;

    /**
     * 状态（1发布 0草稿 2隐藏）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=发布,0=草稿,2=隐藏")
    private Long status;

    /**
     * 排序（越大越靠前）
     */
    private Long sort;

    /**
     * 分享时间
     */
    @ExcelProperty(value = "分享时间")
    private ZonedDateTime shareTime;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    private Boolean isLiked;
    private String content;

}
