package com.bitstrat.strategy.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.bo.CoinsTaskBo;
import com.bitstrat.domain.bo.CoinsTaskLogBo;
import com.bitstrat.domain.bo.PositionVo;
import com.bitstrat.domain.bybit.StrategyConfig;
import com.bitstrat.domain.bybit.StrategySell;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.service.BybitService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.service.ICoinsTaskLogService;
import com.bitstrat.service.ICoinsTaskService;
import com.bitstrat.strategy.NormalStrategy;
import com.bitstrat.strategy.PositionStrategy;
import com.bitstrat.strategy.PositionStrategyManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.log.event.OperLogEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.graalvm.polyglot.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 10:56
 * @Content
 */

@Component
@Slf4j
public class MAStrategy implements NormalStrategy {

    @Autowired
    CommonServce commonServce;

    @Autowired
    BybitService bybitService;

    @Autowired
    PositionStrategyManager positionStrategyManager;
    @Autowired
    ICoinsTaskLogService coinsTaskLogService;
    @Autowired
    ICoinsTaskService coinsTaskService;

    @Override
    public String typeName() {
        return "MA_Strategy";
    }

    @Override
    public Integer typeId() {
        return 1;
    }
    //MA7 21 63 189


    @Override
    public Logger getLogger() {
        return (Logger) LoggerFactory.getLogger(MAStrategy.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void run(CoinsTaskVo taskTemp, OperLogEvent operLog) {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        Logger logger = this.getLogger();
        // 添加 ListAppender 临时收集日志
        logger.addAppender(listAppender);
        listAppender.start();

        CoinsTaskVo task = coinsTaskService.queryById(taskTemp.getId());
        String symbol = task.getSymbol();
        symbol = symbol.endsWith("USDT") ? symbol: symbol+"USDT";
        List<List<BigDecimal>> trxusdt = commonServce.getKlinesData(commonServce.getBybitUserAccountByExchange(task.getCreateBy()), symbol, task.getInterval());

        BarSeries barSeries = commonServce.getBarSeries(trxusdt, symbol, Long.valueOf(task.getInterval()));

        //解析规则
        StrategyConfig config = commonServce.parsRole(task.getStrategyConfig());
        //获取下单规则
        List<String> order = config.getOrder();
        HashSet<String> maCollect = new HashSet<>();
        //历史ma
        HashSet<String> maHCollect = new HashSet<>();
        //RSI
        HashSet<String> rsiCollect = new HashSet<>();


        for (String role : order) {
            List<String> MAValue = findMA(role);
            maCollect.addAll(MAValue);
            List<String> mah = findMAH(role);
            maHCollect.addAll(mah);
            List<String> rsi = findRSI(role);
            rsiCollect.addAll(rsi);
        }
        List<StrategySell> sell = config.getSell();
        for (StrategySell strategySell : sell) {
            String role = strategySell.getRole();
            List<String> MAValue = findMA(role);
            maCollect.addAll(MAValue);
            List<String> mah = findMAH(role);
            maHCollect.addAll(mah);
            List<String> rsi = findRSI(role);
            rsiCollect.addAll(rsi);
        }
        //获取所有ma 并处理
        HashMap<String, BigDecimal> maMap = new HashMap<>();
        HashMap<String, BigDecimal> maHMap = new HashMap<>();
        HashMap<String, BigDecimal> rsiMap = new HashMap<>();

        // 计算 MA
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        int lastIndex = barSeries.getBarCount() - 1;
        for (String ma : maCollect) {
            String maVal = ma.replace("MA", "");
            if (NumberUtils.isParsable(maVal)) {
                try {
                    int mavalue = Integer.parseInt(maVal);
                    SMAIndicator smaIndicator = new SMAIndicator(closePrice, mavalue);
                    Num value = smaIndicator.getValue(lastIndex);
                    BigDecimal bigDecimal = value.bigDecimalValue();
                    maMap.put(ma, bigDecimal);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }

            } else {
                throw new RuntimeException(MessageUtils.message("bybit.task.error.unsupported.ma", ma));
            }

        }
        for (String mah : maHCollect) {
            int[] idxAndHis = findIdxAndHis(mah);
            int index = barSeries.getBarCount() - idxAndHis[1];
            try {
                int mavalue = idxAndHis[0];
                SMAIndicator smaIndicator = new SMAIndicator(closePrice, mavalue);
                Num value = smaIndicator.getValue(index);
                BigDecimal bigDecimal = value.bigDecimalValue();
                maHMap.put(mah, bigDecimal);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

        }

        //计算RSI
        for (String rsi : rsiCollect) {
            String rsiVal = rsi.replace("RSI", "");
            if (NumberUtils.isParsable(rsiVal)) {
                try {
                    int rsivalue = Integer.parseInt(rsiVal);
                    RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsivalue);
//                    SMAIndicator smaIndicator = new SMAIndicator(closePrice, rsivalue);
                    Num value = rsiIndicator.getValue(lastIndex);
                    BigDecimal bigDecimal = value.bigDecimalValue();
                    rsiMap.put(rsi, bigDecimal);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }

            } else {
                throw new RuntimeException(MessageUtils.message("bybit.task.error.unsupported.ma", rsi));
            }
        }
        log.info("RSI: {}",JSONObject.toJSONString(rsiMap));
        //获取市场价
        JSONObject tickers = commonServce.queryTickers(symbol, commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
        //获取当前持仓
        JSONObject position = commonServce.queryPosition(symbol, commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
        BigDecimal markPrice = tickers.getBigDecimal("markPrice");
        double size = position.getBigDecimal("size").doubleValue();
        BigDecimal avgPrice = position.getBigDecimal("avgPrice");
        BigDecimal unrealisedPnl = position.getBigDecimal("unrealisedPnl");

        //判断买入规则
        for (String role : order) {
            Set<String> mahvalue = maHMap.keySet();
            String express = new String(role);
            express = fillParams(mahvalue, express, maHMap, maMap, rsiMap,markPrice,avgPrice,size,unrealisedPnl);
            log.info("process buy Role = {}", express);

            String expression = express; // JS 表达式

            try (Context context = Context.create()) {
                boolean result = context.eval("js", expression).asBoolean();
                log.info("原表达式 {} 表达式 {} 结果: {}",role,expression,result);
                if (result) {



                    if (size >= task.getTotalBalance()) {
                        log.info("当前已无可用额度");
                        break;
                    }
                    //判断冷却时间
                    boolean checkColdSec = commonServce.checkColdSec(task.getLastOrderTime(), task.getColdSec());
                    if (checkColdSec == false) {
                        log.info("当前还在冷却时间内，不操作。");
                        break;
                    }
                    //判断策略
                    PositionStrategy strategy = positionStrategyManager.getStrategy(Math.toIntExact(task.getBuyRoleId()));
                    if (Objects.isNull(strategy)) {
                        log.info("无买入策略");
                        break;
                    } else {
                        PositionVo positionVo = new PositionVo();
                        positionVo.setType("buy");
                        positionVo.setCurrRole(role);
                        positionVo.setMarketPrice(markPrice);
                        positionVo.setAvgPrice(avgPrice);
                        if (!strategy.check(task, positionVo)) {
                            log.info("不满足买入策略，不处理");
                            break;
                        }
                    }


                    CoinsTaskLogBo coinsTaskLogBo = new CoinsTaskLogBo();
                    coinsTaskLogBo.setTaskId(task.getId());
                    coinsTaskLogBo.setExpress(role + "[" + expression + "]");
                    coinsTaskLogBo.setPrice(task.getSingleOrder());
                    coinsTaskLogBo.setBasePrice(markPrice == null ?null:markPrice.doubleValue());
                    coinsTaskLogBo.setTotal(task.getSingleOrder());
                    coinsTaskLogBo.setType(1L);
                    coinsTaskLogBo.setTenantId(task.getTenantId());
                    try {
                        String orderId = bybitService.buy(symbol, task.getSingleOrder(), commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
                        coinsTaskLogBo.setOrderId(orderId);
                        coinsTaskLogBo.setStatus(1);
                        ScheduledExecutorService scheduler = commonServce.getScheduler();
                        ConcurrentHashMap<String, ScheduledFuture<?>> schedulerMap = commonServce.getSchedulerMap();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                log.info("开始检查订单号 {} ", orderId);
                                JSONObject orderBybit = commonServce.queryOrderStatus(orderId, commonServce.getBybitUserAccountByExchange(task.getCreateBy()));

                                coinsTaskLogBo.setAvgPrice(orderBybit.getDouble("avgPrice"));
                                coinsTaskLogBo.setBasePrice(orderBybit.getDouble("basePrice"));
                                coinsTaskLogBo.setOrderStatus(orderBybit.getString("orderStatus"));
                                coinsTaskLogService.updateByBo(coinsTaskLogBo);
                                List<String> finalStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Filled", "Cancelled", "Triggered", "Deactivated");
                                for (String status : finalStatus) {
                                    if (coinsTaskLogBo.getOrderStatus().equalsIgnoreCase(status)) {
                                        //订单最终状态
                                        JSONObject position = commonServce.queryPosition(task.getSymbol() + "USDT", commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
                                        coinsTaskLogBo.setTotal(position.getBigDecimal("size").doubleValue());
                                        coinsTaskLogService.updateByBo(coinsTaskLogBo);
                                        log.info("订单号 {} 已完成，停止同步", coinsTaskLogBo.getOrderId());

                                        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
                                        coinsTaskBo.setId(task.getId());
                                        coinsTaskBo.setLastOrderTime(new Date());
                                        coinsTaskBo.setLastBuyRole(role);
                                        coinsTaskService.updateByBo(coinsTaskBo);
                                        ScheduledFuture<?> remove = schedulerMap.remove(coinsTaskLogBo.getOrderId());
                                        if (Objects.nonNull(remove)) {
                                            remove.cancel(true);
                                        }
                                        break;
                                    }
                                }
                            }
                        };
                        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(runnable, 0, 5, TimeUnit.SECONDS);
                        schedulerMap.put(orderId, scheduledFuture);

                    } catch (Exception e) {
                        coinsTaskLogBo.setTotal(task.getSingleOrder());
                        coinsTaskLogBo.setStatus(2);
                        coinsTaskLogBo.setMsg(e.getMessage());

                    }
                    try{
                        coinsTaskLogBo.setLog(this.fethAllLog(listAppender));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    coinsTaskLogService.insertByBo(coinsTaskLogBo);

                    break;
                }


            }
        }
        //获取卖出规则
        for (StrategySell strategySell : sell) {
            String role = strategySell.getRole();
            Set<String> mahvalue = maHMap.keySet();
            String express = new String(role);
            express = fillParams(mahvalue, express, maHMap, maMap, rsiMap,markPrice,avgPrice,size,unrealisedPnl);
            log.info("process sell Role = {}", express);

            String expression = express; // JS 表达式

            try (Context context = Context.create()) {
                boolean result = context.eval("js", expression).asBoolean();
                log.info("原表达式 {} 表达式 {} 结果: {}",role,expression,result);
                if (result) {

                    if (Objects.isNull(task.getBalance()) || task.getBalance() <= 0) {
                        log.info("任务 {}无持仓,无需卖出", task.getName());
                        break;
                    }
                    //判断冷却时间
                    boolean checkColdSec = commonServce.checkColdSec(task.getLastOrderTime(), task.getColdSec());
                    if (checkColdSec == false) {
                        log.info("当前还在冷却时间内，不操作。");
                        break;
                    }
                    //判断策略
                    PositionStrategy strategy = positionStrategyManager.getStrategy(Math.toIntExact(task.getSellRoleId()));
                    if (Objects.isNull(strategy)) {
                        log.info("无卖出策略");
                        break;
                    } else {
                        PositionVo positionVo = new PositionVo();
                        positionVo.setType("sell");
                        positionVo.setCurrRole(role);
                        positionVo.setAvgPrice(avgPrice);
                        positionVo.setMarketPrice(markPrice);

                        if (!strategy.check(task, positionVo)) {
                            log.info("不满足卖出策略，不处理");
                            continue;
                        }
                    }


                    if (size < task.getSingleOrder()) {
                        log.info("不满足最低卖出数量");
                        continue;
                    }

                    CoinsTaskLogBo coinsTaskLogBo = new CoinsTaskLogBo();
                    coinsTaskLogBo.setTaskId(task.getId());
                    coinsTaskLogBo.setExpress(role + "[" + expression + "]");
                    coinsTaskLogBo.setPrice(task.getSingleOrder());
                    coinsTaskLogBo.setBasePrice(markPrice == null ?null:markPrice.doubleValue());
                    coinsTaskLogBo.setType(2L);
                    coinsTaskLogBo.setTenantId(task.getTenantId());

                    try {
                        //获取卖出数量
                        BigDecimal sellSize = this.getSellSize(strategySell, task);
                        String orderId = bybitService.sell(symbol, sellSize.doubleValue(), commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
                        coinsTaskLogBo.setOrderId(orderId);
                        coinsTaskLogBo.setStatus(1);
                        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
                        ScheduledExecutorService scheduler = commonServce.getScheduler();
                        ConcurrentHashMap<String, ScheduledFuture<?>> schedulerMap = commonServce.getSchedulerMap();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                log.info("开始检查订单号 {} ", orderId);
                                String symbol = task.getSymbol();
                                symbol = symbol.endsWith("USDT") ? symbol: symbol+"USDT";
                                JSONObject orderBybit = commonServce.queryOrderStatus(orderId, commonServce.getBybitUserAccountByExchange(task.getCreateBy()));

                                coinsTaskLogBo.setAvgPrice(orderBybit.getDouble("avgPrice"));
                                coinsTaskLogBo.setOrderStatus(orderBybit.getString("orderStatus"));
                                coinsTaskLogService.updateByBo(coinsTaskLogBo);

                                List<String> finalStatus = Arrays.asList("Rejected", "PartiallyFilledCanceled", "Filled", "Cancelled", "Triggered", "Deactivated");
                                for (String status : finalStatus) {
                                    if (coinsTaskLogBo.getOrderStatus().equalsIgnoreCase(status)) {
                                        //订单最终状态
                                        JSONObject position = commonServce.queryPosition(symbol, commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
                                        coinsTaskLogBo.setTotal(position.getBigDecimal("size").doubleValue());
                                        coinsTaskLogService.updateByBo(coinsTaskLogBo);
                                        log.info("订单号 {} 已完成，停止同步", coinsTaskLogBo.getOrderId());
                                        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
                                        coinsTaskBo.setId(task.getId());
                                        coinsTaskBo.setLastOrderTime(new Date());
                                        coinsTaskBo.setLastSellRole(role);
                                        coinsTaskService.updateByBo(coinsTaskBo);
                                        ScheduledFuture<?> remove = schedulerMap.remove(coinsTaskLogBo.getOrderId());
                                        if (Objects.nonNull(remove)) {
                                            remove.cancel(true);
                                        }
                                        break;
                                    }
                                }
                            }
                        };
                        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(runnable, 0, 5, TimeUnit.SECONDS);
                        schedulerMap.put(orderId, scheduledFuture);
                    } catch (Exception e) {
                        e.printStackTrace();
                        coinsTaskLogBo.setTotal(task.getSingleOrder());
                        coinsTaskLogBo.setStatus(2);
                        coinsTaskLogBo.setMsg(e.getMessage());

                    }
                    try{
                        coinsTaskLogBo.setLog(this.fethAllLog(listAppender));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    coinsTaskLogService.insertByBo(coinsTaskLogBo);
                    break;
                }


            }
        }

        operLog.setJsonResult(JSONObject.toJSONString(maMap));

    }

    private static String fillParams(Set<String> mahvalue, String express,
                                     HashMap<String, BigDecimal> maHMap,
                                     HashMap<String, BigDecimal> maMap,
                                     HashMap<String, BigDecimal> rsiMap,
                                     BigDecimal marketPrice,BigDecimal avgPrice,double size,BigDecimal unrealisedPnl) {
        for (String ma : mahvalue) {
            express = express.replaceAll(ma, maHMap.get(ma).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString());
        }
        Set<String> mavalue = maMap.keySet();
        for (String ma : mavalue) {
            express = express.replaceAll(ma, maMap.get(ma).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString());
        }
        Set<String> rsiValue = rsiMap.keySet();
        for (String rsi : rsiValue) {
            express = express.replaceAll(rsi, rsiMap.get(rsi).setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString());
        }
        express= express.replaceAll("marketPrice",marketPrice.setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString());
        express=express.replaceAll("avgPrice",avgPrice== null ?"null":avgPrice.setScale(6, BigDecimal.ROUND_HALF_UP).toPlainString());
        express=express.replaceAll("size",new BigDecimal(size).toPlainString());
        express=express.replaceAll("unrealisedPnl",unrealisedPnl==null?"null":unrealisedPnl.toPlainString());

        if(Objects.nonNull(avgPrice)){
            // 盈亏百分比：(市场价 - 成本价) / 成本价 × 100
            BigDecimal profitPercent;
            if(avgPrice.compareTo(BigDecimal.ZERO)==0){
                profitPercent = BigDecimal.ZERO;
            }else{
                profitPercent = marketPrice.subtract(avgPrice)
                    .divide(avgPrice, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .multiply(new BigDecimal("10"));//10倍合约
            }

            express=express.replaceAll("profitPercent",profitPercent.setScale(4, BigDecimal.ROUND_HALF_UP).toPlainString());
        }else{
            express=express.replaceAll("profitPercent","0");

        }

        return express;
    }

    private BigDecimal getSellSize(StrategySell strategySell, CoinsTaskVo task) {
        String unit = strategySell.getUnit();
        List<String> allowUnit = Arrays.asList("percentage", "quantity");
        if (!allowUnit.contains(unit)) {
            throw new RuntimeException("unsupport sell unit,unit allow [percentage,quantity] but find " + unit);
        }
        String symbol = task.getSymbol();
        symbol = symbol.endsWith("USDT") ? symbol: symbol+"USDT";
        JSONObject position = commonServce.queryPosition(symbol, commonServce.getBybitUserAccountByExchange(task.getCreateBy()));
        BigDecimal markPrice = position.getBigDecimal("markPrice");
        double size = position.getBigDecimal("size").doubleValue();
        //根据不同的属性获取卖出数量
        if (unit.equalsIgnoreCase("percentage")) {
            //百分比

//            BigDecimal psize = new BigDecimal(size);
//            BigDecimal percent = new BigDecimal(strategySell.getLimit());
//            // 计算占比
//            BigDecimal result = psize.multiply(percent).divide(new BigDecimal(100), Math.toIntExact(task.getScale()), RoundingMode.HALF_UP);
//            double totalMarketPrice = result.multiply(markPrice).doubleValue();
//            if(commonServce.getMinOrderAmt().doubleValue() < totalMarketPrice){
//                log.error("百分比数量 {} 总价值 {} 不满足最低订单价值 {} 要求",result.toPlainString(),totalMarketPrice,commonServce.getMinOrderAmt());
//                throw new RuntimeException("百分比数量 "+result.toPlainString()+" 总价值 "+totalMarketPrice+" 不满足最低订单价值 "+commonServce.getMinOrderAmt()+" 要求");
//            }
            BigDecimal psize = new BigDecimal(size); // 当前持仓量
            BigDecimal percent = new BigDecimal(strategySell.getLimit()); // 设定卖出百分比
            BigDecimal result = psize.multiply(percent).divide(new BigDecimal(100), Math.toIntExact(task.getScale()), RoundingMode.HALF_UP); // 计算卖出量
            BigDecimal remaining = psize.subtract(result); // 计算剩余持仓量
            BigDecimal totalMarketPrice = result.multiply(markPrice); // 卖出的价值
            BigDecimal remainingTotalMarketPrice = remaining.multiply(markPrice); // 剩余价值
            BigDecimal minOrderAmt = BigDecimal.valueOf(commonServce.getMinOrderAmt()); // 最小订单要求

// 判断是否满足最小订单要求
            if (totalMarketPrice.compareTo(minOrderAmt) < 0) {
                if (remainingTotalMarketPrice.compareTo(minOrderAmt) < 0) {
                    // 剩余部分也达不到最小订单要求，直接全部卖出
                    result = psize;
                } else {
                    // 不能卖，直接抛出异常
                    log.error("百分比数量 {} 总价值 {} 不满足最低订单价值 {} 要求", result.toPlainString(), totalMarketPrice, minOrderAmt);
                    throw new RuntimeException("百分比数量 " + result.toPlainString() + " 总价值 " + totalMarketPrice + " 不满足最低订单价值 " + minOrderAmt + " 要求");
                }
            }
            return result;
        } else if (unit.equalsIgnoreCase("quantity")) {
            //数量
            BigDecimal limit = new BigDecimal(strategySell.getLimit());
            if (limit.doubleValue() > size) {
                log.error("需求的数量 {} 不满足当前持仓 {} ", limit, size);
//                throw new RuntimeException("当前持仓" + size + " 不满足需求的数量 " + limit.toPlainString());
                return BigDecimal.valueOf(size);
            }
            double totalMarketPrice = limit.multiply(markPrice).doubleValue();
            if (commonServce.getMinOrderAmt().doubleValue() > totalMarketPrice) {
                log.error("数量 {} 总价值 {} 不满足最低订单价值 {} 要求", limit.toPlainString(), totalMarketPrice, commonServce.getMinOrderAmt());
                throw new RuntimeException("需求数量 " + limit.toPlainString() + " 总价值 " + totalMarketPrice + " 不满足最低订单价值 " + commonServce.getMinOrderAmt() + " 要求");
            }
            return limit;
        }
        throw new RuntimeException("unsupport sell unit,unit allow [percentage,quantity] but find " + unit);
    }

    private static List<String> findMA(String role) {
        // 1️⃣ 定义正则匹配 "MA" + 数字
        Pattern pattern = Pattern.compile("MA\\d+");
        Matcher matcher = pattern.matcher(role);

        // 2️⃣ 存储匹配结果
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    private static List<String> findMAH(String role) {
        // 1️⃣ 定义正则匹配 "MA" + 数字
        Pattern pattern = Pattern.compile("MA\\d+H\\d+");
        Matcher matcher = pattern.matcher(role);

        // 2️⃣ 存储匹配结果
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    private static List<String> findRSI(String role) {
        // 1️⃣ 定义正则匹配 "MA" + 数字
        Pattern pattern = Pattern.compile("RSI\\d+");
        Matcher matcher = pattern.matcher(role);

        // 2️⃣ 存储匹配结果
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    private static int[] findIdxAndHis(String ma) {
        // 正则表达式：匹配数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(ma);
        int[] val = new int[2];
        // 提取第一个数字
        if (matcher.find()) {
            int num1 = Integer.parseInt(matcher.group());
            val[0] = num1;
        }

        // 提取第二个数字
        if (matcher.find()) {
            int num2 = Integer.parseInt(matcher.group());
            val[1] = num2;
        }
        return val;
    }

    public static void main(String[] args) {
        String role = "MA2H1>MA7H1>MA15H1 && MA2>MA7>MA15";
        List<String> mah = findMAH(role);
        System.out.println(mah);
        List<String> ma = findMA(role);
        System.out.println(ma);
    }
}
