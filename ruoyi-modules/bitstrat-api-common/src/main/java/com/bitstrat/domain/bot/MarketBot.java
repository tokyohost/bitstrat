package com.bitstrat.domain.bot;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:59
 * @Content
 */

@Data
public class MarketBot {
    //交易所
    private String exchange;

    //合约币对
    private String symbol;

    //杠杆
    private String leverage;

    //机器人id
    private Long botId;
}
