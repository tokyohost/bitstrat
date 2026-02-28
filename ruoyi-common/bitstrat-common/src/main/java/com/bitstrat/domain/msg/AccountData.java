package com.bitstrat.domain.msg;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.server.MessageData;
import lombok.Data;

@Data
public class AccountData implements MessageData {
    private String exchangeName;
    private Account account;
}
