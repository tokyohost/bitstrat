package com.bitstrat.domain.bo;

import com.bitstrat.constant.OrderAmendStatus;
import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.CoinsOrder;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 订单列表业务对象 coins_order
 *
 * @author Lion Li
 * @date 2025-04-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsOrder.class, reverseConvertGenerate = false)
public class CoinsOrderBo extends BaseEntity {

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
     * 订单id
     */
    private String orderId;

    /**
     * 交易所名称
     */
    private String ex;

    /**
     * 币对
     */
    private String symbol;

    /**
     * 数量
     */
    private String size;

    /**
     * 状态
     */
    private String status;

    /**
     * 手续费
     */
    private String fee;

    /**
     * 平均价格
     */
    private String avgPrice;

    /**
     * 下单价格
     */
    private String price;

    private String side;

    /**
     * 订单是否已经是终结态  1是 0否
     */
    private Integer orderEnd;


    /**
     * 剩余未成交的数量
     */
    BigDecimal leavesQty;
    /**
     * 剩余未成交的价值
     */
    BigDecimal leavesValue;
    /**
     * 累计已成交的价值
     */
    BigDecimal cumExecValue;
    /**
     * 累计已成交的数量
     */
    BigDecimal cumExecQty;

    Long createBy;


    /**
     * 市价修改 {@link OrderAmendStatus}
     */
    Long marketPriceAmend;

    /**
     * 是否是平仓单 1-是 0-不是
     */
    Long closePositionOrder;

    /**
     * 订单类型 {@link OrderType}
     */
    String orderType;

    Long batchId;

    Integer batchCount;

    /**
     * 每笔平仓盈亏
     */
    BigDecimal pnl;

    Long accountId;

    String abTaskId;
}
