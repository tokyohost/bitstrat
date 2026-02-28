package com.bitstrat.domain;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 20:13
 * @Content
 */

@Data
public class Account {

    private String name;
    private Long userId;
    private Long id;
    //bybit api key
    private String apiSecurity;
    //bybit api pwd
    private String apiPwd;

    //okx apikey / bitget apikey / binance apikey
    private String apiKey;

    //okx apiSecret / bitget apiSecret / binance apiSecret
    private String apiSecret;

    //okx passphrase / bitget apikey
    private String passphrase;

    //bitget only
    /**
     * see {@link com.bitget.openapi.common.enums.SignTypeEnum}
     */
    private String signType;

    /**
     * api 类型，模拟盘还是实盘
     */
    private String type;

}
