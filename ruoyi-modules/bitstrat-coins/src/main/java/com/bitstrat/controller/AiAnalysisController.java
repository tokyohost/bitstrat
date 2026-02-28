package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.constant.APITypeEnum;
import com.bitstrat.domain.AiStreamQuery;
import com.bitstrat.domain.vo.CoinsAiConfigVo;
import com.bitstrat.service.AiSereamService;
import com.bitstrat.service.ICoinsAiConfigService;
import com.bitstrat.utils.DifyStreamingClient;
import lombok.AllArgsConstructor;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:47
 * @Content
 */
@RestController
@RequestMapping("/stream")
@AllArgsConstructor
public class AiAnalysisController {

    private final AiSereamService aiSereamService;
    private final ICoinsAiConfigService aiConfigService;


    @PostMapping(value = "/aireq", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaCheckLogin
    public Flux<? extends Object> aireqStream(@RequestBody AiStreamQuery aiStreamQuery) {
        CoinsAiConfigVo coinsAiConfigVo = aiConfigService.queryById(aiStreamQuery.getApiId());
        if (Objects.isNull(coinsAiConfigVo)) {
            return Flux.just("data: {\"type\": \"error\", \"message\": \"智能体不存在\", \"code\": 400}\n\n")
                .concatWith(Flux.error(new RuntimeException("Error occurred")));
        }
        if (!APITypeEnum.AI_SUMMARY.getType().equalsIgnoreCase(coinsAiConfigVo.getType())) {
            return Flux.just("data: {\"type\": \"error\", \"message\": \"智能体不支持此操作\", \"code\": 400}\n\n")
                .concatWith(Flux.error(new RuntimeException("Error occurred")));
        }
        String[] symbols = aiStreamQuery.getSymbol().split(",");
        List<String> collect = Arrays.stream(symbols).collect(Collectors.toList());

        try {
            String userPrompt = aiSereamService.queryKLinePrompt(aiStreamQuery.getExchange(), collect, aiStreamQuery);
//            String userPrompt = "TEST";
            String input = """
                USER REQUIRE && QUESTION \n
                """;
            userPrompt = input + "\n" + aiStreamQuery.getContent() + "\n" + userPrompt;


            String url = coinsAiConfigVo.getUrl();
            String apiKey = coinsAiConfigVo.getToken();

            DifyStreamingClient client = new DifyStreamingClient(url, apiKey);

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("content", userPrompt);

            LoginUser loginUser = LoginHelper.getLoginUser();
            Long userId = loginUser.getUserId();
            return client.streamingFlux(inputs, userId + ":" + loginUser.getUsername());  // 返回 Flux<String>
        } catch (Exception e) {
            e.printStackTrace();

            return Flux.just("data: {\"type\": \"error\", \"message\": \""+e.getMessage()+"\", \"code\": 400}\n\n")
                .concatWith(Flux.error(new RuntimeException("Error occurred")));
        }


    }
}
