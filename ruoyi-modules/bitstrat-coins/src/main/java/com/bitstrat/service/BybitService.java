package com.bitstrat.service;


import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.bybit.BybitSymbol;
import com.bitstrat.domain.bybit.BybitSymbolInfo;

import java.util.List;

/**
 * @author tokyohostcoder
 * @Version 1.0
 * @date 2025/4/1 10:16
 * @Content
 */

public interface BybitService {


    List<BybitSymbol> getSymbols();
    List<BybitSymbolInfo> getSymbolsLiner();

    String buy(String symbol, Double singleOrder, ByBitAccount byBitAccount);

    String sell(String symbol, Double singleOrder, ByBitAccount byBitAccount);
}
