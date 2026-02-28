package com.bitstrat.domain.bo;

import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.CoinsBatch;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 分批订单任务业务对象 coins_batch
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsBatch.class, reverseConvertGenerate = false)
public class CoinsBatchBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 买入交易所
     */
    private String buyEx;

    /**
     * 买入总数量
     */
    private BigDecimal buyTotal;

    /**
     * 卖出交易所
     */
    private String sellEx;

    /**
     * 卖出总数量
     */
    private BigDecimal sellTotal;

    /**
     * 总批次数量
     */
    private BigDecimal totalSize;

    /**
     * 总批次
     */
    private Long batchTotal;

    /**
     * 已完成批次
     */
    private Long doneBatch;

    /**
     * 已完成数量
     */
    private BigDecimal doneSize;

    /**
     * 状态 10-正在执行 20-执行异常 30-已执行完毕  40-已终止
     */
    private Long status;

    /**
     * 异常信息
     */
    private String msg;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     *操作类型，1-加仓，2-平仓'
     */
    private Integer side;

    /**
     * 每批次操作比例
     */
    private BigDecimal batchSize;

    private BigDecimal doneBuySize;
    private BigDecimal doneSellSize;
    private String symbol;
    /**
     * 下单类型  {@link OrderType}
     */
    private String buyOrderType;
    private String sellOrderType;

    /**
     * 杠杆倍数
     */
    private Integer buyLeverage;
    private Integer sellLeverage;


    /**
     * 机器人Id
     */
    private Long botId;
}
