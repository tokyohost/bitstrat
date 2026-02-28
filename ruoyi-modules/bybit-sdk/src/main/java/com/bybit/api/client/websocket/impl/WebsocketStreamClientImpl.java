package com.bybit.api.client.websocket.impl;

import cn.hutool.core.util.IdUtil;
import com.bybit.api.client.constant.BybitApiConstants;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.httpclient.WebSocketStreamHttpClientSingleton;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.security.HmacSHA256Signer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bybit.api.client.constant.Helper.generateTimestamp;
import static com.bybit.api.client.constant.Helper.generateTransferID;

@Getter
@Slf4j
public class WebsocketStreamClientImpl implements WebsocketStreamClient {
    private static final String THREAD_PING = "thread-ping";
    private static final String THREAD_PRIVATE_AUTH = "thread-private-auth";
    private static final String PING_DATA = "{\"op\":\"ping\"}";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String id;
    private  String clientName = "";

    private volatile boolean isConnected = false;
    private WebSocketMessageCallback webSocketMessageCallback;
    private final WebSocketStreamHttpClientSingleton webSocketHttpClientSingleton;
    private WebSocket webSocket;
    private boolean isAuthenticated = false;
    private final List<Map<String, Object>> messageQueue = new ArrayList<>(); // Queue to hold messages before authentication

    private final String apikey;
    private final String secret;
    private final String baseUrl;
    private final Boolean debugMode;
    private final String logOption;
    private final Integer pingInterval;
    private final String maxAliveTime;
    private List<String> argNames;
    private Map<String,Object> params;
    private String path;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int retryCount = 0;
    private final int maxRetryCount = Integer.MAX_VALUE;
    private final int reconnectIntervalSeconds = 5;

    public WebsocketStreamClientImpl(String apikey, String secret, String baseUrl, Integer pingInterval, String maxAliveTime, Boolean debugMode, String logOption, WebSocketMessageCallback webSocketMessageCallback) {
        this.webSocketMessageCallback = webSocketMessageCallback;
        this.apikey = apikey;
        this.secret = secret;
        this.baseUrl = baseUrl;
        this.pingInterval = pingInterval;
        this.debugMode = debugMode;
        this.logOption = logOption;
        this.maxAliveTime = maxAliveTime;
        webSocketHttpClientSingleton = WebSocketStreamHttpClientSingleton.createInstance(this.debugMode, this.logOption);
        this.id = IdUtil.simpleUUID();
    }

    @Override
    public void setClientName(String clientName, boolean forceChange) {
        if (forceChange) {
            this.clientName = clientName + "["+ this.id+"]";
            return;
        }

        if(StringUtils.isEmpty(this.clientName)) {
            this.clientName = clientName + "["+ this.id+"]";
        }else{
            log.info("socket client name {} cannot changed to {}",this.clientName, clientName);
        }
    }

    public void setClientName(String clientName) {
       this.setClientName(clientName, false);
    }

    private void setupChannelStream(List<String> argNames, String path) {
        this.argNames = new ArrayList<>(argNames);
        this.path = path;
    }

    private void setupChannelStream(Map<String,Object> params, String path) {
        this.params = new HashMap<>(params);
        this.path = path;
    }

    private void sendJsonMessage(WebSocket ws, Object messageObject, String messageType) {
        try {
            String json = objectMapper.writeValueAsString(messageObject);
            ws.send(json);
            log.info("Sent {}: {}", messageType, json);
        } catch (JsonProcessingException e) {
            log.error("Error serializing {} message: ", messageType, e);
        }
    }

    public void sendSubscribeMessage(WebSocket ws, Map<String,Object> params) {
        if (!isAuthenticated) {
            // If not authenticated, queue the message
            log.info("Queueing message until authentication is complete.");
            synchronized (messageQueue) {
                messageQueue.add(params);
            }
            return;
        }
        // Proceed to send the message if already authenticated
        String messageType = "Trade";
        Map<String, Object> subscribeMsg = createApiMessage(params);
        sendJsonMessage(ws, subscribeMsg, messageType);
    }

    public void sendSubscribeMessage(WebSocket ws, List<String> args) {
        String messageType = "Subscribe";
        Map<String, Object> subscribeMsg = createSubscribeMessage(args);
        sendJsonMessage(ws, subscribeMsg, messageType);
    }

    @Override
    public WebSocket close(int code, String reason) {
        WebSocket webSocket = null;
        if(this.webSocket != null) {
            this.webSocket.close(code, reason);
            this.isConnected = false;
            webSocket = this.webSocket;
            this.webSocket = null;
        }
        return webSocket;
    }

