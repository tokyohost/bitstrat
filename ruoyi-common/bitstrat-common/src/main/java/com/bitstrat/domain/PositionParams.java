package com.bitstrat.domain;

import lombok.Data;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/22 16:57
 * @Content
 */

@Data
public class PositionParams {
    String ex;
    String posId;

    List<String> closeOrderIds;

    Long startTime;
    Long endTime;

}
