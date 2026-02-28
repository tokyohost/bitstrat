package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:44
 * @Content
 */

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

public class DifyStreamWebClient {

    public static void main(String[] args) throws InterruptedException {

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("content", "你怎么看今天 BTC？");

        streaming(
            "https://api.dify.ai/v1/workflows/run",
            "app-h6J7AQcWfdr01fo3K8muJtYD",
            inputs,
            "test-user"
        );

        // 让 Reactor 持续运行
        Thread.currentThread().join();
    }

    public static void streaming(String url,
                                 String apiKey,
                                 Map<String, Object> inputs,
                                 String user) {

        WebClient webClient = WebClient.builder()
            .baseUrl(url)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", inputs);
        body.put("response_mode", "streaming");
        body.put("user", user);

        Flux<String> flux = webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)  // SSE
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class);

        flux.publishOn(Schedulers.boundedElastic())
            .subscribe(
                chunk -> System.out.println("📩 收到 chunk: " + chunk),
                error -> System.out.println("❌ 错误: " + error.getMessage()),
                () -> System.out.println("🎉 streaming 结束")
            );
    }
}

