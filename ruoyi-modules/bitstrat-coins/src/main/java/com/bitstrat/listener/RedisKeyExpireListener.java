package com.bitstrat.listener;

import com.bitstrat.event.RedisKeyExpireEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.ObjectListener;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/2 9:09
 * @Content
 */

@Component
@Slf4j
public class RedisKeyExpireListener implements ExpiredObjectListener {

    public RedisKeyExpireListener(RedissonClient redissonClient) {
        RBucket<T> result = redissonClient.getBucket("*");
        result.addListener(this);

    }

    @Override
    public void onExpired(String name) {
        log.info("redisKeyExpireEvent:{}", name);
        RedisKeyExpireEvent redisKeyExpireEvent = new RedisKeyExpireEvent();
        redisKeyExpireEvent.setKey(name);
        redisKeyExpireEvent.setExpire(new Date().getTime());
        SpringUtils.getApplicationContext().publishEvent(redisKeyExpireEvent);
    }
}
