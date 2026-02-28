package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/13 16:05
 * @Content
 */

@Data
public class TaskBalance {
    BigDecimal longBalance;
    BigDecimal shortBalance;
}
