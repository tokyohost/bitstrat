package com.bitstrat.client;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/18 17:08
 * @Content
 */

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.annotation.AccountPaptrading;
import com.bitstrat.constant.ApiTypeConstant;
import com.bitstrat.constant.PaptradingType;
import com.bitstrat.domain.Account;
import com.bitstrat.utils.APITypeHelper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.dromara.common.core.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class OkxRestClient {

    private static final String API_URL = "https://www.okx.com";


    DateTimeFormatter dateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final RestTemplate restTemplate = new RestTemplate();

    @AccountPaptrading
    public ResponseEntity<String> get(String path, Map<String, String> queryParams, Account account) {
        String url = API_URL + path;


        if (queryParams != null && !queryParams.isEmpty()) {
            url += "?" + buildQueryString(queryParams);
            path +="?" + buildQueryString(queryParams);
        }
        log.info("okx client get path:{}", url);
        HttpHeaders headers = buildAuthHeaders("GET", path, "",account);
        String paptrading = getPaptrading();
        if(StringUtils.isNotBlank(paptrading) && PaptradingType.TEST.getType().equalsIgnoreCase(paptrading)){
            headers.add("x-simulated-trading", "1");
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @NotNull
    private static String getPaptrading() {
        String paptrading = PaptradingType.TEST.getType();
        String type = APITypeHelper.peek();
        if(StringUtils.isBlank(type)){
            return paptrading;
        }
        switch (type) {
            case ApiTypeConstant.TEST:
                paptrading = PaptradingType.TEST.getType();
                break;
            case ApiTypeConstant.PRO:
                paptrading = PaptradingType.PRO.getType();
                break;
        }
        return paptrading;
    }
    public ResponseEntity<String> getNoAuth(String path, Map<String, String> queryParams) {
        String url = API_URL + path;


        if (queryParams != null && !queryParams.isEmpty()) {
            url += "?" + buildQueryString(queryParams);
        }
        log.info("okx client getNoAuth path:{}", url);
        HttpHeaders headers = new HttpHeaders();
        String paptrading = getPaptrading();
        if(StringUtils.isNotBlank(paptrading) && PaptradingType.TEST.getType().equalsIgnoreCase(paptrading)){
            headers.add("x-simulated-trading", "1");
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @AccountPaptrading
    public ResponseEntity<String> post(String path, String bodyJson, Account account) {
        log.info("okx client post path:{} body:{}", path,bodyJson);
        HttpHeaders headers = buildAuthHeaders("POST", path, bodyJson,account);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String paptrading = getPaptrading();
        if(StringUtils.isNotBlank(paptrading) && PaptradingType.TEST.getType().equalsIgnoreCase(paptrading)){
            headers.add("x-simulated-trading", "1");
        }
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        return restTemplate.exchange(API_URL + path, HttpMethod.POST, entity, String.class);
    }

    @AccountPaptrading
    public HttpHeaders buildAuthHeaders(String method, String requestPath, String body,Account account) {
        HttpHeaders headers = new HttpHeaders();

          String apiKey = account.getApiKey();
          String secretKey = account.getApiSecret();
          String passphrase = account.getPassphrase();

        String utcTimestamp =dateTimeFormatter
            .withZone(ZoneOffset.UTC)
            .format(Instant.now());
        String timestamp = utcTimestamp;
        String preSign = timestamp + method.toUpperCase() + requestPath + body;
        String sign = generateSignature(preSign, secretKey);

        headers.add("OK-ACCESS-KEY", apiKey);
        headers.add("OK-ACCESS-SIGN", sign);
        headers.add("OK-ACCESS-TIMESTAMP", timestamp);
        headers.add("OK-ACCESS-PASSPHRASE", passphrase);
//        headers.add("x-simulated-trading", simulatedTrading);
        headers.add("Content-Type", "application/json");

        return headers;
    }

    private String generateSignature(String data, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    private String buildQueryString(Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();
        queryParams.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
        return sb.substring(0, sb.length() - 1); // remove trailing '&'
    }
}

