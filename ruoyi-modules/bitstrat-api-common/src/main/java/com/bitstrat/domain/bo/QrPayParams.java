package com.bitstrat.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.AliPayGroup;
import org.dromara.common.core.validate.StripePayGroup;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/3 19:29
 * @Content
 */

@Data
public class QrPayParams {
    private CoinsBalanceLogBo coinsBalanceLogBo;

    @NotNull(message = "请选择支付方式",groups = {AliPayGroup.class,StripePayGroup.class})
    @Pattern(regexp = "^(?i)(alipay|wechat|bank|stripe)$", message = "支付方式不合法",groups = {AliPayGroup.class,StripePayGroup.class})
    private String payType;
    @DecimalMin(value = "0.01", message = "支付金额必须大于等于0.01",groups = AliPayGroup.class)
    @DecimalMax(value = "100000", message = "支付金额必须小于10000",groups = AliPayGroup.class)
    private BigDecimal payAmount;

    /**
     * stripe 支付选择的支付金额
     */
    @NotNull(message = "{stripe.payIdMostBeSelect}",groups = StripePayGroup.class)
    private String priceId;

    private Long userId;



}
