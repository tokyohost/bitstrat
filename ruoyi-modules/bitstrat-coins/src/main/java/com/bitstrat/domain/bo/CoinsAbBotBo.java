package com.bitstrat.domain.bo;

import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.CoinsAbBot;
import com.bitstrat.domain.vo.CoinsApiVo;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 套利机器人业务对象 coins_ab_bot
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsAbBot.class, reverseConvertGenerate = false)
public class CoinsAbBotBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 机器人名称
     */
    private String botName;

    /**
     * 套利币对百分比阈值(高于此阈值触发建仓)
     */
    private BigDecimal abPercentThreshold;

    /**
     * 距离资金费结算时间触发阈值(满足此时间内允许建仓/平仓)
     */
    private Long triggerMinutes;

    /**
     * 币对最低要求持仓量(高于此阈值触发建仓)（单位万美元）
     */
    private BigDecimal minVolume;

    /**
     * 杠杆倍数
     */
    private Long leverage;

    /**
     * 开仓最低USDT
     */
    private BigDecimal minSize;

    /**
     * 开仓最高USDT
     */
    private BigDecimal maxSize;

    /**
     * 分批每批最低下单USDT
     */
    private BigDecimal batchSize;

    /**
     * 状态  1-已创建 2-正在运行 3-已持仓
     */
    private Long status;

    /**
     * 用户id
     */
//    @NotNull(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
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
