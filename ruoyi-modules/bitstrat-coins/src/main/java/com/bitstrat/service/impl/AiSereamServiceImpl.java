package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.RedisConstant;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.TickerItem;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.service.AiSereamService;
import com.bitstrat.service.ICoinApiPositionService;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.CalcUtils;
import com.bitstrat.utils.PromptUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:55
 * @Content
 */

@Service
@Slf4j
public class AiSereamServiceImpl implements AiSereamService {

    @Autowired
    private  ExchangeApiManager exchangeApiManager;

    @Autowired
    private  ICoinsApiService coinsApiService;
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SneakyThrows
    @Override
    public String queryKLinePrompt(String exchange, List<String> coinList,AiStreamQuery aiStreamQuery) {
        Long accountId = aiStreamQuery.getAccountId();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(accountId);
        if(Objects.isNull(coinsApiVo)){
            throw new RuntimeException("请选择API");
        }
        Account account = AccountUtils.coverToAccount(coinsApiVo);
        ExchangeType exchangeType = ExchangeType.getExchangeType(exchange);
        if (Objects.isNull(exchangeType)) {
            throw new RuntimeException("不支持的交易所 " + exchange);
        }
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
        ArrayList<CompletableFuture<MarketData>> futures = new ArrayList<>();
        for (String coin : coinList) {
            CompletableFuture<MarketData> future = CompletableFuture.supplyAsync(() -> {
                TickerItem nowPrice = exchangeService.getNowPrice(null, coin, "");
                if (Objects.isNull(nowPrice)) {
                    return null;
                }
                String shortGranularity = "5m";
                String longGranularity = "4H";
                if (StringUtils.isNotEmpty(aiStreamQuery.getShortTermInterval())) {
                    shortGranularity = aiStreamQuery.getShortTermInterval();
                }
                if(StringUtils.isNotEmpty(aiStreamQuery.getLongTermInterval())){
                    longGranularity = aiStreamQuery.getLongTermInterval();
                }

                JSONArray marketCandlesShort = exchangeService.getMarketCandles(account, coin, shortGranularity, 50L);
                BarSeries seriesShort = PromptUtils.getBarSeries(coin, marketCandlesShort);
                JSONArray marketCandlesLong = exchangeService.getMarketCandles(account, coin, longGranularity, 50L);
                BarSeries seriesLong = PromptUtils.getBarSeries(coin, marketCandlesLong);


                // 资金费
                SymbolFundingRate symbolFundingRate = exchangeService.getSymbolFundingRate(coin);
                //总持仓量
                BigDecimal openInterest = exchangeService.getOpenInterest(account, coin);

                TermData shortTermData = CalcUtils.calcShortTerm(seriesShort, 40);

                TermData longerTermData = CalcUtils.calcLongerTerm(seriesLong, 40);

                MarketData marketData = new MarketData();
                marketData.setCurrentPrice(nowPrice.getMarkPrice());
                if (Objects.nonNull(nowPrice.getChange24H())) {
                    marketData.setChange24H(nowPrice.getChange24H().multiply(BigDecimal.valueOf(100)).toPlainString() + "%");
                } else {
                    marketData.setChange24H("Unknow");
                }
                marketData.setShortTerm(shortTermData);
                marketData.setLongTerm(longerTermData);
                marketData.setFundingRate(symbolFundingRate);
                marketData.setOpenInterest(openInterest);
                marketData.setSymbol(coin);

                return marketData;
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        ArrayList<MarketData> marketDatas = new ArrayList<>();
        for (CompletableFuture<MarketData> future : futures) {
            MarketData data = future.get();
            marketDatas.add(data);
        }

        if ("1".equalsIgnoreCase(aiStreamQuery.getPositionFlag())) {
            List<HistoryPosition> historyPositions = exchangeService.queryContractHistoryPosition(account, 20L,new HistoryPositionQuery());
            List<PositionWsData> positionWsData = exchangeService.queryContractPositionDetail(account, new PositionParams());
            AccountBalance balance = exchangeService.getBalance(account, "USDT");
            log.info("当前持仓:{}", JSONObject.toJSONString(positionWsData, JSONWriter.Feature.PrettyFormatWith2Space));
            JSONArray positions = new JSONArray();
            for (PositionWsData positionWsDatum : positionWsData) {
                JSONObject position = new JSONObject();
                position.put("symbol", positionWsDatum.getSymbol());
                position.put("size", exchangeService.calcShowSize(positionWsDatum.getSymbol(), positionWsDatum.getSize()) + positionWsDatum.getSymbol());
                position.put("open", positionWsDatum.getAvgPrice());
                position.put("unrealizedPL", positionWsDatum.getUnrealizedPL() + "USDT");
                position.put("achievedProfits", positionWsDatum.getAchievedProfits() + "USDT");
                position.put("totalFee", (Objects.isNull(positionWsDatum.getTotalFee()) ? BigDecimal.ZERO : positionWsDatum.getTotalFee().abs()) + "USDT");
//            position.put("liquidationPrice",positionWsDatum.getLiqPrice());
//            position.put("keepMarginRate",positionWsDatum.getKeepMarginRate());
                position.put("marginSize", (Objects.isNull(positionWsDatum.getMarginPrice()) ? BigDecimal.ZERO : positionWsDatum.getMarginPrice()) + "USDT");
                position.put("createPositionTime", simpleDateFormat.format(positionWsDatum.getCreateTime()));
                position.put("stopLoss", positionWsDatum.getStopLoss());
                position.put("takeProfit", positionWsDatum.getTakeProfit());


                position.put("side", positionWsDatum.getHoldSide());
                position.put("leverage", positionWsDatum.getLeverage());

                positions.add(position);
            }
            return getUserPrompt(marketDatas, positions, historyPositions, aiStreamQuery,balance);
        }else{
            return getUserPrompt(marketDatas, null, null, aiStreamQuery,null);
        }
    }



    private String getUserPrompt(List<MarketData> marketDatas, JSONArray positions
        ,List<HistoryPosition> historyPositions, AiStreamQuery aiStreamQuery,AccountBalance balance) {
        StringBuilder userPrompt = new StringBuilder();
        String prompt_1 = """
            Current Time is :{currentTime}  We are providing you with a variety of state data, price data, and predictive signals so you can discover alpha. etc.
            """;
        prompt_1 = prompt_1.replace("{currentTime}", dateTimeFormatter.format(LocalDateTime.now()));
        userPrompt.append(prompt_1).append("\n");
        userPrompt.append("""
            ALL OF THE PRICE OR SIGNAL DATA BELOW IS ORDERED: OLDEST → NEWEST
            """).append("\n");
        userPrompt.append("""
            Timeframes note: Unless stated otherwise in a section title, intraday series are provided at 3‑minute intervals. If a coin uses a different interval, it is explicitly stated in that coin’s section.
            """).append("\n");
        userPrompt.append("""
            CURRENT MARKET STATE FOR ALL COINS
            """).append("\n");

        for (MarketData marketData : marketDatas) {
            TermData shortTerm = marketData.getShortTerm();
            TermData longTerm = marketData.getLongTerm();
            userPrompt.append("ALL " + marketData.getSymbol().toUpperCase() + " DATA").append("\n");
            String dataPath1 = """
                current_price = {current_price}, current_ema20 = {current_ema20}, current_macd = {current_macd}, current_rsi (7 period) = {current_rsi7}
                """;
            dataPath1 = dataPath1.replace("{current_price}", marketData.getCurrentPrice());
            dataPath1 = dataPath1.replace("{current_ema20}", shortTerm.getEma20Value());
            dataPath1 = dataPath1.replace("{current_macd}", shortTerm.getMacdValue());
            dataPath1 = dataPath1.replace("{current_rsi7}", shortTerm.getRsi7Value());
            userPrompt.append(dataPath1).append("\n");
            userPrompt.append("""
                In addition, here is the latest BTC open interest and funding rate for perps (the instrument you are trading):
                """).append("\n");
            String dataPath2 = """
                Open Interest: Latest: {openInterest}
                """;
            dataPath2 = dataPath2.replace("{openInterest}", marketData.getOpenInterest().toPlainString());
            userPrompt.append(dataPath2).append("\n");
            String dataPath3 = """
                Funding Rate: {fundingRate}
                """;
            dataPath3 = dataPath3.replace("{fundingRate}", marketData.getFundingRate().getFundingRate().toPlainString());
            userPrompt.append(dataPath3).append("\n");
            String dataPath3Title = """
                Intraday series (by {shortInterval}, oldest → latest):
                """;
            if (StringUtils.isNotEmpty(aiStreamQuery.getShortTermInterval())) {
                dataPath3Title = dataPath3Title.replace("{shortInterval}", PromptUtils.convertInterval(aiStreamQuery.getShortTermInterval()));
            }else{
                dataPath3Title = dataPath3Title.replace("{shortInterval}", "5-minute");
            }
            userPrompt.append(dataPath3Title).append("\n");
            String datapath4 = """
                Mid prices: {Mid}
                """;
            List<Double> midPrices = shortTerm.getMidPrices();
            datapath4 = datapath4.replace("{Mid}", midPrices.toString());
            userPrompt.append(datapath4).append("\n");
            String datapathEMAIndicators = """
                EMA indicators (20‑period): {ema20period}
                """;
            datapathEMAIndicators = datapathEMAIndicators.replace("{ema20period}", shortTerm.getEma20periods().toString());
            userPrompt.append(datapathEMAIndicators).append("\n");
            String dataPathMACDIndicators = """
                MACD indicators: {macdIndicators}
                """;
            dataPathMACDIndicators = dataPathMACDIndicators.replace("{macdIndicators}", shortTerm.getMacdIndicatorsString().toString());
            userPrompt.append(dataPathMACDIndicators).append("\n");
            String dataPathRSIIndicators = """
                RSI indicators (7‑Period): {rsi7period}
                """;
            dataPathRSIIndicators = dataPathRSIIndicators.replace("{rsi7period}", shortTerm.getRsi7period().toString());
            userPrompt.append(dataPathRSIIndicators).append("\n");

            String dataPathRSIndicators14 = """
                RSI indicators (14‑Period): {rsi14period}
                """;
            dataPathRSIndicators14 = dataPathRSIndicators14.replace("{rsi14period}", shortTerm.getRsi14period().toString());
            userPrompt.append(dataPathRSIndicators14).append("\n");
            String dataPathTitle5 = """
                Longer‑term context ({longInterval} timeframe):
                """;
            if(StringUtils.isNoneEmpty(aiStreamQuery.getLongTermInterval())){
                dataPathTitle5 = dataPathTitle5.replace("{longInterval}", PromptUtils.convertInterval(aiStreamQuery.getLongTermInterval()));
            }else{
                dataPathTitle5 = dataPathTitle5.replace("{longInterval}", "4-hour");
            }
            userPrompt.append("\n").append(dataPathTitle5).append("\n");
            String dataPathLonggerEMA = """
                20‑Period EMA: {20PeriodEMA} vs. 50‑Period EMA: {50PeriodEMA}
                """;
            dataPathLonggerEMA = dataPathLonggerEMA.replace("{20PeriodEMA}", longTerm.getEma20Value());
            dataPathLonggerEMA = dataPathLonggerEMA.replace("{50PeriodEMA}", longTerm.getEma50Value());
            userPrompt.append(dataPathLonggerEMA).append("\n");

            String dataPathATR = """
                3‑Period ATR: {ATR3} vs. 14‑Period ATR: {ATR14}
                """;
            dataPathATR = dataPathATR.replace("{ATR3}", longTerm.getAtr3().toString());
            dataPathATR = dataPathATR.replace("{ATR14}", longTerm.getAtr14().toString());
            userPrompt.append(dataPathATR).append("\n");


            String dataPathVolume = """
                Current Volume: {volume} vs. Average Volume: {volumeAVG}
                """;
            dataPathVolume = dataPathVolume.replace("{volume}", longTerm.getCurrentVolume().toString());
            dataPathVolume = dataPathVolume.replace("{volumeAVG}", longTerm.getAverageVolume().toString());
            userPrompt.append(dataPathVolume).append("\n");

            String dataPathMACDIndicatorsLong = """
                MACD indicators: {macdIndicators}
                """;
            dataPathMACDIndicatorsLong = dataPathMACDIndicatorsLong.replace("{macdIndicators}", longTerm.getMacdIndicatorsString().toString());
            userPrompt.append(dataPathMACDIndicatorsLong).append("\n");
            String dataPathRSIndicators14Long = """
                RSI indicators (14‑Period): {rsi14period}
                """;
            dataPathRSIndicators14Long = dataPathRSIndicators14Long.replace("{rsi14period}", longTerm.getRsi14period().toString());
            userPrompt.append(dataPathRSIndicators14Long).append("\n");
        }

        //******* 开始当前持仓部分
        userPrompt.append("\n").append("""
            HERE IS YOUR ACCOUNT INFORMATION & PERFORMANCE
            """).append("\n");
        if(Objects.nonNull(balance)){
            userPrompt.append("Available Cash: " + balance.getFreeBalance().toPlainString() + "USDT").append("\n");
            userPrompt.append("Current Account Value: " + balance.getEquity().toPlainString() + "USDT").append("\n");
        }

        if(Objects.nonNull(positions)){
            String positionStr = "Current live positions & performance:\n" + positions.toJSONString();
            userPrompt.append(positionStr).append("\n");
        }

        if(Objects.nonNull(historyPositions)){
            String positionHisStr = "History positions (limit 10) & performance (price unit:USDT):\n";
            userPrompt.append(positionHisStr).append("\n");
            StringBuilder hispos = new StringBuilder();
            for (HistoryPosition historyPosition : historyPositions) {
                String item = """
                Symbol={symbol} Side={side} OpenAvg={openAvg} CloseAvg={closeAvg} CloseSize={closeSize} Pnl={pnl} netProfit={netProfit} fundingFee={fundingFee} openFee={openFee} closeFee={closeFee} createTime={createTime} closeTime={closeTime}
                """;
                item = item.replace("{symbol}", historyPosition.getSymbol());
                item = item.replace("{side}", historyPosition.getHoldSide());
                if (Objects.nonNull(historyPosition.getOpenAvgPrice())) {
                    item = item.replace("{openAvg}", historyPosition.getOpenAvgPrice().toPlainString());
                }else{
                    item = item.replace("{openAvg}", "-");
                }
                if (Objects.nonNull(historyPosition.getCloseAvgPrice())) {
                    item = item.replace("{closeAvg}", historyPosition.getCloseAvgPrice().toPlainString());
                }else{
                    item = item.replace("{closeAvg}", "-");
                }
                if (Objects.nonNull(historyPosition.getPnl())) {
                    item = item.replace("{pnl}", historyPosition.getPnl().toPlainString());
                }else{
                    item = item.replace("{pnl}", "-");
                }

                if(Objects.nonNull(historyPosition.getOpenFee())){
                    item = item.replace("{openFee}", historyPosition.getOpenFee().toPlainString());
                }else{
                    item = item.replace("{openFee}", "-");
                }
                if(Objects.nonNull(historyPosition.getCloseFee())) {
                    item = item.replace("{closeFee}", historyPosition.getCloseFee().toPlainString());
                }else{
                    item = item.replace("{closeFee}", "-");
                }
                if (Objects.nonNull(historyPosition.getNetProfit())) {
                    item = item.replace("{netProfit}", historyPosition.getNetProfit().toPlainString());
                }else{
                    item = item.replace("{netProfit}", "-");
                }

                if (Objects.nonNull(historyPosition.getTotalFunding())) {
                    item = item.replace("{fundingFee}", historyPosition.getTotalFunding().toPlainString());
                }else{
                    item = item.replace("{fundingFee}", "-");
                }
                item = item.replace("{createTime}", historyPosition.getCtimeFormat());
                item = item.replace("{closeTime}", historyPosition.getUtimeFormat());

                if(Objects.nonNull(historyPosition.getCloseTotalPos())){
                    item = item.replace("{closeSize}", historyPosition.getCloseTotalPos().toPlainString());
                }else{
                    item = item.replace("{closeSize}", "-");
                }

                hispos.append(item).append("\n");
            }
            userPrompt.append(hispos.toString()).append("\n");
        }




        return userPrompt.toString();
    }
}
