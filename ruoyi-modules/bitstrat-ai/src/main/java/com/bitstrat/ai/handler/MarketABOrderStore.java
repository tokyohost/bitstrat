package com.bitstrat.ai.handler;

import com.bitstrat.ai.distuptor.ABDisruptor;
import com.bitstrat.ai.distuptor.MarketPriceDisruptor;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.Event.AckAccountLoadEvent;
import com.bitstrat.domain.PositionWsData;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.dromara.common.core.utils.SpringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 16:26
 * @Content AB 套利市价监控
 */

public class MarketABOrderStore {
    /**
     * key taskId
     */
    final static ConcurrentHashMap<String, ABDisruptor> priceListenerStore = new ConcurrentHashMap<>();
    final static ConcurrentHashMap<Long, AccountBalance> accountBalanceStore = new ConcurrentHashMap<>();
    /**
     * key accountID
     */
    final static ConcurrentHashMap<Long, HashSet<PositionWsData>> positionStore = new ConcurrentHashMap<>();
    /**
     * key exchange:symbol
     * 交易所:币对
     * 如果某个交易所：币对没有人监听了，那么就直接关闭
     */
    final static ConcurrentHashMap<String, ChannelGroup> channelGroup = new ConcurrentHashMap<>();

    public static final AttributeKey<String> EXCHANGE_KEY = AttributeKey.valueOf("exchange_key");
    public static final AttributeKey<String> SYMBOL_KEY = AttributeKey.valueOf("symbol_key");
    public MarketABOrderStore() {

    }

    /**
     * 根据accountId 获取仓位数据
     * @param accountId
     * @return
     */
    public static HashSet<PositionWsData> getPositionByAccountId(Long accountId) {
        return positionStore.computeIfAbsent(accountId, k -> new HashSet<>());
    }

    /**
     * 根据accountId 更新仓位数据
     * @param accountId
     * @return
     */
    public static HashSet<PositionWsData> updatePositionByAccountId(Long accountId,HashSet<PositionWsData> positions) {
        return positionStore.compute(accountId, (k, v) -> positions);
    }


