package com.bitstrat.init;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bitstrat.domain.CoinsRank;
import com.bitstrat.domain.bo.CoinsRankReversedBo;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.bybit.BybitSymbolInfo;
import com.bitstrat.mapper.CoinsRankMapper;
import com.bitstrat.service.BybitService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.service.ICoinsRankReversedService;
import com.bitstrat.utils.BitStratThreadFactory;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.bitstrat.utils.CoinsRankUtils.calcCoinsRankScoreReversed;



/**
 * 反向分数计算规则:
 * 1、计算24小时内比特币涨5个点。所有币，只要没超过5，扣1分，
 * 如果跌5个点，山寨币跌多于5个点扣1分
 * 2、每一个小时统计，BTC
 * BTC如果是涨，山寨币也是涨，但涨过了BTC，加1分，山寨币是跌的或者涨幅没超过BTC就不加分，
 * BTC如果是跌，山寨币也是跌的，跌幅没超过BTC，加1分，如果山寨币是涨的或者跌超过BTC就不加分.
 */
@Slf4j
@Component
public class SyncCoinsRankReversed {



    @Autowired
    ConfigService configService;

    @Autowired
    CommonServce commonServce;

    @Autowired
    BybitService bybitService;

    @Autowired
    CoinsRankMapper coinsRankMapper;
    @Autowired
    ICoinsRankReversedService coinsRankReversedService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, BitStratThreadFactory.forName("sync-Coins-scheduler"));
