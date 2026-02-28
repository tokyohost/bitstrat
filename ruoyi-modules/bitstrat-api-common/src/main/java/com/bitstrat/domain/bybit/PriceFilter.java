package com.bitstrat.domain.bybit;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/11 17:28
 * @Content
 */
@Data
public class PriceFilter {
    private String minPrice;
    private String maxPrice;
    private String tickSize;
}
