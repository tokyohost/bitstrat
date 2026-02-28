package com.bitstrat.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/3 16:05
 * @Content
 */
@Data
public class PositionVo {

    //当前满足条件
    String currRole;
    //buy / sell
    String type;

    /**
     * 市场价
     */
    BigDecimal marketPrice;

    BigDecimal avgPrice;
}
