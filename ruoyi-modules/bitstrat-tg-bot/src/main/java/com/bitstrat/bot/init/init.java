package com.bitstrat.bot.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/8 10:45
 * @Content
 */
@Component
@Slf4j
public class init implements ApplicationRunner {

    @Value("${botNotify.tgBotToken}")
    private String tgToken;
    @Value("${botNotify.tgBotBaseUrl}")
    private String tgUrl;

    @Value("${botNotify.webhookCallbackUrl}")
    private String callbackUrl;

    @Autowired
    RestTemplate restTemplate;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("正在初始化...");
        //配置 Telegram Webhook
        // 构造 webhook URL
        String webhookUrl = tgUrl + "/bot" + tgToken + "/setWebhook";

        // 发送 Webhook 配置请求
        String urlWithParams = webhookUrl + "?url=" + callbackUrl;

        try {
            // 向 Telegram API 发送 Webhook 配置请求
            String response = restTemplate.getForObject(urlWithParams, String.class);

            log.info("Telegram Webhook 配置结果: {}", response);
        } catch (Exception e) {
            log.error("Telegram Webhook 配置失败", e);
        }

        log.info("初始化成功...");
    }
}
