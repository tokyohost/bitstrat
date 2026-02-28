package com.bitstrat.wsClients;

import com.bitstrat.config.wsClient.ConnectionOtherConfig;
import com.bitstrat.domain.Account;
import com.bitstrat.event.ChannelSend;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.handler.WebSocketReconnectHandler;
import com.bitstrat.wsClients.strategy.MessageSendCenter;
import com.bitstrat.wsClients.strategy.MessageSendStrategy;
import com.bitstrat.wsClients.utils.WsCheck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import rx.functions.Action;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.bitstrat.wsClients.constant.SocketConstant.RECONNECT_HANDLER_ATTRIBUTE_KEY;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:11
 * @Content
 */

@ChannelHandler.Sharable
@Component
@Slf4j
public class ExchangeConnectionManager {
    @Value("${ws.enableLog:false}")
    private String enablePrintConnectStatus;
    @Autowired
    MessageSendCenter messageSendCenter;

    @Autowired
    WsClusterManager wsClusterManager;

    private final NioEventLoopGroup eventLoopGroup;

    public NioEventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    // 存储每个用户在每个交易所的 WebSocket 连接
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Channel>> userChannels;

    public ExchangeConnectionManager() {
        // 初始化EventLoopGroup和Bootstrap
        eventLoopGroup = new NioEventLoopGroup();

//            .handler(new WebSocketChannelInitializer());

        // 用于存储每个用户在每个交易所的连接
        userChannels = new ConcurrentHashMap<>();

        /**
         * 每隔10s 统计当前连接状态
         */
        if("true".equalsIgnoreCase(enablePrintConnectStatus)){
            eventLoopGroup.scheduleAtFixedRate(() -> {
                printConnectStatus();
            }, 10, 10, TimeUnit.SECONDS);
        }

    }

    public void printConnectStatus() {
        try {
            int totalUsers = userChannels.size();
            int totalConnections = 0;

            StringBuilder sb = new StringBuilder();
            sb.append("\n---- WebSocket 客户端连接状态统计 ----\n");
            sb.append("总用户数: ").append(totalUsers).append("\n");

            for (Map.Entry<String, ConcurrentHashMap<String, Channel>> userEntry : userChannels.entrySet()) {
                String userId = userEntry.getKey();
                ConcurrentHashMap<String, Channel> exchangeChannels = userEntry.getValue();
                sb.append("  用户ID: ").append(userId).append("\n");

                for (Map.Entry<String, Channel> exchangeEntry : exchangeChannels.entrySet()) {
                    String exchangeName = exchangeEntry.getKey();
                    Channel channel = exchangeEntry.getValue();
                    String symbol = "-";
                    ConnectionConfig connectionConfig = channel.attr(SocketConstant.connectionConfig).get();
                    if(connectionConfig.getOtherConfig() != null){
                        symbol = connectionConfig.getOtherConfig().getSymbol();
                    }

                    boolean isActive = channel.isActive();
                    sb.append("    交易所: ").append(exchangeName)
                        .append("    API-ID: ").append(connectionConfig.getAccount().getId())
                        .append("，连接状态: ").append(isActive ? "活跃" : "断开")
                        .append("，symbol: ").append(symbol).append("\n");
                    totalConnections++;
                }
            }

            sb.append("总连接数: ").append(totalConnections).append("\n");
            sb.append("----------------------------\n");

            log.info(sb.toString());
        } catch (Exception e) {
            log.error("统计连接状态时发生异常", e);
        }
    }

