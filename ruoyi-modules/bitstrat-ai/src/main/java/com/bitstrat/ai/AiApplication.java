package com.bitstrat.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 11:31
 * @Content
 */

@SpringBootApplication
@EnableAsync
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AiApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("Ai 服务启动成功");
    }
}
