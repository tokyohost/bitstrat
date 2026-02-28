package com.bitstrat.event;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/2 9:13
 * @Content
 */
@Data
public class RedisKeyExpireEvent {

    private String key;
    private Long expire;
    private Long ttl;
}
