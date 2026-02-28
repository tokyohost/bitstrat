package com.bitstrat.domain.bybit;

import com.bitstrat.domain.Account;
import lombok.Data;

@Data
public class ByBitAccount extends Account {
    private String apiSecurity;
    private String apiPwd;
}
