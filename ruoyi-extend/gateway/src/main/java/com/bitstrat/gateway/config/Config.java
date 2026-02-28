package com.bitstrat.gateway.config;

import com.bitstrat.gateway.GatewayApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static io.netty.handler.codec.http.cookie.CookieHeaderNames.MAX_AGE;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/17 20:45
 * @Content
 */

@Configuration
public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1.配置跨域
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public GlobalFilter optionsFilter() {
        return (exchange, chain) -> {
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                // 构造标准响应
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.NO_CONTENT);
                response.getHeaders().setAccessControlMaxAge(Duration.ofHours(1));
                return response.setComplete(); // 中断后续处理
            }
            return chain.filter(exchange);
        };
    }

}
