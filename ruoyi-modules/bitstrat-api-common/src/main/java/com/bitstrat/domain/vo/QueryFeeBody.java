package com.bitstrat.domain.vo;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 16:13
 * @Content
 */

@Data
public class QueryFeeBody {
    String exchange;
    String symbol;
    String coin;

    Long apiId;
}