    // 获取用户在指定交易所的WebSocket Channel
    public Channel getChannel(String userId,Long apiId, String exchange,String websocketType) {
        ConcurrentHashMap<String, Channel> exchangeChannels = userChannels.get(userId);
        if (exchangeChannels != null) {
            return exchangeChannels.get(exchange+":"+websocketType+":"+apiId);
        }
        return null;
    }
    // 获取用户在指定交易所所有的存活WebSocket Channel
    public List<Channel> getChannel(String userId, String exchange, String websocketType) {
        ConcurrentHashMap<String, Channel> exchangeChannels = userChannels.get(userId);
        List<Channel> result = new ArrayList<>();
        if (exchangeChannels != null) {
            ArrayList<Channel> channels = new ArrayList<>(exchangeChannels.values());
            for (Channel channel : channels) {
                ConnectionConfig connectionConfig = channel.attr(SocketConstant.connectionConfig).get();
                if (Objects.nonNull(connectionConfig) && connectionConfig.getExchange().equalsIgnoreCase(exchange)
                && connectionConfig.getWebsocketType().equalsIgnoreCase(websocketType)) {
                    result.add(channel);
                }
            }
        }
        return result;
    }
    public void sendMessage(String userId,Long apiId, String exchange,String websocketType, String message) {
        exchange = exchange.toLowerCase();
        ConcurrentHashMap<String, Channel> exchangeChannels = userChannels.get(userId);
        if (exchangeChannels != null) {
            Channel channel = exchangeChannels.get(exchange+":"+websocketType+":"+apiId);
            if (channel != null) {
                MessageSendStrategy strategy = messageSendCenter.getStrategy(exchange);
                if (Objects.isNull(strategy)) {
                    throw new RuntimeException("unsupported messageSendCenter impl exchange: " + exchange);
                }
                strategy.sendMessage(userId,exchange,message,channel);
            }else{
                log.error("发送异常，无活动连接");
            }
        }else{
            log.error("发送异常，无活动连接");
        }
    }
    @Deprecated
    public void sendSubscriptMessage(String userId,Long apiId, String exchange,String websocketType, String message) {
        this.sendSubscriptMessage(userId,apiId,exchange,websocketType,message,null,null);
    }
    public void sendSubscriptMessage(String userId,Long apiId, String exchange,String websocketType, String message,String subscriptType,Channel newerChannel) {
        exchange = exchange.toLowerCase();
        if (StringUtils.isEmpty(message)) {
            log.warn("用户{} {} 订阅为空，不处理",userId,exchange);
            return;
        }
        if (Objects.nonNull(newerChannel)) {
            if (newerChannel.isActive()) {
                MessageSendStrategy strategy = messageSendCenter.getStrategy(exchange);
                if (Objects.isNull(strategy)) {
                    throw new RuntimeException("unsupported messageSendCenter impl exchange: " + exchange);
                }
                strategy.sendSubscriptMessage(userId,exchange,message,newerChannel,subscriptType);
            }else{
                log.error("发送异常，无活动连接");
            }
            return;
        }


        ConcurrentHashMap<String, Channel> exchangeChannels = userChannels.get(userId);
        if (exchangeChannels != null) {
            Channel channel = exchangeChannels.get(exchange+":"+websocketType+":"+apiId);
            if (channel != null && channel.isActive()) {
                MessageSendStrategy strategy = messageSendCenter.getStrategy(exchange);
                if (Objects.isNull(strategy)) {
                    throw new RuntimeException("unsupported messageSendCenter impl exchange: " + exchange);
                }
                strategy.sendSubscriptMessage(userId,exchange,message,channel,subscriptType);
            }else{
                log.error("发送异常，无活动连接");
            }
        }else{
            log.error("发送异常，无活动连接");
        }
    }

    // 创建并连接用户的WebSocket连接
    public void createConnection(String userId, String exchange,String websocketType, URI uri) throws Exception {
        this.createConnection(null,userId,exchange,websocketType,uri);
    }
    public void createConnection(Account account,String userId, String exchange,String websocketType, URI uri) throws Exception {
        this.createConnection(account,userId,exchange,websocketType,uri,null,false,null);
    }
    public void createConnection(Account account,String userId, String exchange,String websocketType, URI uri,Channel bridgChannel,boolean forceCreate,ChannelSend sendSubmsg) throws Exception {
        this.createConnection(account,userId,exchange,websocketType,uri,bridgChannel,null,forceCreate,sendSubmsg);
    }
    public void createConnection(Account account, String userId, String exchange, String websocketType, URI uri, Channel bridgChannel, ConnectionOtherConfig config, ChannelSend sendSubmsg) throws Exception {
        this.createConnection(account,userId,exchange,websocketType,uri,bridgChannel,config,false,sendSubmsg);
    }
    public void createConnection(Account account, String userId, String exchange, String websocketType, URI uri, Channel bridgChannel, ConnectionOtherConfig config, boolean forceCreate
    , ChannelSend sendSubmsg) throws Exception {
        exchange = exchange.toLowerCase();
        Channel channel = getChannel(userId,account.getId(), exchange,websocketType);
        if (channel != null && channel.isActive()) {
            log.info("Connection already exists for user " + userId + " on exchange " + exchange +" api Id "+account.getId());
            wsClusterManager.userOnline(userId,exchange+":"+websocketType+":"+account.getId());
            if (forceCreate) {
                closeConnection(userId,account.getId(),exchange,websocketType);
            }else{
                return;
            }
        }
        boolean online = wsClusterManager.isOnline(userId, exchange + ":" + websocketType + ":" + account.getId());
        if(online){
            String nodeId = wsClusterManager.getUserNode(userId, exchange + ":" + websocketType + ":" + account.getId());
            if(wsClusterManager.getNodeId().equalsIgnoreCase(nodeId)){
                //就是本机，但是Channel 里并没有，则表示redis 是之前未删的key
                log.info("Connection already exists for user " + userId + " on exchange " + exchange +" api Id "+account.getId()+" in RedisCluster Node "+nodeId +" is current" +
                    "node but user channel not exists");
                wsClusterManager.userOffline(userId,exchange+":"+websocketType+":"+account.getId());
            }else{
                log.info("Connection already exists for user " + userId + " on exchange " + exchange +" api Id "+account.getId()+" in RedisCluster Node "+nodeId);
                return;
            }

        }

        CountDownLatch awaitConnectDone = new CountDownLatch(1);
        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setOtherConfig(config);
        connectionConfig.setSourceUri(uri);
        uri = WsCheck.preAuthWebsocket(account, exchange, websocketType, uri);
        connectionConfig.setUri(uri);
        connectionConfig.setBridgChannel(bridgChannel);
        connectionConfig.setAwaitConnectDone(awaitConnectDone);
        connectionConfig.setExchange(exchange);
        connectionConfig.setLoopGroup(eventLoopGroup);
        connectionConfig.setWebsocketType(websocketType);
        connectionConfig.setUserId(userId);
        connectionConfig.setAccount(account);
        connectionConfig.setSubscriptCallback(sendSubmsg);
        connectionConfig.setExchangeConnectionManager(this);
        if (Objects.nonNull(config)) {
            config.setConnectionConfig(connectionConfig);
        }
        // 创建连接并加入到Channel管理器
        Bootstrap bootstrap = new Bootstrap()
            .group(eventLoopGroup)
            .channel(NioSocketChannel.class).handler(new WebSocketChannelInitializer(connectionConfig));


        int port = uri.getPort();
        if (uri.getPort() < 0) {
            if(uri.getScheme().equals("ws")) {
                port = 80;
            }else if(uri.getScheme().equals("wss")){
                port = 443;
            }else {
                throw new Exception("Invalid URI port");
            }
        }

        ChannelFuture future = bootstrap.connect(uri.getHost(),port).sync();
        if (!future.isSuccess()) {
            Throwable cause = future.cause();
            log.error("Connect failed: ", cause);
        }
        //等待连接30s 防止死锁
        awaitConnectDone.await(30,TimeUnit.SECONDS);
        addChannelToManager(userId,account.getId(), exchange,websocketType, future.channel());
        sendSubmsg.handle(future.channel());
        log.info("create ok for user " + userId + " on exchange " + exchange+" websocketType "+websocketType);
    }



