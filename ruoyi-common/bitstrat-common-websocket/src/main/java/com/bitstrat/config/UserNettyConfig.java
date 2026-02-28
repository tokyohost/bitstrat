package com.bitstrat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bitstrat.user-websocket")
public class UserNettyConfig {
    private String port = "8400";
    private int bossThreads = 1;
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    private int maxFrameLength = 65536;
    private boolean ssl = false;
    private String certFile;
    private String keyFile;
    private int connectionTimeout = 30;
    private int maxConnections = 10000;
    private String path = "/user";

    private String serviceName;

    /**
     * 注册至nacos 时的客户端IP
     */
    private String ip;
}
