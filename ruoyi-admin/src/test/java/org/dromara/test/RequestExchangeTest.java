package org.dromara.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * 请求交易所api单元测试案例
 * @author caoyang
 * @date 2025-05-02
 */

//@DisplayName("请求交易所api单元测试案例")
public class RequestExchangeTest {

//    @DisplayName("测试 okx 方法")
    @Test
    public void testOkxApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

// 使用 Apache HttpClient 提升兼容性
//        HttpClient httpClient = HttpClient.newHttpClient();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(factory);
        String url = "https://www.okx.com/api/v5/public/instruments";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
    }


}
