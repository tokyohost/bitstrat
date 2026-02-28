package com.bitstrat.ai.distuptor;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 17:07
 * @Content
 */


public class OkxKlineFetcher {

    public static String fetchFormattedPrompt(String symbol) throws IOException, InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.okx.com/api/v5/market/candles?instId="+symbol+"&bar=1m&limit=50";


        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        JSONObject json= JSONObject.parseObject(response.getBody());
        JSONArray data = json.getJSONArray("data");

        JSONArray formatted = new JSONArray();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        for (int i = data.size() - 1; i >= 0; i--) {  // 按时间升序
            JSONArray row = data.getJSONArray(i);
            JSONObject kline = new JSONObject();
            kline.put("timestamp", formatter.format(Instant.ofEpochMilli(Long.parseLong(row.getString(0)))));
            kline.put("open", row.getDouble(1));
            kline.put("high", row.getDouble(2));
            kline.put("low", row.getDouble(3));
            kline.put("close", row.getDouble(4));
            kline.put("volume", row.getDouble(5));
            formatted.add(kline);
        }

        // 构建 GPT prompt
        String prompt = "你是一个胜率很高的专业短线交易者,你会严格按照三因子体系交易策略,请根据以下 "+symbol+" 永续合约的 1 分钟 K 线数据，分析当前市场趋势，并提供交易策略建议：\n\n"
            + "K线数据如下（共50根）：\n"
            + formatted.toJSONString(JSONWriter.Feature.PrettyFormatWith2Space) + "\n\n"
            + "请回答以下问题：\n"
            + "1. 当前市场处于上涨、下跌还是震荡？\n"
            + "2. 有哪些可能的支撑/阻力位？\n"
            + "3. 建议的交易策略（进场点、止盈、止损）？\n";

        return prompt;
    }


    public static void main(String[] args) {
        try {
            String prompt = fetchFormattedPrompt("ETH-USDT-SWAP");
            System.out.println(prompt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