    @NotNull
    private Map<String, Object> createSubscribeMessage(List<String> args) {
        Map<String, Object> wsPostMsg = new LinkedHashMap<>();
        wsPostMsg.put("req_id", generateTransferID());
        wsPostMsg.put("op", "subscribe");
        wsPostMsg.put("args", args); // Ensure argNames is correctly formatted for subscription messages
        return wsPostMsg;
    }

    @NotNull
    private Map<String, Object> createApiMessage(Map<String, Object> params) {
        Map<String, Object> wsPostMsg = new LinkedHashMap<>();
        wsPostMsg.put("reqId", params.getOrDefault("reqId", generateTransferID()));
        wsPostMsg.put("header", constructWsAPIHeaders(params));
        wsPostMsg.put("op", "order.create");
        wsPostMsg.put("args", constructWsAPIArgs(params)); // Ensure this is structured correctly
        return wsPostMsg;
    }

    private List<Map<String, Object>> constructWsAPIArgs(Map<String,Object> originalParams) {
        Map<String,Object> params = new HashMap<>(originalParams); // Create a mutable copy
        // Remove specified keys
        params.remove(BybitApiConstants.TIMESTAMP_HEADER);
        params.remove("reqId");
        params.remove(BybitApiConstants.RECV_WINDOW_HEADER);
        params.remove(BybitApiConstants.BROKER_HEADER);
        return Collections.singletonList(params);
    }

    private Map<String, String> constructWsAPIHeaders(Map<String,Object> params) {
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put(BybitApiConstants.TIMESTAMP_HEADER, String.valueOf(generateTimestamp()));
        headerMap.put(BybitApiConstants.RECV_WINDOW_HEADER, params.getOrDefault(BybitApiConstants.RECV_WINDOW_HEADER, BybitApiConstants.DEFAULT_RECEIVING_WINDOW).toString());
        // Check broker referral code
        if(params.containsKey(BybitApiConstants.BROKER_HEADER))
            headerMap.put(BybitApiConstants.BROKER_HEADER, params.get(BybitApiConstants.BROKER_HEADER).toString());
        return headerMap;
    }

    private boolean requiresAuthentication(String path) {
        return BybitApiConfig.V5_TRADE.equals(path) ||
            BybitApiConfig.V5_PRIVATE.equals(path);
/*                BybitApiConfig.V3_CONTRACT_PRIVATE.equals(path) ||
                BybitApiConfig.V3_UNIFIED_PRIVATE.equals(path) ||
                BybitApiConfig.V3_SPOT_PRIVATE.equals(path);*/
    }

