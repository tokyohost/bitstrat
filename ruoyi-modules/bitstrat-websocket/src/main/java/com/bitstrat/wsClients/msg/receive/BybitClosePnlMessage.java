package com.bitstrat.wsClients.msg.receive;

import com.bitstrat.domain.Account;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/14 10:25
 * @Content  bybit 平仓盈亏信息！
 */

@Data
public class BybitClosePnlMessage {
    private String orderId;
    private BigDecimal pnlAmount;
    private Account account;

}