//    @Scheduled(cron = "0 10 */1 * * *")
//    @Scheduled(cron = "0 */5 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public synchronized void run() throws Exception {
        try{
            SyncStatusContextReversed.start();
            this.runContent();
        }finally {
            SyncStatusContextReversed.stop();
        }
    }
    public synchronized void runContent() throws Exception {
        log.info("开始同步计算山寨币（反向）相关表");
//        String coinsRankRange = configService.getConfigValue("coins_rank_range");

//        checkZeroAndInit();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfLastHour = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfLastHour = now.withMinute(0).withSecond(0).withNano(0);
        // 转换为时间戳（秒）
        long startTimestamp = startOfLastHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = endOfLastHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        List<List<BigDecimal>> btcusdt = commonServce.getKlinesData(commonServce.getByBitAccount(), "BTCUSDT", "60", startTimestamp, endTimestamp-1);
        log.info("btcusdt:{}", btcusdt);
        //获取BTC 开盘价，收盘价
        if(!btcusdt.isEmpty()){
            BigDecimal close = btcusdt.get(0).get(4);
            BigDecimal open = btcusdt.get(0).get(1);
            //计算百分比
            BigDecimal btcPercent = close.subtract(open) // close - open
                .divide(open, 4, RoundingMode.HALF_UP)  // / open 保留4位小数
                .multiply(new BigDecimal("100"));       // * 100 得到百分比

            //开始获取山寨币
            List<BybitSymbolInfo> symbolsLiner = bybitService.getSymbolsLiner();
            for (BybitSymbolInfo bybitSymbolInfo : symbolsLiner) {
                List<List<BigDecimal>> klinesData = commonServce.getKlinesData(commonServce.getByBitAccount(), bybitSymbolInfo.getSymbol(), "60", startTimestamp, endTimestamp - 1);
                if(!klinesData.isEmpty()){
                    BigDecimal targetClose = klinesData.get(0).get(4);
                    BigDecimal targetOpen = klinesData.get(0).get(1);
                    BigDecimal targetPercent = targetClose.subtract(targetOpen) // close - open
                        .divide(targetOpen, 4, RoundingMode.HALF_UP)  // / open 保留4位小数
                        .multiply(new BigDecimal("100"));       // * 100 得到百分比

                    int score = checkScore(btcPercent, targetPercent);

                    CoinsRankReversedBo coinsRankBo = coinsRankReversedService.selectBySymbol(bybitSymbolInfo.getSymbol());
                    if(Objects.nonNull(coinsRankBo)){
                        coinsRankBo.setMarketPrice(targetClose.toPlainString());
                        coinsRankBo.setScore(coinsRankBo.getScore() + score);
                        coinsRankBo.setPercentage(targetPercent.doubleValue());
                        calcCoinsRankScoreReversed(coinsRankBo, score);
                        coinsRankReversedService.updateByBo(coinsRankBo);
                    }else{
                        CoinsRankReversedBo insertBo = new CoinsRankReversedBo();
                        insertBo.setSymbol(bybitSymbolInfo.getSymbol());
                        insertBo.setMarketPrice(targetClose.toPlainString());
                        insertBo.setScore((long) score);
//                        calcCoinsRankScoreReversed(coinsRankBo, score);
                        insertBo.setPercentage(targetPercent.doubleValue());
                        coinsRankReversedService.insertByBo(insertBo);
                    }

                }


            }

        }

        //判断24H
        check24();



        log.info("同步计算山寨币相关表结束");

    }

    /**
     * 判断当前是否是0点，是则重置score 为0
     */
    private void checkZeroAndInit() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour == 0) {
            //重置
            UpdateWrapper<CoinsRank> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().set(CoinsRank::getScore, BigDecimal.ZERO);
            coinsRankMapper.update(updateWrapper);
            log.info("已重置排行榜");
        }

    }

    private void check24() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfLastHour = now.minusHours(24).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfLastHour = now.withMinute(0).withSecond(0).withNano(0);
        // 转换为时间戳（秒）
        long startTimestamp = startOfLastHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = endOfLastHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        List<List<BigDecimal>> btcusdt = commonServce.getKlinesData(commonServce.getByBitAccount(), "BTCUSDT", "60", startTimestamp, endTimestamp-1);
        log.info("btcusdt 24:{}", btcusdt);
        if(!btcusdt.isEmpty()){
            BigDecimal close = btcusdt.get(0).get(4);
            BigDecimal open = btcusdt.get(btcusdt.size() -1 ).get(1);
            //计算百分比
            BigDecimal btcPercent = close.subtract(open) // close - open
                .divide(open, 6, RoundingMode.HALF_UP)  // / open 保留4位小数
                .multiply(new BigDecimal("100"));       // * 100 得到百分比

            //开始获取山寨币
            List<BybitSymbolInfo> symbolsLiner = bybitService.getSymbolsLiner();
            for (BybitSymbolInfo bybitSymbolInfo : symbolsLiner) {
                List<List<BigDecimal>> klinesData = commonServce.getKlinesData(commonServce.getByBitAccount(), bybitSymbolInfo.getSymbol(), "60", startTimestamp, endTimestamp - 1);
                if(!klinesData.isEmpty()){
                    BigDecimal targetClose = klinesData.get(0).get(4);
                    BigDecimal targetOpen = klinesData.get(klinesData.size() - 1).get(1);
                    BigDecimal targetPercent = targetClose.subtract(targetOpen) // close - open
                        .divide(targetOpen, 6, RoundingMode.HALF_UP)  // / open 保留4位小数
                        .multiply(new BigDecimal("100"));       // * 100 得到百分比

                    int score = check24Score(btcPercent, targetPercent);

                    CoinsRankReversedBo coinsRankBo = coinsRankReversedService.selectBySymbol(bybitSymbolInfo.getSymbol());
                    if(Objects.nonNull(coinsRankBo)){
                        coinsRankBo.setMarketPrice(targetClose.toPlainString());
                        calcCoinsRankScoreReversed(coinsRankBo, score);
                        coinsRankBo.setPercentage(targetPercent.doubleValue());
                        coinsRankBo.setUpdateTime(new Date());
                        coinsRankReversedService.updateByBo(coinsRankBo);
                    }else{
                        CoinsRankReversedBo insertBo = new CoinsRankReversedBo();
                        insertBo.setSymbol(bybitSymbolInfo.getSymbol());
                        insertBo.setMarketPrice(targetClose.toPlainString());

//                        calcCoinsRankScoreReversed(coinsRankBo, score);
                        insertBo.setPercentage(targetPercent.doubleValue());
                        insertBo.setUpdateTime(new Date());
                        coinsRankReversedService.insertByBo(insertBo);
//                        calcCoinsRankScoreReversed(insertBo, score);
                    }

                }
            }
        }

    }
    /**
     * 检查并打分
     24小时 比特币涨5个点。所有币，只要没超过5，不管多少分，都不上榜，
     如果跌5个点，山寨币跌多于5个点不上榜
     *
     * @param btcPercent
     * @param targetPercent
     * @return
     */
    private int check24Score(BigDecimal btcPercent, BigDecimal targetPercent) {
        //BTC 是否是涨
        if (btcPercent.compareTo(BigDecimal.ZERO) > 0) {
            //山寨币也是涨，但没超过BTC，隐藏
            if(targetPercent.compareTo(BigDecimal.ZERO) > 0 && targetPercent.compareTo(btcPercent) < 0) {
                return -1;
            }
        }
        //BTC 是跌
        if(btcPercent.compareTo(BigDecimal.ZERO) < 0) {
            //山寨币也是跌的，跌幅超过BTC，隐藏
            if(targetPercent.compareTo(BigDecimal.ZERO) < 0 && targetPercent.compareTo(btcPercent) < 0) {
                return -1;
            }

        }
        return 0;
    }

    /**
     * 检查并打分
     * 没btc 跌的厉害，涨的倒是比btc 多
     * 每一天一个周期，0点所有币种权重为0，然后每一个小时统计一下，BTC
     * 如果是涨，山寨币也是涨，但涨过了BTC，加分，山寨币是跌的或者涨幅没超过BTC就不加分，
     * BTC如果 是跌，山寨币也是跌的，跌幅没超过BTC，加1分，如果山寨币是涨的或者跌超过BTC就不加分，然后按分数高低排名
     *
     * @param btcPercent
     * @param targetPercent
     * @return
     */
    private int checkScore(BigDecimal btcPercent, BigDecimal targetPercent) {
        //BTC 是否是涨
        if (btcPercent.compareTo(BigDecimal.ZERO) > 0) {
            //山寨币也是涨，但涨过了BTC，加分
            if(targetPercent.compareTo(BigDecimal.ZERO) > 0 && targetPercent.compareTo(btcPercent) > 0) {
                return 1;
            }
        }
        //BTC 是跌
        if(btcPercent.compareTo(BigDecimal.ZERO) < 0) {
            //山寨币也是跌的，跌幅没超过BTC，加1分，如果山寨币是涨的或者跌超过BTC就不加分，然后按分数高低排名
            if(targetPercent.compareTo(BigDecimal.ZERO) < 0 && targetPercent.compareTo(btcPercent) > 0) {
                return 1;
            }

        }

        return 0;
    }


    private JSONObject getSymbolList() {
        ByBitAccount byBitAccount = commonServce.getByBitAccount();
        var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(),byBitAccount.getApiPwd()).newMarketDataRestClient();
        MarketDataRequest marketDataRequest = MarketDataRequest.builder().category(commonServce.getCateType()).build();
        Object instrumentsInfo = client.getInstrumentsInfo(marketDataRequest);
        JSONObject from = JSONObject.from(instrumentsInfo);
        return from;
    }

    public static void main(String[] args) {
        BigDecimal target = new BigDecimal("-6");
        BigDecimal btc = new BigDecimal("-5");
        boolean b = target.compareTo(btc) < 0;
        System.out.printf(b +"");

    }
}
