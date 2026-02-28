package com.bitstrat.wsClients;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ServerInstanceUtil;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WsClusterManager {

    @Autowired
    private Environment env;

    private final RedissonClient redisson;
    private final String nodeId; // 当前服务节点 ID

    private final static String USER_NODE_KEY = "ws:user:node:";
    private final static String NODE_USERS_KEY = "ws:node:users:";
    private static final String NODE_ALIVE_KEY = "ws:node:alive:"; // 节点心跳 key
    public  final static String CONNECT_DELY_KEY = "ws:node:connectDelay";

    public WsClusterManager(RedissonClient redisson) {
        this.redisson = redisson;
        this.nodeId = getInstanceId();
        ServerInstanceUtil.SERVER_INSTANCE_ID = this.nodeId;
        log.info("节点启动: 初始化节点ID:"+nodeId);
    }

    /** 生成当前节点 ID */
    private String getInstanceId() {
        //获取docker 容器名称
        // 优先手动配置
        String id = System.getenv("NODE_ID");
        if (id != null) {
            return id;
        }

        // 其次使用容器 hostName（docker / k8s）
        id = System.getenv("HOSTNAME");
        if (id != null){
            return id;
        }

        // fallback 本地开发使用 IP
        return NetUtil.getLocalhostStr();
    }

    /** 用户上线 */
    public void userOnline(String userId,String profixStr) {
        // 用户所属节点
        RBucket<Object> bucket = redisson.getBucket(USER_NODE_KEY + userId + ":" + profixStr);
        bucket.set(nodeId);
        bucket.expire(Duration.ofMinutes(3));
        // 节点持有用户
        RSet<Object> set = redisson.getSet(NODE_USERS_KEY + nodeId);
        set.add(userId);
        set.expire(Duration.ofMinutes(3));
        log.info("集群用户 {} 已上线 节点ID: {}",userId,nodeId);
    }

    /** 用户下线 */
    public void userOffline(String userId,String profixStr) {
        redisson.getBucket(USER_NODE_KEY + userId+":"+profixStr).delete();
        redisson.getSet(NODE_USERS_KEY + nodeId).remove(userId);
        log.info("集群用户 {} 已下线 节点ID: {}",userId,nodeId);
    }

    /** 查询用户是否在线 */
    public boolean isOnline(String userId,String profixStr) {
        return redisson.getBucket(USER_NODE_KEY + userId + ":" + profixStr).isExists();
    }

    /** 获取用户所在节点 */
    public String getUserNode(String userId,String profixStr) {
        return (String) redisson.getBucket(USER_NODE_KEY + userId+":"+profixStr).get();
    }

    // -----------------------------------------------------------
    // 服务停止事件：清理本节点所有用户
    // -----------------------------------------------------------
    @EventListener(ContextClosedEvent.class)
    public void cleanDely() {
        long exists = redisson.getKeys().countExists(CONNECT_DELY_KEY + ":" + getNodeId() + "*");
        redisson.getKeys().deleteByPattern(CONNECT_DELY_KEY+":"+getNodeId()+"*");
        log.info("节点 {} 延迟信息 已清理 {} 个",getNodeId(),exists);
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        log.warn("服务正在停止，执行节点 {} 用户清理...", nodeId);

        RSet<String> userSet = redisson.getSet(NODE_USERS_KEY + nodeId, StringCodec.INSTANCE);

        if (userSet.isEmpty()) {
            log.info("节点 {} 无在线用户，无需清理。", nodeId);
            return;
        }

        int count = 0;

        for (String userId : userSet) {
            // 删除 ws:user:node:{user} 绑定
            String patternPrefix = "*";

            // 扫描 ws:user:node:user:* 的所有 bucket（兼容多个 prefix）
            Iterable<String> keys = redisson.getKeys().getKeysByPattern(USER_NODE_KEY + userId + ":*");

            for (String key : keys) {
                redisson.getBucket(key).delete();
            }

            count++;
        }

        // 最后删除本节点的用户集合
        userSet.delete();

        log.warn("节点 {} 清理完成，已下线 {} 用户。", nodeId, count);
    }

    @PostConstruct
    public void listenNodeExpire() {
        String topicName = "__keyevent@0__:expired";

        RTopic topic = redisson.getTopic(topicName,new StringCodec());
        topic.addListener(String.class, (channel, expiredKey) -> {
            log.info("redis 过期KEY {}", expiredKey);
            if (expiredKey.startsWith(NODE_ALIVE_KEY)) {
                String expiredNodeId = expiredKey.substring(NODE_ALIVE_KEY.length());
                log.warn("检测到节点过期: {}", expiredNodeId);
                cleanDeadNode(expiredNodeId);
            }
        });

        log.info("节点过期监听已启动...");
    }

    /** 清理死亡节点 */
    public void cleanDeadNode(String deadNodeId) {
        log.warn("开始清理失效节点 {} 的用户数据...", deadNodeId);

        RSet<String> userSet = redisson.getSet(NODE_USERS_KEY + deadNodeId, StringCodec.INSTANCE);

        for (String userId : userSet) {
            // 删除 ws:user:node:user-*（兼容不同 prefix）
            Iterable<String> keys = redisson.getKeys()
                .getKeysByPattern(USER_NODE_KEY + userId + ":*");

            for (String key : keys) {
                redisson.getBucket(key).delete();
            }
        }

        // 最后删除节点用户列表
        userSet.delete();

        log.warn("节点 {} 清理完成，共处理 {} 个用户", deadNodeId, userSet.size());
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setApiDely(Long accountId,String userId,String profix, Long apiDely) {
        RBucket<Long> bucket = redisson.getBucket(CONNECT_DELY_KEY+":"+getNodeId()+":" + accountId);
        bucket.set(Long.valueOf(apiDely), Duration.ofHours(24));
        //刷新上线状态
        userOnline(userId,profix);
    }
    public Long getApiDely(Long accountId,String nodeId) {
        RBucket<Object> bucket = redisson.getBucket(CONNECT_DELY_KEY+":"+nodeId+":" + accountId);
        Object obj = bucket.get();
        if (obj == null) return null;
        return ((Number) obj).longValue();
    }
}
