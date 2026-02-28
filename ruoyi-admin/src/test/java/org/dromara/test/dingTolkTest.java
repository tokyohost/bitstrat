package org.dromara.test;

import com.bitstrat.utils.DingTalkBotClient;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/25 17:02
 * @Content
 */

public class dingTolkTest {

    @Test
    public void test() {
        String token = "f90bddf33ae7ba7ce9f3c5cd45fdf0abebdbad9b78b24f49bf2bb43d6cc2a42e";
        String webhook = "https://oapi.dingtalk.com/robot/send?access_token="+token;
        String secret = "SEC68633fbffb8b68489233039cbe1b00eea80bf8058cdb08209bc9c52767e80372"; // 如果没有加签，传 null 或 ""

        DingTalkBotClient dingBot = new DingTalkBotClient();

        // 文本消息 + @指定手机号
        dingBot.sendText(webhook, secret, "🔥 报警：BTC 跌破 66000！", Arrays.asList("17790520134"), false);

        // Markdown 消息
        String markdown = "## ⚠️ 预警通知\n> **ETH** 当前价格：`3490`\n- 时间：" + new Date();
        dingBot.sendMarkdown(webhook, secret, "行情预警", markdown, Collections.emptyList(), false);

    }
    @Test
    public void test1() {

        String webhook = "https://oapi.dingtalk.com/robot/send?access_token=278491b0a845f7efa3d32782300df8d95e56e9a8f929b3b9e99a2304b6efca65";
        String secret = "SEC8791c103fc4af367252a0e54fc8b7d225002b6bcecf2d97ff6fe52830f2dcda3";

        DingTalkBotClient dingBot = new DingTalkBotClient();

        // 文本消息 + @指定手机号
        dingBot.sendText(webhook, secret, "测试", Arrays.asList("17790520134"), false);

        // Markdown 消息
        String markdown = "## 测试\n> **ETH** 当前价格：`3490`\n- 时间：" + new Date();
        dingBot.sendMarkdown(webhook, secret, "测试", markdown, Collections.emptyList(), false);

    }
}
