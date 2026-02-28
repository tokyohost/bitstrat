package com.bitstrat.ai.config;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/27 19:11
 * @Content
 */

import cn.dev33.satoken.stp.StpUtil;
import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.ai.constant.SocketConstant;
import com.bitstrat.ai.domain.CompareContext;
import com.bitstrat.ai.domain.CompareItem;
import com.bitstrat.domain.Account;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.WebSocketType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConnectionManager {
    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;
    // 存储 clientId -> Channel 映射
    private final Map<String, Channel> clientChannelMap = new ConcurrentHashMap<>();

    // 属性键：绑定在 Channel 上用于快速获取 clientId
    private static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf("clientId");

    /**
     * 鉴权 + 绑定客户端连接
     */
    public boolean authenticateAndRegister(ChannelHandlerContext ctx, String clientId, String token) {
        // 1. 执行鉴权逻辑（此处伪代码，你可以接数据库、Redis 等）
        if (!validToken(clientId, token)) {
            return false;
        }

        // 2. 绑定 clientId 到 Channel
        Channel channel = ctx.channel();
        channel.attr(CLIENT_ID_KEY).set(clientId);

        // 3. 存储映射
        clientChannelMap.put(clientId, channel);
        return true;
    }

    /**
     * 获取指定 clientId 的 Channel
     */
    public Channel getChannel(String clientId) {
        return clientChannelMap.get(clientId);
    }

    /**
     * 移除连接（在断开连接或异常时调用）
     */
    public void remove(Channel channel) {
        String clientId = channel.attr(CLIENT_ID_KEY).get();
        if (clientId != null) {
            clientChannelMap.remove(clientId);
            doClearChannel(channel);
        }
    }

    /**
     * 清空资源
     * @param channel
     */
    private void doClearChannel(Channel channel) {
        CompareContext compareContext = channel.attr(SocketConstant.COMPARE_CONTEXT).get();
        //终止行情数据获取
        Account account = compareContext.getAccount();
        //关闭

        for (CompareItem compareItem : compareContext.getCompareList()) {
            if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SWAP)) {
                //关闭合约
                exchangeConnectionManager.closeConnection(channel.id()+ "",account.getId(),compareItem.getExchange(), WebSocketType.LINER);
                //取消定时刷给前端任务
                ScheduledFuture<?> scheduledFuture = channel.attr(SocketConstant.COMPARE_MARKET_PRICE_SCHEDULE).get();
                if (Objects.nonNull(scheduledFuture)) {
                    scheduledFuture.cancel(true);
                }
                //取消定时采样
                ScheduledFuture<?> scheduledSpreadFuture = channel.attr(SocketConstant.COMPARE_SPREAD_RECORD_PRICE_SCHEDULE).get();
                if (Objects.nonNull(scheduledFuture)) {
                    scheduledSpreadFuture.cancel(true);
                }

            }
            if (compareItem.getType().equalsIgnoreCase(BusinessType.COMPARE_TYPE_SPOT)) {
                //关闭现货
                exchangeConnectionManager.closeConnection(channel.id()+ "",account.getId(),compareItem.getExchange(), WebSocketType.SPOT);
            }

        }

    }

    /**
     * 模拟 token 验证（你可以替换为实际验证逻辑）
     */
    private boolean validToken(String clientId, String token) {
        try {
            Object loginIdByToken = StpUtil.getLoginIdByToken(token);
            if (Objects.nonNull(loginIdByToken)) {
                return true;
            }else{
                log.info("无效的token {}", token);
                return false;
            }
        } catch (Exception e) {
            log.info("无效的token {}",e.getMessage());
            return false;
        }
    }
}
