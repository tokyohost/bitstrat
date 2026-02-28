package com.bitstrat.domain.vo;

import java.util.Date;

import com.bitstrat.domain.CoinsUserVip;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 用户VIP 状态视图对象 coins_user_vip
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsUserVip.class)
public class CoinsUserVipVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * VIP ID
     */
    @ExcelProperty(value = "VIP ID")
    private Long vipId;

    /**
     * 购买时间
     */
    @ExcelProperty(value = "购买时间")
    private Date buyTime;

    /**
     * 过期时间
     */
    @ExcelProperty(value = "过期时间")
    private Date expireTime;

    /**
     * 会员状态 1-正常 2-禁用 3-过期
     */
    @ExcelProperty(value = "会员状态 1-正常 2-禁用 3-过期")
    private Long status;


    /**
     * 是否是续费 0-不是 1-是
     */
    private Integer isRenew;

    /**
     * 续费id
     */
    private Long renewId;


}
