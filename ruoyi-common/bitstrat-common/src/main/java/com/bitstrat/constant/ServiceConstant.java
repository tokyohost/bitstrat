package com.bitstrat.constant;

import io.netty.util.AttributeKey;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 14:41
 * @Content
 */

public class ServiceConstant {
    public static final AttributeKey<String> CLIENT_ID_ATTR = AttributeKey.valueOf("clientId");
    public static final AttributeKey<String> EXCHANGE_ATTR = AttributeKey.valueOf("exchangeName");

    public static final String SERVICE_EXCHANGE_NAME = "serviceExchangeName";
}
