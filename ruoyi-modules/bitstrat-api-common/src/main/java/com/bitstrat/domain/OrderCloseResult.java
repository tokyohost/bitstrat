package com.bitstrat.domain;

import com.bitstrat.constant.CrossOrderStatus;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/6 20:43
 * @Content
 */
@Data
public class OrderCloseResult {
    /**
     * see {@link CrossOrderStatus}
     */
    String status;
    String msg;

    String body;
}
