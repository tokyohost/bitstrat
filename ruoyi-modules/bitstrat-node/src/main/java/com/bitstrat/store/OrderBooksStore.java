package com.bitstrat.store;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.OrderBook;
import com.bybit.api.client.domain.websocket_message.public_channel.PublicOrderBookData;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 18:50
 * @Content
 */

public class OrderBooksStore {
    private final static ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, OrderBook> getOrderBooks() {
        return orderBooks;
    }

    public static void  initOrderBook(PublicOrderBookData data, String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        if(orderBook == null) {
            synchronized(orderBooks) {
                orderBook = orderBooks.get(symbol);
                if(orderBook == null) {
                    orderBook = new OrderBook();
                    orderBooks.put(symbol,orderBook);
                }
            }
        }
        ConcurrentSkipListMap<BigDecimal, BigDecimal> asks = orderBook.getAsks();
        ConcurrentSkipListMap<BigDecimal, BigDecimal> bids = orderBook.getBids();
        for (List<String> bid : data.getB()) {
            BigDecimal price = new BigDecimal(bid.get(0));
            BigDecimal size = new BigDecimal(bid.get(1));
            if (size.compareTo(BigDecimal.ZERO) > 0) {
                bids.put(price, size);
            }
        }
        for (List<String> ask : data.getA()) {
            BigDecimal price = new BigDecimal(ask.get(0));
            BigDecimal size = new BigDecimal(ask.get(1));
            if (size.compareTo(BigDecimal.ZERO) > 0) {
                asks.put(price, size);
            }
        }
    }

    /**
     * 如果 imbalance ≫ 0：买单比卖单多 → 很可能有人要大买
     *
     * 如果 imbalance ≪ 0：卖压大 → 可能有人要大卖
     * @param symbol
     * @return
     */
    public static Double direction(String symbol) {
        OrderBook orderBook = orderBooks.getOrDefault(symbol,new OrderBook());
        ConcurrentSkipListMap<BigDecimal, BigDecimal> asks = orderBook.getAsks();
        ConcurrentSkipListMap<BigDecimal, BigDecimal> bids = orderBook.getBids();
        BigDecimal totalBids = bids.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAsks = asks.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal imbalance = totalBids.subtract(totalAsks);
        return imbalance.doubleValue();
    }

    public static void updateOrderBook(PublicOrderBookData data,String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);

        if(orderBook == null) {
            synchronized(orderBooks) {
                orderBook = orderBooks.get(symbol);
                if(orderBook == null) {
                    orderBook = new OrderBook();
                    orderBooks.put(symbol,orderBook);
                }
            }
        }

        ConcurrentSkipListMap<BigDecimal, BigDecimal> asks = orderBook.getAsks();
        ConcurrentSkipListMap<BigDecimal, BigDecimal> bids = orderBook.getBids();

        for (List<String> bid : data.getB()) {
            BigDecimal price = new BigDecimal(bid.get(0));
            BigDecimal size = new BigDecimal(bid.get(1));
            if (size.compareTo(BigDecimal.ZERO) == 0) {
                bids.remove(price);
            } else {
                bids.put(price, size);
            }
        }

        for (List<String> ask : data.getA()) {
            BigDecimal price = new BigDecimal(ask.get(0));
            BigDecimal size = new BigDecimal(ask.get(1));
            if (size.compareTo(BigDecimal.ZERO) == 0) {
                asks.remove(price);
            } else {
                asks.put(price, size);
            }
        }
    }
}
