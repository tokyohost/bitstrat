package com.bitstrat.domain.bybit;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/11 17:28
 * @Content
 */
@Data
public class LotSizeFilter {
    private String qtyStep;
    private String postOnlyMaxOrderQty;
    private String minNotionalValue;
    private String maxOrderQty;
    private String minOrderQty;
    private String maxMktOrderQty;
}
