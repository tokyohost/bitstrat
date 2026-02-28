package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 18:47
 * @Content
 */

@Data
public class OrderBook {
    ConcurrentSkipListMap<BigDecimal, BigDecimal> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    ConcurrentSkipListMap<BigDecimal, BigDecimal> asks = new ConcurrentSkipListMap<>();
}
