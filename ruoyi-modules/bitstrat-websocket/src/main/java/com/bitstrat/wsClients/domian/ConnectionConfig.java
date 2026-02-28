package com.bitstrat.wsClients.domian;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.bitstrat.config.wsClient.ConnectionOtherConfig;
import com.bitstrat.domain.Account;
import com.bitstrat.event.ChannelSend;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.WsClusterManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 15:15
 * @Content
 */

@Data
@Slf4j
public class ConnectionConfig {
    private String userId;
    private String exchange;
    private String websocketType;
    private CountDownLatch awaitConnectDone;
    private URI uri;
    private URI sourceUri;
    private volatile boolean handshakeComplete = false;
    private NioEventLoopGroup loopGroup;
    private boolean useSsl;
    private Channel bridgChannel;

    ChannelSend subscriptCallback;
    /**
     * 对应的channel
     */
    private Channel channel;
    private int readIdleTime;
    private int writeIdleTime;
    private ExchangeConnectionManager exchangeConnectionManager;
    /**
     * 心跳定时任务
     */
    private ScheduledFuture<?> heatebeatScheduledFuture;
    /**
     * 延迟测试定时任务
     */
    private ScheduledFuture<?> delyTestScheduledFuture;

    /**
     * 主动获取仓位定时任务
     */
    private ScheduledFuture<?> positionSyncScheduledFuture;

    private WebSocketClientHandshaker webSocketClientHandshaker;

    /**
     * 鉴权相关
     */
    private Account account;


    /**
     * 当前连接订阅的消息
     */
    private volatile ConcurrentHashSet<String> subscriptQueue;

    private volatile Long maxReconnectTimes = 3L;
    private volatile AtomicInteger currentReconnectTimes = new AtomicInteger(1);

    /**
     * 心跳以及延迟检测
     */
    private volatile Long lastPingTimeStamp = 0L;
    private volatile String lastPingMsgId = "";
    private volatile Long lastPongTimeStamp = 0L;
    private volatile Long dely = 0L;

    public void setDely(Long dely) {
        if(otherConfig != null) {
            otherConfig.setDely(dely);
        }
        this.dely = dely;
        WsClusterManager wsClusterManager = this.exchangeConnectionManager.getWsClusterManager();
        if(wsClusterManager != null) {
            //更新API 延迟
            //exchange+":"+websocketType+":"+account.getId()
            String profix = this.exchange + ":" + this.websocketType + ":" + account.getId();
            wsClusterManager.setApiDely(this.account.getId(),this.userId,profix,dely);
        }else{
            log.error("用户更新 Dely Redis 失败，没有找到 wsClusterManager");
        }
    }

    private ConnectionOtherConfig<?> otherConfig;

    /**
     * 用户下线
     */
    public void offLineUser() {
        WsClusterManager wsClusterManager = this.exchangeConnectionManager.getWsClusterManager();
        if(wsClusterManager != null) {

            //更新API 延迟
            //exchange+":"+websocketType+":"+account.getId()
            String profix = this.exchange + ":" + this.websocketType + ":" + account.getId();
            wsClusterManager.userOffline(this.userId,profix);
        }else{
            log.error("用户下线更新Redis 失败，没有找到 wsClusterManager");
        }
    }
}
