package com.bitstrat.domain;

import com.bitstrat.domain.binance.BinanceOrderDetail;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/20 18:37
 * @Content
 */
@Data
public class SyncOrderDetail {
    BinanceOrderDetail binanceOrderDetail;
}
