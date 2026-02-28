package com.bitstrat.controller;

import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/29 16:39
 * @Content
 */

@Component
@Slf4j
public class RedisKeyExpire {

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RedisKeyExpire(RedissonClient redissonClient) {

//        RTopic topic = redissonClient.getTopic("__keyevent@0__:expired");
//        topic.addListener(String.class, (channel, expiredKey) -> {
//            log.error("⏰ Key expired: " + expiredKey);
//            if(expiredKey.startsWith("aiRequestKey")){
//                //重试
//                log.info("重试发起Dify流程 expired {}",expiredKey);
//                executorService.execute(()->{
//                    try {
//                        testController.autoRun();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//
//            }
//        });
    }
}
