package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 18:04
 * @Content
 */


import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Component
public class DifyWorkflowUtils {

    private static final String BASE_URL = "https://api.dify.ai/v1/workflows/run";
    private static final String API_KEY = "YOUR-WORKFLOW-API-KEY"; // 替换为你的密钥
    static ExecutorService postService = Executors.newFixedThreadPool(1);
    private static RestTemplate restTemplate;

    public DifyWorkflowUtils() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时（毫秒）
        factory.setReadTimeout(5000);   // 读取超时（毫秒）

        restTemplate =  new RestTemplate(factory);
    }

    public static String runWorkflow(String apiKey, Map<String, Object> inputs,String user,String mode) {
       return runWorkflow(BASE_URL,apiKey,inputs,user,mode);
    }

    /**
     * 调用 Dify Workflow 工作流
     *
     * @param apiKey 工作流 APIKEY
     * @param inputs     输入内容（根据工作流定义）
     * @return 工作流响应内容
     */
    public static String runWorkflow(String url,String apiKey, Map<String, Object> inputs,String user,String mode) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", inputs);
        body.put("response_mode", StringUtils.isEmpty(mode) ? "blocking" : mode);
        body.put("user", user);
        log.info("request url: {} ,body:{}", url, JSONObject.toJSONString(body));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            postService.submit(()->{
                try {
                    ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    log.error("请求已发送 {}",exchange);
                } catch (RestClientException e) {
//                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            });
            return "invoke api success";
        } catch (Exception e) {
            return "调用异常：" + e.getMessage();
        }
    }

    public static void runWorkflowStreaming(String url,
                                            String apiKey,
                                            Map<String, Object> inputs,
                                            String user,
                                            Consumer<String> onMessage) {

        WebClient client = WebClient.builder()
            .baseUrl(url)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", inputs);
        body.put("response_mode", "streaming"); // **关键**
        body.put("user", user);

        log.info("Streaming Request url: {} , body: {}", url, JSONObject.toJSONString(body));

        client.post()
            .uri(url)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)      // 接收流式数据
            .subscribe(
                chunk -> {
                    log.info("收到流数据 chunk: {}", chunk);
                    if (onMessage != null) {
                        onMessage.accept(chunk);
                    }
                },
                error -> log.error("Streaming Error: ", error),
                () -> log.info("Streaming 完成")
            );
    }

    /**
     * 示例：使用字符串输入调用 workflow
     */
    public static void main(String[] args) {

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("content", "你怎么看今天的BTC走势？");


        runWorkflowStreaming(
            "https://ai.bitstrat.org/v1/workflows/run",
            "app-h6J7AQcWfdr01fo3K8muJtYD",
            inputs,
            "test",
            chunk -> {
                // 实时处理每一段流式内容
                System.out.println("📩 收到: " + chunk);
            }
        );
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
