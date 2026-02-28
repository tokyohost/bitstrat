package com.bitstrat.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 17:55
 * @Content
 */

public interface MarketTask {
    public void run(JSONObject params);

    public void stop();

    public String getExchangeName();
}
