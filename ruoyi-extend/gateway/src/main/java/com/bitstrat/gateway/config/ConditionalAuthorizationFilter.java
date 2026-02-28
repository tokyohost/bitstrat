package com.bitstrat.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ConditionalAuthorizationFilter implements GatewayFilter {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求 Header 中是否有 token
        String token = exchange.getRequest().getHeaders().getFirst("token");
        String encryptKey = exchange.getRequest().getHeaders().getFirst("encrypt-key");
        String clientid = exchange.getRequest().getHeaders().getFirst("clientid");

        // 构建新的请求
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

        // 如果请求有 token，就加 Authorization
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        // 全部请求都加这两个 Header
        requestBuilder.header("encrypt-key", encryptKey);
        requestBuilder.header("clientid", clientid);

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }
}
