package com.bitstrat.store;

import com.bitstrat.domain.msg.ActiveLossPoint;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 11:12
 * @Content
 */
@Slf4j
public class RoleCenter {
    ConcurrentHashMap<String, ActiveLossPoint> symbolLossPointMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, List<ActiveLossPoint>> nodeLossPointMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, List<ActiveLossPoint>> activeLossPoints = new ConcurrentHashMap<>();
    private ActiveLossPoint searchKey = new ActiveLossPoint();
    public RoleCenter() {


    }

    public synchronized void clear() {
        symbolLossPointMap.clear();
        activeLossPoints.clear();
        nodeLossPointMap.clear();
    }

    public ConcurrentHashMap<String, List<ActiveLossPoint>> getNodeLossPointMap() {
        return nodeLossPointMap;
    }

    public ConcurrentHashMap<String, ActiveLossPoint> getSymbolLossPointMap() {
        log.debug("getSymbolLossPointMap");
        return symbolLossPointMap;
    }

    public List<ActiveLossPoint> getSymbolLossPoint(String symbol, String exchangeName) {
        ArrayList<ActiveLossPoint> lpoints = new ArrayList<>();
        Enumeration<String> keys = symbolLossPointMap.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(getKey(symbol,exchangeName,0L))) {
                ActiveLossPoint activeLossPoint = symbolLossPointMap.get(key);
                lpoints.add(activeLossPoint);
            }
        }
        return lpoints;
    }

    public String getKey(String exchangeName,String symbol, Long lossPointId) {
        if (Objects.nonNull(lossPointId)) {
            return exchangeName+ ":" + symbol +":" + lossPointId;
        }else{
            return exchangeName+":" + symbol;
        }

    }

    public void put(String exchangeName, String symbol,Long lossPointId, String clientId, ActiveLossPoint activeLossPoint) {
        String key = this.getKey(exchangeName, symbol,lossPointId);
        activeLossPoint.setNodeName(clientId);
        symbolLossPointMap.put(key, activeLossPoint);
        //删除滑点缓存
        activeLossPoints.remove(exchangeName+":"+symbol);
    }

    /**
     * 获取所有任务需要监听的币对
     * @return
     */
    public HashSet<String> getSymbols() {
        HashSet<String> symbols = new HashSet<>();
        for (String key : symbolLossPointMap.keySet()) {
            String[] split = key.split(":");
            String symbol = split[1];
            symbols.add(symbol);
        }
        return symbols;
    }

    /**
     * 根据实时价格找到预设滑点最近的一个滑点
     * @param lastPrice
     * @param symbol
     * @return
     */
    public ActiveLossPoint getTraggerPrice(String exchangeName,Double lastPrice, String symbol) {
        String cacheKey = exchangeName+":"+symbol;
        if(activeLossPoints.containsKey(cacheKey)){
            //存在排序缓存，直接使用
            return matcherPrice(activeLossPoints.get(cacheKey),lastPrice);

        }else{
            synchronized (activeLossPoints) {
                if(activeLossPoints.containsKey(exchangeName+":"+symbol)){
                    //已加载好了，直接用
                    return matcherPrice(activeLossPoints.get(cacheKey),lastPrice);
                }
                //创建缓存
                List<ActiveLossPoint> lpoints = new ArrayList<>();
                for (ActiveLossPoint value : symbolLossPointMap.values()) {
                    if (value.getSymbol().equalsIgnoreCase(symbol)) {
                        lpoints.add(value);
                    }
                }
                // 按 price 排序（可提前排好）
                lpoints.sort(Comparator.comparing(ActiveLossPoint::getPrice));
                activeLossPoints.put(exchangeName+":"+symbol, lpoints);
            }
            return matcherPrice(activeLossPoints.get(cacheKey),lastPrice);
        }
    }

    private ActiveLossPoint matcherPrice(List<ActiveLossPoint> activeLossPoints, Double lastPrice) {
        BigDecimal currPrice = BigDecimal.valueOf(lastPrice);
        searchKey.setPrice(currPrice);
        // 使用二分查找定位插入点
        int index = Collections.binarySearch(
            activeLossPoints,
            searchKey,
            Comparator.comparing(ActiveLossPoint::getPrice)
        );

        if (index >= 0) {
//            ("当前价格精确命中滑点：{}",lastPrice);
            ActiveLossPoint activeLossPoint = activeLossPoints.get(index);
            return activeLossPoint;
        } else {
            if(activeLossPoints.size() == 0){
                log.info("没有滑点设置");
                return null;
            }
            int insertPoint = -index - 1;

            if (insertPoint == 0) {
               log.debug("当前价格低于所有滑点，最小滑点：{}",activeLossPoints.get(0).getPrice());
               return activeLossPoints.get(0);
            } else if (insertPoint == activeLossPoints.size()) {
                log.debug("当前价格高于所有滑点，最大滑点：" + activeLossPoints.get(activeLossPoints.size() - 1).getPrice());
                return activeLossPoints.get(activeLossPoints.size() - 1);
            } else {
                ActiveLossPoint lower = activeLossPoints.get(insertPoint - 1);
                ActiveLossPoint upper = activeLossPoints.get(insertPoint);

                log.debug("当前价格位于滑点区间： 上: {}  下:{}",upper.getPrice(),lower.getPrice());


                // 找出最近的滑点
                BigDecimal diffLower = currPrice.subtract(lower.getPrice()).abs();
                BigDecimal diffUpper = currPrice.subtract(upper.getPrice()).abs();
                ActiveLossPoint closest = diffLower.compareTo(diffUpper) <= 0 ? lower : upper;

                return closest;
            }
        }
    }
}