    // 将Channel存入管理器
    public void addChannelToManager(String userId,Long apiId, String exchange,String websocketType, Channel channel) {
        userChannels.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(exchange+":"+websocketType+":"+apiId, channel);
        wsClusterManager.userOnline(userId,exchange+":"+websocketType+":"+apiId);
    }

    // 关闭指定用户和交易所的连接
    public void closeConnection(String userId, String exchange) {
        ConcurrentHashMap<String, Channel> exchangeChannels = userChannels.get(userId);
        if (exchangeChannels != null) {
            // 使用迭代器遍历并安全地移除元素
            Iterator<Map.Entry<String, Channel>> iterator = exchangeChannels.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Channel> entry = iterator.next();
                String s = entry.getKey();
                if (s.startsWith(exchange)) {
                    Channel channel = entry.getValue();
                    if (channel != null) {
                        WebSocketReconnectHandler reconnectHandler = channel.attr(RECONNECT_HANDLER_ATTRIBUTE_KEY).get();
                        if(reconnectHandler != null) {
                            reconnectHandler.close(channel);
                        }else{
                            channel.close();
                        }
                        iterator.remove();  // 安全移除
                    }
                }
            }
        }
    }
    public void closeConnection(String userId,Long apiId, String exchange,String websocketType) {
        ConcurrentHashMap<String, Channel> exchangeChannels = userChannels.get(userId);
        if (exchangeChannels != null) {
            Channel channel = exchangeChannels.get(exchange+":"+websocketType+":"+apiId);
            if (channel != null) {
                WebSocketReconnectHandler reconnectHandler = channel.attr(RECONNECT_HANDLER_ATTRIBUTE_KEY).get();
                if(reconnectHandler != null) {
                    reconnectHandler.close(channel);
                    log.info("关闭socket 成功，停止自动重连成功 userId {} on exchange {} websocketType {}", userId, exchange, websocketType);
                }else{
                    channel.close();
                    log.info("关闭socket 成功 userId {} on exchange {} websocketType {}", userId, exchange, websocketType);
                }
                exchangeChannels.remove(exchange+":"+websocketType+":"+apiId);
                wsClusterManager.userOffline(userId,exchange+":"+websocketType+":"+apiId);
            }
        }
    }

    // 关闭所有连接
    public void closeAll() {
        userChannels.values().forEach(exchangeChannels ->
            exchangeChannels.values().forEach(channel -> {
                WebSocketReconnectHandler reconnectHandler = channel.attr(RECONNECT_HANDLER_ATTRIBUTE_KEY).get();
                if(reconnectHandler != null) {
                    reconnectHandler.close(channel);
                }else{
                    channel.close();
                }
            })
        );
        userChannels.clear();
    }

    // 停止EventLoopGroup
    @EventListener(ContextClosedEvent.class)
    public void stop() {
        eventLoopGroup.shutdownGracefully();
        //关闭所有
    }

    public WsClusterManager getWsClusterManager() {
        return wsClusterManager;
    }
}
