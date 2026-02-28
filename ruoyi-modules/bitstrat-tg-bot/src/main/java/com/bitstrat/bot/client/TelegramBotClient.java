package com.bitstrat.bot.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/25 16:53
 * @Content
 */

@Component
public class TelegramBotClient {


    private final RestTemplate restTemplate;

    public TelegramBotClient(RestTemplate restTemplate) {
//        this.baseUrl = "https://api.telegram.org/bot" + botToken + "/";
        this.restTemplate = restTemplate;
    }

    /**
     *
     * @param tgBotBaseUrl Telegram Bot API 的基本 URL
     * @param botToken 机器人令牌
     * @param chatId 接收者 chat_id（可以是群组 ID）
     * @param text 文本内容
     * @return 是否成功
     * @return
     */
    public boolean sendMessage(String tgBotBaseUrl, String botToken, String chatId, String text) {
        String url = tgBotBaseUrl + botToken + "/" + "sendMessage";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", text);
        request.put("parse_mode", "Markdown"); // 可选: HTML / Markdown

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception ex) {
            System.err.println("Telegram 发送失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 发送 Markdown 格式消息
     */
    public boolean sendMarkdown(String tgBotBaseUrl, String botToken, String chatId, String markdownText) {
        return sendMessage(tgBotBaseUrl, botToken, chatId, markdownText);
    }

    /**
     * 构造可点击链接
     */
    public static String link(String title, String url) {
        return "[" + title + "](" + url + ")";
    }

}
