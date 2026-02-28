package com.bitstrat.domain.vo;

import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.CoinsAbBot;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.bo.CoinsApiBo;
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
 * 套利机器人视图对象 coins_ab_bot
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAbBot.class)
public class CoinsAbBotVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 机器人名称
     */
    @ExcelProperty(value = "机器人名称")
    private String botName;

    /**
     * 套利币对百分比阈值(高于此阈值触发建仓)
     */
    @ExcelProperty(value = "套利币对百分比阈值(高于此阈值触发建仓)")
    private BigDecimal abPercentThreshold;

    /**
     * 距离资金费结算时间触发阈值(满足此时间内允许建仓/平仓)
     */
    @ExcelProperty(value = "距离资金费结算时间触发阈值(满足此时间内允许建仓/平仓)")
    private Long triggerMinutes;

    /**
     * 币对最低要求持仓量(高于此阈值触发建仓)（单位万美元）
     */
    @ExcelProperty(value = "币对最低要求持仓量(高于此阈值触发建仓)", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "单位万美元")
    private BigDecimal minVolume;

    /**
     * 杠杆倍数
     */
    @ExcelProperty(value = "杠杆倍数")
    private Long leverage;

    /**
     * 开仓最低USDT
     */
    @ExcelProperty(value = "开仓最低USDT")
    private BigDecimal minSize;

    /**
     * 开仓最高USDT
     */
    @ExcelProperty(value = "开仓最高USDT")
    private BigDecimal maxSize;

    /**
     * 分批每批最低下单USDT
     */
    @ExcelProperty(value = "分批每批最低下单USDT")
    private BigDecimal batchSize;

    /**
     * 状态  1-已创建 2-正在运行 3-已持仓
     */
    @ExcelProperty(value = "状态  1-已创建 2-正在运行 3-已持仓", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "batch_order_status")
    private Long status;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;


    /**
     * 持仓任务ID
     */
    private Long avaliableTaskId;

    /**
     * 允许最低收益率，超出触发平仓
     */
    private BigDecimal minAllowPercent;

    /**
     * 下单类型  {@link OrderType}
     */
    private String orderType;
    /**
     * 可使用的api账户列表
     */
    private List<CoinsApiVo> canUseApis;
}