    /**
     * 添加channel
     * @param exchange
     * @param symbol
     * @param channel
     */
    public static void addChannel(String exchange, String symbol, Channel channel) {
        String key = getKey(exchange, symbol);
        channel.attr(EXCHANGE_KEY).set(exchange);
        channel.attr(SYMBOL_KEY).set(symbol);
        channelGroup.computeIfAbsent(key, k -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE))
                .add(channel);
    }
    public static void removeChannel(Channel channel) {

    }
    public static void removeChannel(String exchange, String symbol, Channel channel) {
        String key = getKey(exchange, symbol);
        ChannelGroup channels = channelGroup.get(key);
        if (Objects.nonNull(channels)) {
            channels.remove(channel);
            //如果没有人监听了，那么就直接关闭
            if (channels.isEmpty()) {
                channels.close();
                channelGroup.remove(key);
            }
        }

    }
    private static String getKey(String exchange, String symbol) {
        return exchange + ":" + symbol;
    }
    private static String getDisruptorKey(Long userId, String taskId) {
        return userId + ":" + taskId;
    }
    public static List<ABDisruptor> getByUserId(Long userId) {
        return priceListenerStore.values().stream()
                .filter(disruptor -> Objects.equals(disruptor.getUserId(), userId))
                .toList();
    }

    public synchronized static ABDisruptor getPriceListener(ABOrderTask abOrderTask) {
        String key = getDisruptorKey(abOrderTask.getUserId() ,abOrderTask.getTaskId());
        //检查ab 是否是同一个account
        List<ABDisruptor> byUserId = getByUserId(abOrderTask.getUserId());
        ABDisruptor existsDisruptor = checkExists(byUserId, abOrderTask);
        if(Objects.nonNull(existsDisruptor)) {
            String taskId = existsDisruptor.getTaskId();
            key = getDisruptorKey(abOrderTask.getUserId() ,taskId);
            priceListenerStore.put(key, existsDisruptor);
            return existsDisruptor;
        }
        return priceListenerStore.computeIfAbsent(key, k -> {
            MarketPriceDisruptor disruptorA = new MarketPriceDisruptor(1024,abOrderTask.getMarketABPriceEventHandler());
            disruptorA.setExchangeName(abOrderTask.getExchangeA());
            disruptorA.setSymbol(abOrderTask.getSymbolA());
            disruptorA.setSide(1);
            MarketPriceDisruptor disruptorB = new MarketPriceDisruptor(1024,abOrderTask.getMarketABPriceEventHandler());
            disruptorB.setExchangeName(abOrderTask.getExchangeB());
            disruptorB.setSymbol(abOrderTask.getSymbolB());
            disruptorB.setSide(2);
            ABDisruptor disruptor = new ABDisruptor(disruptorA, disruptorB,abOrderTask);
            return disruptor;
        });
    }

    public synchronized static ABDisruptor stopPriceListener(ABOrderTask abOrderTask) {
        String key = getDisruptorKey(abOrderTask.getUserId() ,abOrderTask.getTaskId());
        //检查ab 是否是同一个account
        List<ABDisruptor> byUserId = getByUserId(abOrderTask.getUserId());
        ABDisruptor existsDisruptor = checkExists(byUserId, abOrderTask);
        if(Objects.nonNull(existsDisruptor)) {
            priceListenerStore.put(key, existsDisruptor);
            return existsDisruptor;
        }
        return null;
    }

    private static ABDisruptor checkExists(List<ABDisruptor> byUserId, ABOrderTask abOrderTask) {
        HashSet<Long> currAccountIds = new HashSet<>();
        currAccountIds.add(abOrderTask.getAccountA().getId());
        currAccountIds.add(abOrderTask.getAccountB().getId());

        for (ABDisruptor disruptor : byUserId) {
            Long aId = disruptor.getAbOrderTask().getAccountA().getId();
            Long bId = disruptor.getAbOrderTask().getAccountB().getId();

            if(currAccountIds.contains(aId) && currAccountIds.contains(bId)) {
                return disruptor;
            }
        }
        return null;
    }

//    public static ABDisruptor getDisruptorByTaskId(String taskId) {
//        //5*60*60*24 一天的数据量是 432000
//        return priceListenerStore.getOrDefault(taskId,null);
//    }


    public static void CloseABDisruptor(ABOrderTask abOrderTask) {
        String key = getDisruptorKey(abOrderTask.getUserId() ,abOrderTask.getTaskId());
//        List<ABDisruptor> byUserId = getByUserId(abOrderTask.getUserId());
//        ABDisruptor existsDisruptor = checkExists(byUserId, abOrderTask);

        ABDisruptor remove = priceListenerStore.remove(key);
        if (Objects.nonNull(remove)) {
            remove.shutdown();
        }
    }

    /**
     * 更新账户余额
     * @param accountId
     * @param accountBalance
     */
    public static void updateAccountByAccountId(Long accountId, AccountBalance accountBalance) {
        accountBalanceStore.compute(accountId, (k, v) -> accountBalance);
    }

    /**
     * 查询账户余额
     * @param accountId
     * @return
     */
    public static AccountBalance getAccountByAccountId(Long accountId) {
        AccountBalance accountBalance = accountBalanceStore.get(accountId);
        if (Objects.nonNull(accountBalance)) {
            return accountBalance;
        }
        AckAccountLoadEvent ackAccountLoadEvent = new AckAccountLoadEvent();
        ackAccountLoadEvent.setAccountId(accountId);
        SpringUtils.getApplicationContext().publishEvent(ackAccountLoadEvent);
        return null;
    }

    public static List<ABDisruptor> getPriceListenerList(Long userId) {
        return getByUserId(userId);
    }
}
