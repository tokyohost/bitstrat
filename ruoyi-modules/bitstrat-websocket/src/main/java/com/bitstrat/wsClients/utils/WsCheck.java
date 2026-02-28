package com.bitstrat.wsClients.utils;

import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.Account;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.handler.binance.BinanceAuthUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/22 18:58
 * @Content
 */

@Slf4j
public class WsCheck {
    /**
     * 针对不在websocket 内进行鉴权登录而是url 链接带有鉴权的交易所进行提前处理
     * @param account
     * @param exchange
     * @param websocketType
     * @param uri
     * @return
     */
    @SneakyThrows
    public static URI preAuthWebsocket(Account account, String exchange, String websocketType, URI uri) {
        if(ExchangeType.BINANCE.getName().equalsIgnoreCase(exchange)) {

            if(WebSocketType.PRIVATE.equalsIgnoreCase(websocketType)) {
                //币安是url鉴权
                String url = uri.toString();
                String uMstreamUrl = BinanceAuthUtils.getUMstreamUrl(account);
                if(url.endsWith("/")){
                    URI enUri = new URI(url + uMstreamUrl);
                    log.info("币安 私有websocket 链接:{}" ,enUri.toString());
                    return enUri;
                }else{
                    URI enUri = new URI(url +"/"+ uMstreamUrl);
                    log.info("币安 私有websocket 链接:{}" ,enUri.toString());
                    return enUri;
                }

            }

        }

        return uri;
    }
}
