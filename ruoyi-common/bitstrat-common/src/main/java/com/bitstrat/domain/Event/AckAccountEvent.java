package com.bitstrat.domain.Event;

import com.bitstrat.domain.Account;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AckAccountEvent {
    Account account;
    Long accountId;
    BigDecimal balance;
    BigDecimal freeBalance;

}
