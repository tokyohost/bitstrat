package com.bitstrat.service;

import com.bitstrat.utils.EmailTemplateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;

@Service
public class MailgunClientRestTemplate {

    private final String apiKey;
    private final String domain;
    private final String from;
    private final RestTemplate restTemplate;

    public MailgunClientRestTemplate(@Value("${mailgun.api-key}")String apiKey, @Value("${mailgun.domain}")String domain, @Value("${mailgun.form}") String from) {
        this.apiKey = apiKey;
        this.domain = domain;
        this.from = from;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 发送文本邮件
     */
    public String sendTextEmail(List<String> toList, String subject, String text) {
        return sendEmail(toList, subject, text, null);
    }

    /**
     * 发送 HTML 邮件
     */
    public String sendHtmlEmail(List<String> toList, String subject, String html) {
        return sendEmail(toList, subject, null, html);
    }

    /**
     * 内部统一发送方法
     */
    private String sendEmail(List<String> toList, String subject, String text, String html) {
        String url = "https://api.mailgun.net/v3/" + domain + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Basic Auth
        String auth = "api:" + apiKey;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", from);

        StringJoiner toJoiner = new StringJoiner(",");
        for (String to : toList) {
            toJoiner.add(to);
        }
        body.add("to", toJoiner.toString());
        body.add("subject", subject);
        if (text != null) body.add("text", text);
        if (html != null) body.add("html", html);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        return response.getBody();
    }

}
