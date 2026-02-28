package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.service.impl.AiServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/25 16:45
 * @Content
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AICallbackController {

    @Autowired
    AiServiceImpl aiService;


    @PostMapping("/callback")
    @SaIgnore
    public String callback(@RequestBody JSONObject data){
       return aiService.aiTaskCallBack(data);
    }

    /**
     * "taskId":"2003839874063503361",
     *   "usage":{
     *     "prompt_tokens":24656,
     *     "prompt_unit_price":"2",
     *     "prompt_price_unit":"0.000001",
     *     "prompt_price":"0.049312",
     *     "completion_tokens":3058,
     *     "completion_unit_price":"3",
     *     "completion_price_unit":"0.000001",
     *     "completion_price":"0.009174",
     *     "total_tokens":27714,
     *     "total_price":"0.058486",
     *     "currency":"RMB",
     *     "latency":111.68808843195438
     *   }
     * @param data
     * @return
     */
    @PostMapping("/difyCallbackError")
    @SaIgnore
    public String difyCallbackError(@RequestBody JSONObject data){
       return aiService.aiTaskCallBack(data);
    }
}
