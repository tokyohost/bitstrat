package com.bitstrat.domain.okx;

import com.bitstrat.domain.Account;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/18 17:31
 * @Content
 */

@Data
public class OkxAccount extends Account {
    //okx apikey
    private String apiKey;

    //okx apiSecret
    private String apiSecret;

    //okx passphrase
    private String passphrase;
}
