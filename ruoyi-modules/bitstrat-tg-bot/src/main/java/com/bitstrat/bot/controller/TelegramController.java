package com.bitstrat.bot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.bot.client.TelegramBotClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/8 8:51
 * @Content
 */

@RestController
@RequestMapping("/telegram")
@Slf4j
public class TelegramController {

    @Autowired
    TelegramBotClient telegramBotClient;

    @Value("${botNotify.tgBotBaseUrl}")
    private String tgUrl;

    @Value("${botNotify.tgBotToken}")
    private String tgToken;

    // 用于接收 Telegram 机器人的消息
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveTelegramMessage(@RequestBody JSONObject message) {
        log.info("receive telegram message: {}", message);
        // 从消息中提取用户的 clientId（假设用户的 clientId 是 `message` 对象中的某个字段）
        String clientId = message.getJSONObject("message").getJSONObject("from").getString("id"); // 获取 Telegram 用户的 ID
        String text = message.getJSONObject("message").getString("text");
        if (text.equalsIgnoreCase("myid")) {
            String msg = "您的clientID 为" + clientId + " 请复制并配置在平台通知配置中";
            telegramBotClient.sendMessage(tgUrl + "/bot", tgToken, clientId, msg);
            return ResponseEntity.ok("ok");
        }
        String msg = "无对应操作 " + clientId;
        telegramBotClient.sendMessage(tgUrl + "/bot", tgToken, clientId, msg);
        // 返回该用户的 clientId
        return ResponseEntity.ok("ok");
    }
}
