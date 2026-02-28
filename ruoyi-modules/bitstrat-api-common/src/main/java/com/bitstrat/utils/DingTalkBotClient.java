package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/25 17:01
 * @Content
 */

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class DingTalkBotClient {
    private final RestTemplate restTemplate;

    public DingTalkBotClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 发送文本消息
     * @param webhookUrl
     * @param secret 可选：如果启用了加签
     * @param content
     * @param atMobiles
     * @param isAtAll
     * @return
     */
    public boolean sendText(String webhookUrl, String secret, String content, List<String> atMobiles, boolean isAtAll) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        payload.put("text", text);

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles);
        at.put("isAtAll", isAtAll);
        payload.put("at", at);

        return send(webhookUrl, secret, payload);
    }

    public boolean sendMarkdown(String webhookUrl, String secret, String title, String markdownText, List<String> atMobiles, boolean isAtAll) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", markdownText);
        payload.put("markdown", markdown);

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles);
        at.put("isAtAll", isAtAll);
        payload.put("at", at);

        return send(webhookUrl, secret, payload);
    }

    private boolean send(String webhookUrl, String secret, Map<String, Object> payload) {
        try {
            String url = webhookUrl;
            if (secret != null && !secret.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                String sign = generateSign(secret, timestamp);
                url += "&timestamp=" + timestamp + "&sign=" + sign;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("钉钉消息发送失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 加签算法
     */
    private String generateSign(String secret, long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(Base64.getEncoder().encodeToString(signData), "UTF-8");
    }


}
