package com.bitstrat.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:48
 * @Content
 */

public class DifyStreamingClient {

    private final WebClient webClient;

    public DifyStreamingClient(String baseUrl, String apiKey) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }

    /**
     * 返回 Flux<String>，由 Controller 推给前端
     */
    public Flux<? extends Object> streamingFlux(Map<String, Object> inputs, String user) {
        Map<String, Object> body = new HashMap<>();
        body.put("inputs", inputs);
        body.put("response_mode", "streaming");
        body.put("user", user);

        //
        //data:{"event":"text_chunk","workflow_run_id":"d7d4d6b7-6b32-4503-b17a-d35c77b31bfb","task_id":"a53ab507-f21a-48ad-9c76-bb5ab98a1141","data":{"text":"\n","from_variable_selector":["1765193613143","text"]}}
        //怎么封装成 {"type":"text_chunk","data":{"text":"\n","from_variable_selector":["1765193613143","text"]}}
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .defaultIfEmpty("请求失败")
                    .flatMap(msg -> Mono.error(
                        new ResponseStatusException(resp.statusCode(), msg)
                    ))
            )
            .bodyToFlux(String.class)
            .doOnError(Throwable::printStackTrace)
            .onErrorResume(Flux::error)
            // 关键的封装步骤：解析、提取和重构 JSON
            .map(sseFrame -> {
                // 1. 移除 SSE 前缀：移除 "//data:" 和末尾的可能的换行符或注释
                String jsonPayload = sseFrame.replaceFirst("^//data:", "").trim();

                try {
                    // 2. 将字符串解析成 Map 或合适的类
                    // 假设您使用 Jackson ObjectMapper (或其他 JSON 库)
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> originalMap = mapper.readValue(jsonPayload, new TypeReference<Map<String, Object>>() {});
                    if("workflow_finished".equalsIgnoreCase(originalMap.get("event").toString())){
                        return "{\"type\": \"done\", \"code\": 200}";
                    }
                    // 3. 提取所需字段并重构
                    Map<String, Object> newPayload = new HashMap<>();
                    newPayload.put("type", originalMap.get("event"));
                    newPayload.put("data", originalMap.get("data"));

                    // 4. 将重构后的 Map 转换回 JSON 字符串
                    return mapper.writeValueAsString(newPayload);

                } catch (Exception e) {
                    // 异常处理：如果解析失败，可以返回一个空字符串或错误JSON，
                    // 或者直接抛出异常让流终止（取决于业务需求）。
                    e.printStackTrace();
                    return "{\"type\": \"error\", \"message\": \""+e.getMessage()+"\", \"code\": 400}"; // 返回一个安全、但可能需要特殊处理的默认值
                }
            })
            .publishOn(Schedulers.boundedElastic());
    }
}
