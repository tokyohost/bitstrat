package com.bitstrat.domain;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 19:47
 * @Content
 */

@Data
public class ExchangeData {
    private String exchangeName;
    private String nodeName;
    private String clientId;
    private Long delay;
    private String ip;
    private String status;
    private Long currRoleSize;
    private Long maxRoleSize;

}
