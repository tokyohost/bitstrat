package com.bitstrat.domain.msg;

import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.server.ByBitMessageData;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 18:12
 * @Content 监听实时行情数据
 */

public class SubscribeOrder extends ByBitMessageData {
    ByBitAccount account;

    public ByBitAccount getAccount() {
        return account;
    }

    public void setAccount(ByBitAccount account) {
        this.account = account;
    }
}
