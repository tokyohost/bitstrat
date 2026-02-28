package com.bitstrat.store;

import com.bitstrat.domain.OrderInfo;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 19:42
 * @Content
 */

@Component
public class OrderStore {

    static RedissonClient redisson;
    static String nodeAuth;

    public OrderStore(RedissonClient redisson,@Value("${node.auth}")String nodeName) {
        this.redisson = redisson;
        this.nodeAuth = nodeName;
    }

    private static final ConcurrentHashMap<String, OrderInfo> orderHolder = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> userOrderHolder = new ConcurrentHashMap<>();
    private static List<String> cancelStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Cancelled", "Deactivated");
    private static List<String> finalStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Cancelled", "Deactivated");

    public static ConcurrentHashMap<String, OrderInfo> getOrderHolder() {
        return orderHolder;
    }

    public static OrderInfo getOrderByApiKey(String apiKey) {
        if (userOrderHolder.containsKey(apiKey)) {
            return orderHolder.get(userOrderHolder.get(apiKey));
        }
        return null;
    }

    public static OrderInfo getOrderByLinkId(String orderid) {
        return orderHolder.get(orderid);
    }

    public synchronized static void put(OrderInfo orderInfo) {
        orderHolder.put(orderInfo.getOrderId(), orderInfo);
        userOrderHolder.put(orderInfo.getApiKey(), orderInfo.getOrderId());
        RBucket<String> bucket = redisson.getBucket(orderInfo.getExchange()+":"+ orderInfo.getApiKey()+":"+orderInfo.getSymbol());
        bucket.set(nodeAuth);
    }

    public synchronized static void remove(OrderInfo order) {
        OrderInfo remove = orderHolder.remove(order.getOrderId());
        if (remove != null) {
            String apiKey = remove.getApiKey();
            userOrderHolder.remove(apiKey);
            RBucket<String> bucket = redisson.getBucket(remove.getExchange()+":"+ remove.getApiKey()+":"+remove.getSymbol());
            bucket.delete();
        }

    }

    public static List<String> getCancelStatus() {
        return cancelStatus;
    }
}
