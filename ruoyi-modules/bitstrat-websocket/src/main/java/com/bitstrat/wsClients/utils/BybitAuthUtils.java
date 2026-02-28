package com.bitstrat.wsClients.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;


/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 16:35
 * @Content
 */

public class BybitAuthUtils {
    private String generateSignature(long timestamp,String API_KEY,String API_SECRET) {
        String paramStr = "api_key=" + API_KEY + "&timestamp=" + timestamp + "&api_secret=" + API_SECRET;
        return HMACSHA256(paramStr);
    }

    private String HMACSHA256(String data) {
        // Implement HMAC-SHA256 signing here
        // Refer to Bybit API documentation for signing method
//        String signature = sign(apiKey, secret, StringUtils.isEmpty(payload) ? ""  : payload, String.valueOf(timestamp), String.valueOf(recvWindow));
        return "";
    }

    public static String generateTransferID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
