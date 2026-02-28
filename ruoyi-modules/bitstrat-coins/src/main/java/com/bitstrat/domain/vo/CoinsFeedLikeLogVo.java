package com.bitstrat.domain.vo;

import com.bitstrat.domain.CoinsFeedLikeLog;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 策略广场like日志视图对象 coins_feed_like_log
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsFeedLikeLog.class)
public class CoinsFeedLikeLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * feed ID
     */
    @ExcelProperty(value = "feed ID")
    private Long feedId;

    /**
     * 点赞用户
     */
    @ExcelProperty(value = "点赞用户")
    private Long userId;


}
