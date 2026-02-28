package com.bitstrat.wsServer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.bitstrat.config.NetUtil;
import com.bitstrat.config.UserNettyConfig;
import com.bitstrat.config.UserWsServerConditional;
import com.bitstrat.wsServer.handler.UserWsHttpRequestHandler;
import com.bitstrat.wsServer.handler.WebSocketHandlerUser;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ServerInstanceUtil;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@Conditional(UserWsServerConditional.class)
public class WebsocketUserServer {
    /**
     * 主要用于异步处理集群消息
     */
    ExecutorService ClusterMessageExecutorService = Executors.newWorkStealingPool(2);

    @Autowired
    private UserNettyConfig nettyConfig;

    @Autowired
    WebSocketHandlerUser webSocketHandler;
    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    @Autowired
    UserWsHttpRequestHandler httpRequestHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {

        log.info("已加载用户Websocket服务端...");
        log.info("Starting UserWebsocket Netty Server...");
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreads());
        registerNamingService(nettyConfig.getServiceName(),nettyConfig.getPort());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new WebSocketUserServerInitializer(nettyConfig,webSocketHandler,httpRequestHandler));

            // 异步绑定端口
            ChannelFuture f = b.bind(Integer.parseInt(nettyConfig.getPort()));
            f.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    serverChannel = future.channel();
                    log.info("UserWebsocket Netty Server started successfully on port: {}",nettyConfig.getPort());
                } else {
                    log.error("Failed to start UserWebsocket Netty Server", future.cause());
                }
            });


        } catch (Exception e) {
            log.error("Failed to start UserWebsocket Netty Server", e);
            throw e;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        //订阅消息
        WebSocketUtils.subscribeMessage((msg)->{
            log.info("WsClusterManager subscribeMessage: {}", msg);
            ClusterMessageExecutorService.submit(()->{
                try{
                    if (StringUtils.isNoneEmpty(msg.getIgnoreInstanceId()) && ServerInstanceUtil.SERVER_INSTANCE_ID.equals(msg.getIgnoreInstanceId())) {
                        //是当前节点发送的，忽略掉
                        log.info("WsClusterManager ignore message: {}", msg.getMsgId());
                        return;
                    }
                    for (Long sessionKey : msg.getSessionKeys()) {
                        WebSocketUtils.sendMessageBySubscribe(sessionKey,msg.getMessage());
                    }
                }catch (Exception e){
                    log.error("WsClusterManager message SendError",e.getMessage());
                }
            });
        });
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping UserWebsocket Netty Server...");
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("UserWebsocket Netty Server stopped");
    }


    /**
     * 注册到 nacos 服务中
     *
     * @param nettyName netty服务名称
     * @param nettyPort netty服务端口
     */
    private void registerNamingService(String nettyName, String nettyPort) {
        try {
            NamingService namingService = NamingFactory.createNamingService(nacosDiscoveryProperties.getNacosProperties());
//            NamingService namingService = NamingFactory.createNamingService(nacosDiscoveryProperties.getServerAddr());
            InetAddress address = NetUtil.getLocalAddress(nettyConfig.getIp());
            namingService.registerInstance(nettyName, address.getHostAddress(), Integer.parseInt(nettyPort));
            log.warn("Netty 服务已注册Nacos ...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
