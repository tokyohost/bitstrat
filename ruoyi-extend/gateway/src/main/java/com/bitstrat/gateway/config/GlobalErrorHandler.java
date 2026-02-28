package com.bitstrat.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/18 9:56
 * @Content
 */

@Component
@Order(-1) // 一定要比默认的优先级高
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(HttpStatus.OK);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 500);
        body.put("msg", "Sorry, a gateway error occurred. We are working on it. Please try again later.");
        body.put("path", exchange.getRequest().getPath().value());
        body.put("copyright", "© 2024 BitStrat Cloud Technology Co., Ltd. All rights reserved.");

        // 常见异常细分
        if (ex instanceof NotFoundException) {
            // LB 找不到服务实例 / 路由不存在
            body.put("code", 404);
            body.put("msg", "Service Not Found");

        } else if (ex instanceof ServiceUnavailableException) {
            // 服务不可用
            body.put("code", 503);
            body.put("msg", "Service Unavailable, please try again later");

        }

//        else if (ex instanceof CallNotPermittedException) {
//            // 熔断（resilience4j）
//            body.put("code", 503);
//            body.put("msg", "Service temporarily unavailable");
//
//        }

        else if (ex instanceof TimeoutException) {
            // 下游超时
            body.put("code", 504);
            body.put("msg", "Server Timeout,We are working on it. Please try again later.");

        } else if (ex instanceof ConnectException) {
            // 连接失败 / 服务未启动
            body.put("code", 503);
            body.put("msg", "Unable to connect to service, We are working on it. Please try again later.");

        } else if (ex instanceof ResponseStatusException
            && ((ResponseStatusException) ex).getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            // 手动抛出的 503
            body.put("code", 503);
            body.put("msg", "Service Unavailable,We are working on it. Please try again later.");

        } else {
            // 兜底
            body.put("code", 500);
            body.put("msg", "Gateway Error, Please try again later");
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
