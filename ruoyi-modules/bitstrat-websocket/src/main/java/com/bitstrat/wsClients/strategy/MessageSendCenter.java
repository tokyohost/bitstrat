package com.bitstrat.wsClients.strategy;

import com.bitstrat.wsClients.strategy.MessageSendStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 17:01
 * @Content
 */

@Component
public class MessageSendCenter {
    HashMap<String, MessageSendStrategy> strategyHashMap = new HashMap<>();
    public MessageSendCenter(List<MessageSendStrategy> strategies) {

        for (MessageSendStrategy strategy : strategies) {
            strategyHashMap.put(strategy.getExchange(), strategy);
        }
    }

    public MessageSendStrategy getStrategy(String exchange) {
        return strategyHashMap.get(exchange);
    }
}
