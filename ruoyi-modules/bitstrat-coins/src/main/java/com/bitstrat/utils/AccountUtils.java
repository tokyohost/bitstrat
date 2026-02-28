package com.bitstrat.utils;

import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.vo.CoinsApiVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.core.exception.coins.api.ApiNotFindException;

import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/17 15:04
 * @Content
 */


public class AccountUtils {

    public static Account coverToAccount(CoinsApi api) {
        if (Objects.isNull(api)) {
            throw new ApiNotFindException();
        }
        Account account = new Account();
        account.setId(api.getId());
        account.setUserId(api.getUserId());
        account.setName(api.getName());
        if (api.getExchangeName().equalsIgnoreCase(ExchangeType.BYBIT.getName())) {
            //bybit account
            account.setApiSecurity(api.getApiKey());
            account.setApiPwd(api.getApiSecurity());
            return account;
        }else if(api.getExchangeName().equalsIgnoreCase(ExchangeType.OKX.getName())){
            // OKX account
            account.setApiKey(api.getApiKey());
            account.setApiSecret(api.getApiSecurity());
            account.setPassphrase(api.getPassphrase());
            return account;

        }else if(api.getExchangeName().equalsIgnoreCase(ExchangeType.BINANCE.getName())){
            // BINANCE account
            account.setApiKey(api.getApiKey());
            account.setApiSecret(api.getApiSecurity());
            return account;

        }else if(api.getExchangeName().equalsIgnoreCase(ExchangeType.BITGET.getName())){
            // BITGET account
            account.setApiKey(api.getApiKey());
            account.setApiSecret(api.getApiSecurity());
            account.setPassphrase(api.getPassphrase());
            return account;

        }

        return account;
    }
    public static Account coverToAccount(CoinsApiVo api) {
        Account account = new Account();
        account.setId(api.getId());
        account.setUserId(api.getUserId());
        account.setName(api.getName());
        account.setType(api.getType());
        if (api.getExchangeName().equalsIgnoreCase(ExchangeType.BYBIT.getName())) {
            //bybit account
            account.setApiSecurity(StringUtils.trim(api.getApiKey()));
            account.setApiPwd(StringUtils.trim(api.getApiSecurity()));
            return account;
        }else if(api.getExchangeName().equalsIgnoreCase(ExchangeType.OKX.getName())){
            // other account
            account.setApiKey(StringUtils.trim(api.getApiKey()));
            account.setApiSecret(StringUtils.trim(api.getApiSecurity()));
            account.setPassphrase(StringUtils.trim(api.getPassphrase()));
            return account;

        }else if(api.getExchangeName().equalsIgnoreCase(ExchangeType.BINANCE.getName())){
            // BINANCE account
            account.setApiKey(api.getApiKey());
            account.setApiSecret(api.getApiSecurity());
            return account;

        }else if(api.getExchangeName().equalsIgnoreCase(ExchangeType.BITGET.getName())){
            // other account
            account.setApiKey(StringUtils.trim(api.getApiKey()));
            account.setApiSecret(StringUtils.trim(api.getApiSecurity()));
            account.setPassphrase(StringUtils.trim(api.getPassphrase()));
            return account;

        }

        return account;
    }

}
