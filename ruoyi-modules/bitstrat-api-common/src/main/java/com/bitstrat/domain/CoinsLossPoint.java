package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 滑点管理对象 coins_loss_point
 *
 * @author Lion Li
 * @date 2025-04-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_loss_point")
public class CoinsLossPoint extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 交易所名称
     */
    private String exchangeName;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 回撤率
     */
    private BigDecimal retread;

    /**
     * 下单数量
     */
    private BigDecimal quantity;

    private BigDecimal triggerPrice1;
    private BigDecimal triggerPrice2;

    private String nodeClientId;

    private Integer enable;
    private Long createBy;

    private BigDecimal stopLossCalcLimit;



}
