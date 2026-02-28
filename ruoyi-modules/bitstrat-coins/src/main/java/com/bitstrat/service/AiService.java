package com.bitstrat.service;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.AIOperateItem;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.event.RedisKeyExpireEvent;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/22 17:44
 * @Content
 */


public interface AiService {
    void doOperate(AIOperateItem aiOperate, ExchangeType exchangeType, Account account,String requestKey,String details,Long taskId);

    void invokeAiTask(CoinsAiTask coinsAiTask);

    String aiTaskCallBack(JSONObject data);
    String aiTaskCallBackError(JSONObject data);

    void onRequestKeyExpire(RedisKeyExpireEvent redisKeyExpireEvent);
}
