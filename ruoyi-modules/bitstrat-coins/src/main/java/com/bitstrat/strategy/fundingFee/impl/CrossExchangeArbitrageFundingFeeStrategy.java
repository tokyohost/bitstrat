package com.bitstrat.strategy.fundingFee.impl;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.CoinsConstant;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.FundFeeTask;
import com.bitstrat.domain.TaskBalance;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CreateArbitrageTaskVo;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.strategy.fundingFee.FundingFeeStrategy;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.FundingFeeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author caoyang
 * @date 2025-05-04
 */
@Component
@Slf4j
public class CrossExchangeArbitrageFundingFeeStrategy implements FundingFeeStrategy {
    private final CommonServce commonServce;

    @Autowired
    private ICoinsApiService coinsApiService;

    @Autowired
    private ExchangeApiManager exchangeApiManager;

    public CrossExchangeArbitrageFundingFeeStrategy(CommonServce commonServce) {
        this.commonServce = commonServce;
    }

    /**
     * 资金费率
     * 负数是空头给多头钱
     * 正数是多头给空头钱
     *
     * @param task
     * @return
     */
    @Override
    public boolean isFundingFeeInverted(CoinsCrossExchangeArbitrageTaskVo task) {
        // 获取做多和做空的资金费率
        SymbolFundingRate shortSymbolFundingRate = (SymbolFundingRate) commonServce
            .querySymbolFundingRate(task.getShortEx(), task.getSymbol()).getData();
        SymbolFundingRate longSymbolFundingRate = (SymbolFundingRate) commonServce
            .querySymbolFundingRate(task.getLongEx(), task.getSymbol()).getData();
        // 做多
        BigDecimal longFundingFee = longSymbolFundingRate.getFundingRate();
        // 做空
        BigDecimal shortFundingFee = shortSymbolFundingRate.getFundingRate();
        // 比较调整后的资金费
        BigDecimal canMakeFee = FundingFeeUtils.checkFunding(longFundingFee, shortFundingFee);
        BigDecimal compare = canMakeFee.multiply(new BigDecimal(100)).subtract(task.getWarningThreshold());
        return compare.compareTo(BigDecimal.ZERO) < 0;

//        return longFundingFee.add(shortFundingFee).compareTo(task.getWarningThreshold()) <= 0;
    }

    @Override
    public FundFeeTask queryFundingFeeByTask(CoinsCrossExchangeArbitrageTaskVo task) {
        SymbolFundingRate shortSymbolFundingRate = (SymbolFundingRate) commonServce
            .querySymbolFundingRate(task.getShortEx(), task.getSymbol()).getData();
        SymbolFundingRate longSymbolFundingRate = (SymbolFundingRate) commonServce
            .querySymbolFundingRate(task.getLongEx(), task.getSymbol()).getData();
        // 做多
        BigDecimal longFundingFee = longSymbolFundingRate.getFundingRate();
        // 做空
        BigDecimal shortFundingFee = shortSymbolFundingRate.getFundingRate();
        BigDecimal canMake = FundingFeeUtils.checkFunding(longFundingFee, shortFundingFee);
        FundFeeTask fundFeeTask = new FundFeeTask();
        fundFeeTask.setLongFundingFee(longFundingFee);
        LocalDateTime longfundingtime = Instant.ofEpochMilli(longSymbolFundingRate.getNextFundingTime())
            .atZone(ZoneId.systemDefault())  // 使用系统默认时区
            .toLocalDateTime();
        LocalDateTime shortfundingtime = Instant.ofEpochMilli(shortSymbolFundingRate.getNextFundingTime())
            .atZone(ZoneId.systemDefault())  // 使用系统默认时区
            .toLocalDateTime();
        fundFeeTask.setLongNextFundingTime(longfundingtime);
        fundFeeTask.setShortNextFundingTime(shortfundingtime);
        fundFeeTask.setShortFundingFee(shortFundingFee);
        fundFeeTask.setFundingFeeCanMake(canMake);
        String role = task.getRole();
        //计算年化
        CreateArbitrageTaskVo createArbitrageTaskVo = JSONObject.parseObject(role, CreateArbitrageTaskVo.class);
        CoinFundingInfo argitrageData = createArbitrageTaskVo.getArgitrageData();
        Double buyfundingIntervalHours = argitrageData.getBuy().getFundingIntervalHours();
        Double sellfundingIntervalHours = argitrageData.getSell().getFundingIntervalHours();
        BigDecimal buyHours = new BigDecimal(buyfundingIntervalHours);
        BigDecimal sellHours = new BigDecimal(sellfundingIntervalHours);
        //根据做多资金费率、结算时间和做空资金费率、结算时间，计算年华收益率
        BigDecimal longPreHours = longFundingFee.divide(buyHours, 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal shortPreHours = shortFundingFee.divide(sellHours, 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal canMakePreHours = FundingFeeUtils.checkFunding(longPreHours, shortPreHours);
        BigDecimal apy = canMakePreHours.multiply(new BigDecimal(24)).multiply(new BigDecimal(365));
        fundFeeTask.setApy(apy);

        fundFeeTask = queryPrice(fundFeeTask,task);
        return fundFeeTask;

    }

    private FundFeeTask queryPrice(FundFeeTask fundFeeTask,CoinsCrossExchangeArbitrageTaskVo task) {
        ExchangeService longService = exchangeApiManager.getExchangeService(task.getLongEx());
        ExchangeService shortService = exchangeApiManager.getExchangeService(task.getShortEx());
        BigDecimal longPrice = longService.getNowPrice(null, task.getSymbol());
        BigDecimal shortPrice = shortService.getNowPrice(null, task.getSymbol());
        fundFeeTask.setLongMarketPrice(longPrice);
        fundFeeTask.setShortMarketPrice(shortPrice);
        return fundFeeTask;
    }

    @Override
    public TaskBalance queryBalanceByTask(CoinsCrossExchangeArbitrageTaskVo task) {
        Long userId = task.getUserId();
        CoinsApiVo longexApi = coinsApiService.queryByUserAndId(userId, task.getLongAccountId());
        CoinsApiVo shortexApi = coinsApiService.queryByUserAndId(userId, task.getShortAccountId());

        Account longAccount = AccountUtils.coverToAccount(longexApi);
        Account shortAccount = AccountUtils.coverToAccount(shortexApi);

        ExchangeService longService = exchangeApiManager.getExchangeService(task.getLongEx());
        ExchangeService shortService = exchangeApiManager.getExchangeService(task.getShortEx());

        CompletableFuture<AccountBalance> longFuture = CompletableFuture.supplyAsync(() -> {
            try {
                AccountBalance balance = longService.getBalance(longAccount, CoinsConstant.USDT);
                return balance;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        CompletableFuture<AccountBalance> shortFuture = CompletableFuture.supplyAsync(() -> {
            try {
                AccountBalance balance = shortService.getBalance(shortAccount, CoinsConstant.USDT);
                return balance;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        });

        TaskBalance taskBalance = new TaskBalance();
        AccountBalance longbalance = longFuture.join();

        AccountBalance shortbalance = shortFuture.join();
        if(Objects.nonNull(longbalance)) {
            taskBalance.setLongBalance(longbalance.getEquity());
        }
        if(Objects.nonNull(shortbalance)) {
            taskBalance.setShortBalance(shortbalance.getEquity());
        }

        return taskBalance;
    }
}
