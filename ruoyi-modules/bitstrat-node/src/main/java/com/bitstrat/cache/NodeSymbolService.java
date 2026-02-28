package com.bitstrat.cache;

import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class NodeSymbolService {

    private final RedissonClient redissonClient;

    public NodeSymbolService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 向指定节点添加 symbol，构建双向索引
     */
    public void addSymbolToNode(String nodeId, String symbol) {
        String normSymbol = symbol.toLowerCase();
        String nodeKey = buildNodeKey(nodeId);
        String symbolKey = buildSymbolKey(normSymbol);

        redissonClient.getSet(nodeKey).add(normSymbol);
        redissonClient.getSet(symbolKey).add(nodeId);
    }
    /**
     * 删除某个 symbol 的所有监听记录：
     * - 从所有 node:<id> 中移除该 symbol
     * - 删除 symbol:<symbol> 的反向索引
     */
    public void deleteSymbol(String symbol) {
        String normSymbol = symbol.toLowerCase();
        String symbolKey = buildSymbolKey(normSymbol);
        RSet<String> nodeSet = redissonClient.getSet(symbolKey);

        Set<String> nodeIds = nodeSet.readAll();

        // 从每个 node 对应的 set 中删除该 symbol
        for (String nodeId : nodeIds) {
            RSet<String> symbols = redissonClient.getSet(buildNodeKey(nodeId));
            symbols.remove(normSymbol);

            // 可选：如果这个节点已没有任何 symbol，可以清理
            if (symbols.isEmpty()) {
                symbols.delete();
            }
        }

        // 删除 symbol:<symbol> 的集合本身
        nodeSet.delete();
    }
    /**
     * 获取某个节点当前订阅的所有 symbol
     */
    public Set<String> getSymbolsByNode(String nodeId) {
        RSet<String> set = redissonClient.getSet(buildNodeKey(nodeId));
        return set != null ? set.readAll() : Collections.emptySet();
    }

    /**
     * 获取正在监听某个 symbol 的所有节点
     */
    public Set<String> getNodesBySymbol(String symbol) {
        RSet<String> set = redissonClient.getSet(buildSymbolKey(symbol.toLowerCase()));
        return set != null ? set.readAll() : Collections.emptySet();
    }

    /**
     * 删除指定节点所有 symbol，并清理 symbol -> node 的反向索引
     */
    public void deleteAllSymbolsByNode(String nodeId) {
        String nodeKey = buildNodeKey(nodeId);
        RSet<String> symbolSet = redissonClient.getSet(nodeKey);

        for (String symbol : symbolSet.readAll()) {
            RSet<String> symbolNodeSet = redissonClient.getSet(buildSymbolKey(symbol));
            symbolNodeSet.remove(nodeId);

            // 可选：如果 symbol 已无人订阅，可自动删除
            if (symbolNodeSet.isEmpty()) {
                symbolNodeSet.delete();
            }
        }

        symbolSet.delete();
    }

    private String buildNodeKey(String nodeId) {
        return "node:" + nodeId;
    }

    private String buildSymbolKey(String symbol) {
        return "symbol:" + symbol;
    }
}
