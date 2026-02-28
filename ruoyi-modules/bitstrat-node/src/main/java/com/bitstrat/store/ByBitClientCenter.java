package com.bitstrat.store;

import com.bitstrat.bybit.ByBitReconnectSocketClient;
import com.bitstrat.handler.ByBitSocketOrderHandler;
import com.bybit.api.client.log.LogOption;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.bybit.api.client.config.BybitApiConfig.V5_TRADE;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:17
 * @Content
 */

@Component
@Slf4j
public class ByBitClientCenter {
    ConcurrentHashMap<String, BybitApiClientFactory> clientFactory = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, WebsocketStreamClient> streamClientHold = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, WebSocket> connectClientHold = new ConcurrentHashMap<>();

    @Autowired
    ByBitSocketOrderHandler byBitSocketClientHandler;


    public synchronized BybitApiClientFactory getBybitApiClientFactory(String apiKey,String apiSecret,String domain,boolean forceCreate) {
        //BybitApiConfig.STREAM_MAINNET_DOMAIN
        String key = getKey(apiKey, apiSecret, domain);
        if (clientFactory.containsKey(key) && forceCreate == false) {
            return clientFactory.get(key);
        }
        BybitApiClientFactory bybitApiClientFactory = BybitApiClientFactory.newInstance(apiKey,apiSecret,domain, true);

        clientFactory.put(key, bybitApiClientFactory);
        return bybitApiClientFactory;
    }
    public synchronized WebsocketStreamClient getBybitApiSocketStream(String apiKey,String apiSecret,String domain,boolean forceCreate) {
        //BybitApiConfig.STREAM_MAINNET_DOMAIN
        String key = getKey(apiKey, apiSecret, domain);
        if (streamClientHold.containsKey(key) && forceCreate == false) {
            return streamClientHold.get(key);
        }
//        BybitApiClientFactory bybitApiClientFactory = getBybitApiClientFactory(apiKey, apiSecret, domain, false);
        ByBitReconnectSocketClient  reconnectClient
            = new ByBitReconnectSocketClient(apiKey,apiSecret,domain,20,"-1",true,LogOption.SLF4J.getLogOptionType(),byBitSocketClientHandler);
//        WebsocketStreamClient websocketStreamClient = bybitApiClientFactory.newWebsocketClient(byBitSocketClientHandler);
        streamClientHold.put(key, reconnectClient);
        return reconnectClient;
    }

    private HashMap<String, Object> getPingData() {
        HashMap<String, Object> pingData = new HashMap<>();
        pingData.put("req_id", "100001");
        pingData.put("op", "ping");
        return pingData;
    }

    private String getKey(String apiKey,String apiSecret,String domain) {
        return apiKey + ":" + apiSecret + ":" + domain;
    }
    @PreDestroy
    public void destroy() {
        log.info("正在关闭用户长连接...");
        for (String key : connectClientHold.keySet()) {
            WebSocket remove = connectClientHold.remove(key);
            boolean closed = remove.close(1000, "Normal Closure");
            if (closed) {
                log.info("已关闭 {}",key);
            } else {
                log.info("WebSocket 已经关闭或者关闭失败 {}",key);
            }
        }
        log.info("已关闭所有长连接...");



    }
}
