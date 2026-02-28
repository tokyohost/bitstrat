package com.bitstrat.wsClients.handler.binance;
import com.alibaba.fastjson2.JSONObject;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.bitstrat.domain.Account;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/22 17:15
 * @Content
 */

@Slf4j
public class BinanceAuthUtils {

    /**
     * 币安比较特殊，鉴权是通过websocket url 的后缀去实现鉴权
     * @param account
     * @return
     */
    public static String getUMstreamUrl(Account account) {
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String result = client.userData().createListenKey();
        log.info("币安 userid {} 获取U本位合约websocket listenKey:{}",account.getUserId(), result);
        return JSONObject.parseObject(result).getString("listenKey");
    }
    /**
     * 币安比较特殊，鉴权是通过websocket url 的后缀去实现鉴权
     * @param account
     * @return
     */
    public static void closeUMstreamUrl(Account account) {
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String result = client.userData().closeListenKey();
        log.info("币安 userid {} 关闭U本位合约websocket closeUMstreamUrl:{}",account.getUserId(), result);
    }
    /**
     * 币安比较特殊，鉴权是通过websocket url 的后缀去实现鉴权
     * @param account
     * @return
     */
    public static void extendUMstreamUrl(Account account) {
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String result = client.userData().extendListenKey();
        log.info("币安 userid {} 续约U本位合约websocket closeUMstreamUrl:{}",account.getUserId(), result);
    }

}
