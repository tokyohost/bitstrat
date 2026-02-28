package org.dromara.test;

import com.bitstrat.utils.DingTalkBotClient;
import com.bitstrat.utils.TelegramBotClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/25 17:05
 * @Content
 */

public class TgTest {
    @Test
    public void test() {
        String botToken = "REMOVED"; // 替换成你的 BOT Token
        String chatId = "5061986239";         // 群组或个人的 chat_id
        String tgBotBaseUrl = "https://api.telegram.org/bot";

        TelegramBotClient tg = new TelegramBotClient(new RestTemplate());

        String msg = "*行情通知*\nBTC 价格已突破 70000 美元";
        boolean success = tg.sendMarkdown(tgBotBaseUrl, botToken, chatId, msg);

        System.out.println("发送结果: " + success);

    }
}
