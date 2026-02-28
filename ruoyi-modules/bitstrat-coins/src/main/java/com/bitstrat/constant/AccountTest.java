package com.bitstrat.constant;

import com.bitstrat.domain.Account;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/28 16:44
 * @Content
 */

public class AccountTest {

    public static Account getBitgetTestAccount(){
        Account account = new Account();
        account.setId(1L);
        account.setApiKey("bg_915f9bab251093bb61bcfff6fb8da268");
        account.setApiSecret("6fb7393ce4c4b983fddea971ef31b91dff4fcb9a16ef815620bdf7e225971c1e");
        account.setPassphrase("Iloveyou520");
        return account;
    }
}
