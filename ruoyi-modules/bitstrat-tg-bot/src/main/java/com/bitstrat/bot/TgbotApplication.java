package com.bitstrat.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:36
 * @Content
 */
@SpringBootApplication
public class TgbotApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TgbotApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("tg bot 启动成功");
    }
}
