package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsVipLevel;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * VIP 权限视图对象 coins_vip_level
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsVipLevel.class)
public class CoinsVipLevelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * VIP名称
     */
    @ExcelProperty(value = "VIP名称")
    private String name;

    /**
     * VIP等级
     */
    @ExcelProperty(value = "VIP等级")
    private Long level;

    /**
     * 最大套利金额 USDT
     */
    @ExcelProperty(value = "最大套利金额 USDT")
    private Long maxAbAmount;

    /**
     * 最大同时允许运行中状态的任务数量
     */
    @ExcelProperty(value = "最大同时允许运行中状态的任务数量")
    private Long maxActiveTask;

    /**
     * VIP状态，1-正常 2-禁用 3-不可购买
     */
    @ExcelProperty(value = "VIP状态，1-正常 2-禁用 3-不可购买")
    private Long status;

    /**
     * VIP 开通金额 单位USDT
     */
    @ExcelProperty(value = "VIP 开通金额 单位USDT")
    private Long price;

    /**
     * 可用时长 单位-天
     */
    @ExcelProperty(value = "可用时长 单位-天")
    private Long avaliableDay;


}
