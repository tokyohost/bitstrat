package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsLossPoint;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 滑点管理业务对象 coins_loss_point
 *
 * @author Lion Li
 * @date 2025-04-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsLossPoint.class, reverseConvertGenerate = false)
public class CoinsLossPointBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
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
