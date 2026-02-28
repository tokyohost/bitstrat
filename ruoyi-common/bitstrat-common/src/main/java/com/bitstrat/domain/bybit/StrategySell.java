package com.bitstrat.domain.bybit;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 17:21
 * @Content
 */

@Data
public class StrategySell {
    //卖出条件,示例：MA7<MA21<MA63<MA189
    String role;
    //单位：可选percentage(百分比)/quantity(具体数量)
    String unit;
    //卖出数量，示例：1
    String limit;

}