    @NotNull
    private Thread createPingThread() {
        return new Thread(() -> {
            try {
                // check if the WebSocket is still valid
                while (this.webSocket != null) {
                    webSocket.send(PING_DATA);
                    log.info("ping data :{},client name {}",PING_DATA,this.clientName);
                    TimeUnit.SECONDS.sleep(pingInterval); // waits for 10 seconds before the next iteration
                }
            } catch (InterruptedException e) {
                log.error("Ping thread was interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    @NotNull
    private Map<String, Object> createAuthMessage() {
        long expires = Instant.now().toEpochMilli() + 10000;
        String val = "GET/realtime" + expires;
        String signature = HmacSHA256Signer.getSignature(val, secret);

        var args = List.of(apikey, expires, signature);
        return Map.of("req_id", generateTransferID(), "op", "auth", "args", args);
    }

    private void sendAuthMessage(WebSocket ws) {
        var authMessage = createAuthMessage();
        sendJsonMessage(ws, authMessage, "Auth");
    }

    @NotNull
    private Thread createAuthThread(WebSocket ws, Runnable afterAuth) {
        return new Thread(() -> {
            try {
                sendAuthMessage(ws);
                if (afterAuth != null) {
                    afterAuth.run();
                }
            } catch (Exception e) {
                log.error(",client name {} Error during authentication: ",clientName, e);
            }
        });
    }

    @NotNull
    private String getWssUrl() {
        Pattern pattern = Pattern.compile("(\\d+)([sm])");
        Matcher matcher = pattern.matcher(maxAliveTime);
        String wssUrl;
        if (matcher.matches()) {
            int timeValue = Integer.parseInt(matcher.group(1));
            String timeUnit = matcher.group(2);
            boolean isTimeValid = isTimeValid(timeUnit, timeValue);

            wssUrl = isTimeValid && requiresAuthentication(path) ? baseUrl + path + "?max_alive_time=" + maxAliveTime : baseUrl + path;
        } else {
            wssUrl = baseUrl + path;
        }
        return wssUrl;
    }

    private boolean isTimeValid(String timeUnit, int timeValue) {
        int minValue = "s".equals(timeUnit) ? 30 : 1;
        int maxValue = "s".equals(timeUnit) ? 600 : 10;
        return timeValue >= minValue && timeValue <= maxValue;
    }

    @NotNull
    private WebSocketListener createWebSocketListener() {
        return new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {

                WebsocketStreamClientImpl.this.onClose(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                WebsocketStreamClientImpl.this.onFailure(t);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                try {
                    WebsocketStreamClientImpl.this.onMessage(text);
                } catch (Exception e) {
//                    WebsocketStreamClientImpl.this.onError(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                WebsocketStreamClientImpl.this.onOpen(webSocket);
            }
        };
    }

    public void setMessageHandler(WebSocketMessageCallback webSocketMessageCallback) {
        this.webSocketMessageCallback = webSocketMessageCallback;
    }

    private void flushMessageQueue() {
        synchronized (messageQueue) {
            for (Map<String, Object> params : messageQueue) {
                sendSubscribeMessage(webSocket, params);
            }
            messageQueue.clear(); // Clear the queue after sending all messages
        }
    }

    @Override
    public void onMessage(String msg) throws JsonProcessingException {
        if (requiresAuthentication(path) && msg.contains("\"op\":\"auth\"")) {
            // Check if authentication was successful
            isAuthenticated = msg.contains("\"retCode\":0");
            boolean success = msg.contains("\"success\":true");
            if (isAuthenticated || success) {
                log.info("Authentication successful. client name {}",clientName);
                flushMessageQueue(); // Send queued messages after successful authentication
            } else {
                log.error("Authentication failed. client name {}",clientName);
            }
        }

        if (webSocketMessageCallback != null) {
            webSocketMessageCallback.onMessage(msg);
        } else {
            log.info(msg);
        }
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        log.error("clientName {} error {}",clientName,t.getMessage());
        this.webSocket.close(4600, t.getMessage());
        this.webSocket = null;
        this.scheduleReconnect();

    }
    public void onFailure(Throwable t) {

        t.printStackTrace();
        log.error("clientName {} error {}",clientName,t.getMessage());
        this.webSocket.close(4500, t.getMessage());
        this.webSocket = null;
        this.scheduleReconnect();
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason) {


        log.info("WebSocket closed. Code: {}, Reason: {} clientName {}", code, reason, clientName);
        isConnected = false;
        ws.close(code, reason);
        this.webSocket = null;
        if(code != 1000) {
            this.scheduleReconnect();
        }
    }

    @Override
    public void onOpen(WebSocket ws) {
        retryCount = 0;
        isConnected = true;
//        scheduler.shutdownNow();
        // If it requires authentication, authenticate first, then subscribe.
        if (requiresAuthentication(path)) {
            Thread authThread = createAuthThread(ws, () -> {
                // After auth, trade api
                if(path.equals(BybitApiConfig.V5_TRADE)){
                    sendSubscribeMessage(ws, params);
                }
                // After auth, send a subscribed message
                else{
                    sendSubscribeMessage(ws, argNames);
                }
            });
            authThread.start();
        } else {
            // If no authentication is needed, just send the subscribed message.
            sendSubscribeMessage(ws, argNames);
        }

        log.warn("client name {} 连接成功",clientName);
    }

    @Override
    public WebSocket connect() {
        String wssUrl = getWssUrl();
        log.info(wssUrl);

        if (webSocket != null) {
            log.info("WebSocket connected successfully. callback exists socket. clientName {}",clientName);
            this.params = new HashMap<>();
            return webSocket;
        }
        this.webSocket = webSocketHttpClientSingleton.createWebSocket(wssUrl, createWebSocketListener());

        // Start the ping thread immediately.
        Thread pingThread = createPingThread();
        pingThread.setName(THREAD_PING); // Default to public ping name
        pingThread.start();
        return this.webSocket;
    }

    @Override
    public WebSocket getPublicChannelStream(List<String> argNames, String path) {
        setupChannelStream(argNames, path);
        return connect();
    }

    @Override
    public WebSocket getPrivateChannelStream(List<String> argNames, String path) {
        setupChannelStream(argNames, path);
        return connect();
    }

    @Override
    public WebSocket getTradeChannelStream(Map<String,Object> params, String path) {
        setupChannelStream(params, path);
        return connect();
    }

    private void scheduleReconnect() {

        if (retryCount >= maxRetryCount) {
            log.error("已达到最大重试次数，停止重连");
            return;
        }

        retryCount++;
        int delay = reconnectIntervalSeconds; // 指数退避可用 delay *= 2;
        log.info("准备在 " + delay + " 秒后重连...（第 " + retryCount + " 次）");

        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }
}
