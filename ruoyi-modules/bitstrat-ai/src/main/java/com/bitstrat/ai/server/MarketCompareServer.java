package com.bitstrat.ai.server;

import com.bitstrat.ai.config.CompareNettyConfig;
import com.bitstrat.ai.config.WsServerConditional;
import com.bitstrat.ai.server.handler.HttpRequestHandler;
import com.bitstrat.ai.server.handler.WebSocketHandlerMarketCompare;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Conditional(WsServerConditional.class)
public class MarketCompareServer{

    @Autowired
    private CompareNettyConfig nettyConfig;

    @Autowired
    WebSocketHandlerMarketCompare webSocketHandler;

    @Autowired
    HttpRequestHandler httpRequestHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {
        log.info("已加载服务端...");
        log.info("Starting MarketCompare Netty Server...");
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreads());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new MarketCompareWebSocketServerInitializer(nettyConfig,webSocketHandler,httpRequestHandler));

            // 异步绑定端口
            ChannelFuture f = b.bind(Integer.parseInt(nettyConfig.getPort()));
            f.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    serverChannel = future.channel();
                    log.info("MarketCompare Netty Server started successfully on port: {}",nettyConfig.getPort());
                } else {
                    log.error("Failed to start MarketCompare Netty Server", future.cause());
                }
            });

        } catch (Exception e) {
            log.error("Failed to start MarketCompare Netty Server", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping MarketCompare Netty Server...");
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("MarketCompare Netty Server stopped");
    }
}
