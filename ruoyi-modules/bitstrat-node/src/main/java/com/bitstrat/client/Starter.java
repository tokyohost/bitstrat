package com.bitstrat.client;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:51
 * @Content
 */

@Component
public class Starter {

    @Value("${node.server}")
    String server;

    @Autowired
    WebSocketClient webSocketClient;

    @PostConstruct
    public void runClient() {
        Thread thread = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    try {
                        webSocketClient.init(server);
                        webSocketClient.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.sleep(3000);
                    }
                }

            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
