package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsUserVip;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 用户VIP详情 状态视图对象 coins_user_vip 关联 coins_vip_level
 * @author caoyang
 * @date 2025-05-17
 */

@Data
public class CoinsUserVipInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * coins_user_vip id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * VIP ID
     */
    private Long vipId;

    /**
     * 购买时间
     */
    private Date buyTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 会员状态 1-正常 2-禁用 3-过期
     */
    private Long status;



    /**
     * VIP名称
     */
    private String name;

    /**
     * VIP等级
     */
    private Long level;

    /**
     * 最大套利金额 USDT
     */
    private Long maxAbAmount;

    /**
     * 最大同时允许运行中状态的任务数量
     */
    private Long maxActiveTask;

    /**
     * VIP 开通金额 单位USDT
     */
    private Long price;

    /**
     * 可用时长 单位-天
     */
    private Long avaliableDay;


}
