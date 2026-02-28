package com.bitstrat.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/23 19:02
 * @Content
 */

@Data
public class QueryProfitParam {
    @NotNull(message = "taskId not be empty")
    private Long taskId;
    private Long apiId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long startTimeStamp;
    private Long endTimeStamp;
}
