package com.bitstrat.domain.server;

import com.bitstrat.domain.bybit.ByBitAccount;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:26
 * @Content
 */

public class ByBitMessageData implements MessageData{
    ByBitAccount byBitAccount;

    public ByBitAccount getByBitAccount() {
        return byBitAccount;
    }

    public void setByBitAccount(ByBitAccount byBitAccount) {
        this.byBitAccount = byBitAccount;
    }
}
