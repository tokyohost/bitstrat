package com.bitstrat.config;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:36
 * @Content
 */

@Data
public class ExchangeConfig {
    private String ex;           // 交易所名称，"bybit"、"okx"
    private String privateUrl;
    private String publicSpot;
    private String publicUrl;
    private String publicLinear;
    private String trade;

    //模拟盘
    private String papPublicUrl;
    private String papPrivateUrl;
}
