package com.bitstrat.bybit;

import com.bybit.api.client.constant.Helper;
import com.bybit.api.client.security.HmacSHA256Signer;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.impl.WebsocketStreamClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class ByBitReconnectSocketClient extends WebsocketStreamClientImpl {
    public ByBitReconnectSocketClient(String apikey, String secret, String baseUrl, Integer pingInterval, String maxAliveTime, Boolean debugMode, String logOption, WebSocketMessageCallback webSocketMessageCallback) {
        super(apikey, secret, baseUrl, pingInterval, maxAliveTime, debugMode, logOption, webSocketMessageCallback);
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason) {
        super.onClose(ws, code, reason);

    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        log.error(t.getMessage());
    }



}
