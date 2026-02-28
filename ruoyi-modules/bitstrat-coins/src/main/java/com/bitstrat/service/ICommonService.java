package com.bitstrat.service;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.LinerSymbol;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.bybit.StrategyConfig;
import com.bitstrat.domain.vo.*;
import com.bybit.api.client.domain.CategoryType;
import org.dromara.common.core.domain.R;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/14 14:42
 * @Content
 */

public interface ICommonService {
    public Double getMinOrderAmt();

    public CategoryType getCateType();
    public ByBitAccount getByBitAccount();
    public ExecutorService getSyncExecutorService();
    public ScheduledExecutorService getScheduler();

    public List<Account> getUserAccountByExchange(String exchangeName);
    public R setLeverageBody(SetLeverageBody body);

    public void doOrderSubscript(String exchangeName, String clientId, CoinsLossPointVo coinsLossPointVo);
    public JSONObject queryTickers(String symbol, ByBitAccount account);
    public JSONObject getMarketInfo(ByBitAccount account, String symbol);

    public boolean checkColdSec(Date lastOrderTime, Long coldSec);
    public ConcurrentHashMap<Long, ScheduledFuture<?>> getTaskSchedulerMap();
    public ConcurrentHashMap<Long, ScheduledFuture<?>> getSyncTaskSchedulerMap();
    public ConcurrentHashMap<String, ScheduledFuture<?>> getSchedulerMap();
    public JSONObject queryPositionSpot(String symbol, ByBitAccount byBitAccount);
    public JSONObject queryPosition(String symbol, ByBitAccount byBitAccount);
    public JSONObject queryOrderStatus(String orderId, ByBitAccount byBitAccount);
    public StrategyConfig parsRole(String strategyConfig);
    public BarSeries getBarSeries(List<List<BigDecimal>> klines, String symbol, Long interval);
    public List<List<BigDecimal>> getKlinesData(ByBitAccount byBitAccount, String symbol, String interval, Long start, Long end);
    public List<List<BigDecimal>> getKlinesData(ByBitAccount byBitAccount, String symbol, String interval, Long size);
    public List<List<BigDecimal>> getKlinesData(ByBitAccount byBitAccount, String symbol, String interval);


    ByBitAccount getBybitUserAccountByExchange(Long createBy);

    R queryBalanceByEx(CoinsApi api,QueryBalanceBody body);

    R queryFeeByExSymbol(CoinsApi coinsApi, QueryFeeBody body);

    List<Account> getUserAccountByNodeClient(String clientId);

    R querySymbolFundingRate(String exchange, String symbol);

    R querySymbolContractInfo(String exchange, String symbol,Long accountId);

    R querySymbolMarketPrice(String exchange, String symbol);

    List<LinerSymbol> getAllLinerSymbolByEx(String ex);

    Integer getSymbolFundingTimeIntervalByEx(String ex,String symbol);

    void createWebsocketConnections(List<String> exchanges);

    void createWebsocketConnections(String exchange, Long accountId);
    void createWebsocketConnections(String exchange, Long accountId,Long userId);
}
