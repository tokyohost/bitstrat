package com.bitstrat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bitstrat.netty")
public class NettyConfig {
    private String port = "1883";
    private int bossThreads = 1;
    private int workerThreads = 4;
    private int maxFrameLength = 65536;
    private boolean ssl = false;
    private String certFile;
    private String keyFile;
    private int connectionTimeout = 30;
    private int maxConnections = 10000;
}
