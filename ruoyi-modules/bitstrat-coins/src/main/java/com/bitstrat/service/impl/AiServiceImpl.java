package com.bitstrat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.bitstrat.calc.constant.DefaultIndicatorType;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.*;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.domain.bo.CoinAiTaskRequestBo;
import com.bitstrat.domain.bo.CoinTestAiResultBo;
import com.bitstrat.domain.diy.ExtConfigItem;
import com.bitstrat.domain.diy.MarketDataPromptRule;
import com.bitstrat.domain.okx.OkxTpSlOrderItem;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.event.RedisKeyExpireEvent;
import com.bitstrat.mapper.CoinsAiConfigMapper;
import com.bitstrat.service.*;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/22 17:45
 * @Content
 */

@Slf4j
@Service
public class AiServiceImpl implements AiService {
    ExecutorService executorService = new ThreadPoolExecutor(3, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000));
    ExecutorService runExecutorService = new ThreadPoolExecutor(3, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000));
    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    private ICoinApiPositionService apiPositionService;
    @Autowired
    ICoinTestAiResultService coinTestAiResultService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    DingTalkBotClient dingTalkBotClient;

    @Autowired
    KeyCryptoService keyCryptoService;

    @Autowired
    ICoinAITaskBalanceService coinTestAiService;

    @Autowired
    DifyWorkflowUtils difyWorkflowUtils;
    @Autowired
    PromptService promptService;
    @Autowired
    ICoinsNotifyConfigService notifyConfigService;

    @Autowired
    ICoinsNotifyService coinsNotifyService;

    @Autowired
    CoinsAiConfigMapper coinsAiMapper;
    @Autowired
    ISysUserService sysUserService;

    @Autowired
    CommonService commonService;


    @Autowired
    ICoinsBalanceLogService balanceLogService;

    @Autowired
    ICoinsAiTaskService coinsAiTaskService;

    @Autowired
    ICoinTestAiRequestService coinTestAiRequestService;

    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public void doOperate(AIOperateItem aiOperate, ExchangeType exchangeType, Account account, String requestKey
        , String details, Long taskId) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
        CoinTestAiResultBo coinTestAiResult = new CoinTestAiResultBo();
        coinTestAiResult.setCreateTime(new Date());
        if (Objects.nonNull(aiOperate.getTaskVo())) {
            coinTestAiResult.setSize(aiOperate.getSize().toPlainString());
        } else {
            coinTestAiResult.setSize("-");
        }
        if (Objects.nonNull(aiOperate.getLeverage())) {
            coinTestAiResult.setLeverage(aiOperate.getLeverage().longValue());
        }

        coinTestAiResult.setSymbol(aiOperate.getSymbol());
        coinTestAiResult.setAction(aiOperate.getAction());
        coinTestAiResult.setTakeProfit(Optional.ofNullable(aiOperate.getTakeProfit())
            .map(BigDecimal::toPlainString)
            .orElse("-"));
        coinTestAiResult.setStopLoss(Optional.ofNullable(aiOperate.getStopLoss())
            .map(BigDecimal::toPlainString)
            .orElse("-"));
        if (Objects.nonNull(aiOperate.getReasoning())) {
            Reasoning reasoning = aiOperate.getReasoning();
            coinTestAiResult.setReasoningEn(reasoning.getEn());
            coinTestAiResult.setReasoningZh(reasoning.getZh());
            coinTestAiResult.setThink(details);
        }
        coinTestAiResult.setRequestKey(requestKey);
        coinTestAiResult.setTaskId(taskId);
        coinTestAiResult.setCreateBy(aiOperate.getUserId());
        coinTestAiResultService.insertByBo(coinTestAiResult);
        StringBuilder result = new StringBuilder();
        try {
            /**
             * 只有做多做空关仓时判断是否满足情况
             */
            if (ActionEnum.contains(aiOperate.getAction(), ActionEnum.LONG, ActionEnum.SHORT)) {
                if (!checkEffectivePrice(aiOperate, result, exchangeService, account)) {
                    return;
                }
            }

            if (aiOperate.getAction().equalsIgnoreCase("long")) {
                OrderVo orderVo = new OrderVo();
                orderVo.setSymbol(aiOperate.getSymbol());
                orderVo.setTakeProfitPrice(aiOperate.getTakeProfit());
                orderVo.setStopLossPrice(aiOperate.getStopLoss());
                orderVo.setSize(exchangeService.calcOrderSize(aiOperate.getSymbol(), aiOperate.getSize()));
                orderVo.setLeverage(aiOperate.getLeverage());
                exchangeService.setLeverage(account, aiOperate.getLeverage().intValue(), aiOperate.getSymbol(), CrossContractSide.LONG);
//            orderVo.setPrice(resultV.getInPrice());
                orderVo.setOrderType(OrderType.MARKET);
                exchangeService.buyContract(account, orderVo);
                setTpSl(aiOperate, account, exchangeService);
                saveExtInfo(aiOperate);
                runExecutorService.submit(() -> {
                    MessageUtils.sendLongMsg(aiOperate);
                });

            } else if (aiOperate.getAction().equalsIgnoreCase("short")) {
                OrderVo orderVo = new OrderVo();
                orderVo.setSymbol(aiOperate.getSymbol());
                orderVo.setTakeProfitPrice(aiOperate.getTakeProfit());
                orderVo.setStopLossPrice(aiOperate.getStopLoss());
                orderVo.setSize(exchangeService.calcOrderSize(aiOperate.getSymbol(), aiOperate.getSize()));
                orderVo.setLeverage(aiOperate.getLeverage());
                exchangeService.setLeverage(account, aiOperate.getLeverage().intValue(), aiOperate.getSymbol(), CrossContractSide.SHORT);
//            orderVo.setPrice(resultV.getInPrice());
                orderVo.setOrderType(OrderType.MARKET);
                exchangeService.sellContract(account, orderVo);
                setTpSl(aiOperate, account, exchangeService);
                saveExtInfo(aiOperate);
                runExecutorService.submit(() -> {
                    MessageUtils.sendShortMsg(aiOperate);
                });
            } else if (aiOperate.getAction().equalsIgnoreCase("hold")) {
                log.info("继续持有 {}", aiOperate.getSymbol());
                saveExtInfo(aiOperate);
            } else if (aiOperate.getAction().equalsIgnoreCase("nothing")) {
                log.info("维持现状 {}", aiOperate.getSymbol());
            } else if (aiOperate.getAction().equalsIgnoreCase("close")) {
                //平仓之前先查持仓
                closePosition(aiOperate, account, exchangeService, result);
                runExecutorService.submit(() -> {
                    MessageUtils.sendClose(aiOperate);
                });

            } else if (aiOperate.getAction().equalsIgnoreCase("tpsl")) {
                setTpSl(aiOperate, account, exchangeService);
                saveExtInfo(aiOperate);
                runExecutorService.submit(() -> {
                    MessageUtils.sendTpSl(aiOperate);
                });
            } else if (aiOperate.getAction().equalsIgnoreCase("reduce")) {
                reducePosition(aiOperate, account, exchangeService);
                setTpSl(aiOperate, account, exchangeService);
                saveExtInfo(aiOperate);
                runExecutorService.submit(() -> {
                    MessageUtils.sendReduceMsg(aiOperate);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.append(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        } finally {
            if (result.isEmpty()) {
                result.append("ALL OPERATE HAS SUCCESSFUL");
            }
            coinTestAiResult.setResult(result.toString());
            coinTestAiResultService.updateByBo(coinTestAiResult);
        }


    }

    /**
     * 判断价格是否满足策略生效条件
     *
     * @param aiOperate
     * @param result
     * @return
     */
    private boolean checkEffectivePrice(AIOperateItem aiOperate, StringBuilder result, ExchangeService exchangeService, Account account) {
        EffectivePriceRange range = aiOperate.getEffectivePriceRange();


        // 未提供生效区间，直接判定策略无效（强约束）
        if (range == null || !range.isValidateRange()) {
            result.append("Effective price range is missing or invalid;");
            return false;
        }

        BigDecimal nowPrice = exchangeService.getNowPrice(account, aiOperate.getSymbol());
        if (nowPrice == null) {
            result.append("Failed to fetch current market price;");
            return false;
        }

        BigDecimal min = range.getMin();
        BigDecimal max = range.getMax();

        // 使用闭区间判断：min <= price <= max
        boolean inRange =
            nowPrice.compareTo(min) >= 0 &&
                nowPrice.compareTo(max) <= 0;

        if (!inRange) {
            result.append(String.format(
                "Current price %s is outside effective range [%s, %s];",
                nowPrice, min, max
            ));
            log.warn("Current price {} is outside effective range [{},{}]", nowPrice, min, max);
            return false;
        }

        return true;
    }

    private void closePosition(AIOperateItem aiOperate, Account account, ExchangeService exchangeService, StringBuilder result) {
        OrderPosition position = exchangeService.queryContractPosition(account, aiOperate.getSymbol(), new PositionParams());
        if (Objects.nonNull(position)) {
            //仓位没有平
            OrderPosition orderPosition = new OrderPosition();
            orderPosition.setSymbol(aiOperate.getSymbol());
            orderPosition.setSide(position.getSide());
            orderPosition.setSize(position.getSize());
            OrderCloseResult optResult = exchangeService.closeContractPosition(account, orderPosition);
            if (!CrossOrderStatus.SUCCESS.equalsIgnoreCase(optResult.getStatus())) {
                throw new RuntimeException("平仓异常：" + optResult.getMsg() + "  " + optResult.getBody());
            }
            removeExtInfo(aiOperate);
            log.info("已平仓 {}", aiOperate.getSymbol());
        } else {
            log.warn("仓位已平，无需重复平仓 {}", aiOperate.getSymbol());
            result.append("仓位已平，无需重复平仓 " + aiOperate.getSymbol() + "\n");
        }

    }

    @Override
    public void invokeAiTask(CoinsAiTask coinsAiTask) {
        //先加入池子
        executorService.submit(() -> {
//            String systemPrompt = promptService.loadSystemPrompt();
//            if (StringUtils.isNoneEmpty(systemPrompt)) {
//                coinsAiTask.setSystemPrompt(systemPrompt+coinsAiTask.getSystemPrompt());
//            }
//            systemPrompt = promptService.formatPrompt(systemPrompt, coinsAiTask);
            CoinAiTaskRequestBo coinTestAiRequest = new CoinAiTaskRequestBo();
            coinTestAiRequest.setSysContent(coinsAiTask.getSystemPrompt());
            coinTestAiRequest.setTaskId(coinsAiTask.getId());
            coinTestAiRequest.setStatus(1L);
            coinTestAiRequest.setAiId(coinsAiTask.getAiWorkflowId());
            coinTestAiRequest.setCreateBy(coinsAiTask.getCreateUserId());
            coinTestAiRequest.setCreateTime(new Date());
            coinTestAiRequestService.insertByBo(coinTestAiRequest);


            try {
                //检查余额
                balanceLogService.checkBalance(coinTestAiRequest, coinsAiTask);
                this.runTask(coinsAiTask, coinTestAiRequest);
            } catch (Exception e) {
                e.printStackTrace();
                coinTestAiRequest.setStatus(4L);
                coinTestAiRequest.setErrorMsg(e.getMessage());
                coinTestAiRequestService.updateByBo(coinTestAiRequest);
            }

        });


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String aiTaskCallBack(JSONObject object) {

        log.info("difyCallback:{}", JSONObject.toJSONString(object, JSONWriter.Feature.PrettyFormatWith2Space));
        JSONArray data = object.getJSONArray("data");
//        executorService.submit(()->{
//            String jsonString = JSONObject.toJSONString(data, JSONWriter.Feature.PrettyFormatWith2Space);
//            String markdown = "## deepseek 响应 \n>时间：" + dateTimeFormatter.format(LocalDateTime.now()) +"\n" +jsonString;
//            dingTalkBotClient.sendMarkdown(webhook, secret, "deepseek响应", markdown, Collections.emptyList(), false);
//
//        });

        String requestKey = object.getString("requestKey");
        Long taskId = object.getLong("taskId");

        JSONObject usage = object.getJSONObject("usage");
        Long token = usage.getLong("total_tokens");
        CoinsAiTaskVo coinsAiTaskVo = coinsAiTaskService.queryById(taskId);
        if (Objects.isNull(coinsAiTaskVo)) {
            log.error("taskId:{} not exists", taskId);
            return "taskId:" + taskId + " not exists";
        }
        Long apiId = coinsAiTaskVo.getApiId();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(apiId);
        Account account = AccountUtils.coverToAccount(coinsApiVo);

        String exchange = coinsAiTaskVo.getExchange();

        ExchangeType exchangeType = ExchangeType.getExchangeType(exchange);
        if (Objects.isNull(exchangeType)) {
            throw new RuntimeException("不支持的交易所 " + exchange);
        }

        BigDecimal reducePrice;
        //计算token 以及金额
        SysUserVo sysUserVo = sysUserService.selectUserById(coinsAiTaskVo.getCreateUserId());

        CoinsAiConfig coinsAiConfig = coinsAiMapper.selectById(coinsAiTaskVo.getAiWorkflowId());
        BigDecimal price = coinsAiConfig.getPrice();//100M 的价格
        //查询汇率
        String currency = usage.getString("currency");
        BigDecimal rate = BigDecimal.ONE;
        if("RMB".equalsIgnoreCase(currency)){
            //RMB
            rate = commonService.getRMB2USDRate();
        }
        reducePrice = TokenCalcUtils.calculateCostPerTokens(BigDecimal.valueOf(token), price,rate);
//        executorService.submit(()->{
        CoinsBalanceLog coinsBalanceLog = new CoinsBalanceLog();
        coinsBalanceLog.setUserId(coinsAiTaskVo.getCreateUserId());
        coinsBalanceLog.setAfterBalance(sysUserVo.getBalance().subtract(reducePrice));
        coinsBalanceLog.setBeforeBalance(sysUserVo.getBalance());
        coinsBalanceLog.setChangeAmount(reducePrice.negate());
        coinsBalanceLog.setType(2L);
        coinsBalanceLog.setStatus(2L);
        coinsBalanceLog.setRemark("requestKey:" + requestKey);
        balanceLogService.reduceBalanceByLog(coinsBalanceLog);

//        });

        CoinAiTaskRequest coinAiTaskRequest = coinTestAiRequestService.queryByRequestKey(requestKey);
        CoinAiTaskRequestBo coinAiTaskRequestBo = MapstructUtils.convert(coinAiTaskRequest, CoinAiTaskRequestBo.class);
        if (Objects.isNull(coinAiTaskRequestBo)) {
            log.error("requestKey:{} not exists", requestKey);
            return "requestKey:" + requestKey + " not exists";
        }
        coinAiTaskRequestBo.setPrice(reducePrice);
        String details = object.getString("details");
        if (StringUtils.isNotEmpty(requestKey)) {
            RBucket<Object> bucket = redissonClient.getBucket("aiRequestKey:" + requestKey);
            if (!bucket.isExists()) {
                log.info("requestKey:{} not exists  已过期，不处理", requestKey);
//                executorService.submit(()->{
//                    String markdown = "## deepseek 响应 已过期\n>时间：" + dateTimeFormatter.format(LocalDateTime.now()) +"\n requestKey"+requestKey;
//                    dingTalkBotClient.sendMarkdown(webhook, secret, "deepseek响应 已过期", markdown, Collections.emptyList(), false);
//
//                });
                if (coinAiTaskRequestBo.getStatus().equals(1L)) {
                    coinAiTaskRequestBo.setStatus(3L);
                    coinAiTaskRequestBo.setResult(JSONObject.toJSONString(data, JSONWriter.Feature.PrettyFormatWith2Space));
                    coinAiTaskRequestBo.setResponseTime(new Date());
                    coinAiTaskRequestBo.setErrorMsg("超时");
                    coinAiTaskRequestBo.setToken(token);
                    coinTestAiRequestService.updateByBo(coinAiTaskRequestBo);
                } else {
                    coinAiTaskRequestBo.setResult(JSONObject.toJSONString(data, JSONWriter.Feature.PrettyFormatWith2Space));
                    coinAiTaskRequestBo.setResponseTime(new Date());
                    coinAiTaskRequestBo.setToken(token);
                    coinTestAiRequestService.updateByBo(coinAiTaskRequestBo);
                }

                return "expire response";
            } else {
                bucket.delete();
                //成功
                coinAiTaskRequestBo.setStatus(2L);
                coinAiTaskRequestBo.setResult(JSONObject.toJSONString(data, JSONWriter.Feature.PrettyFormatWith2Space));
                coinAiTaskRequestBo.setResponseTime(new Date());
                coinAiTaskRequestBo.setErrorMsg("成功");
                coinAiTaskRequestBo.setToken(token);
                coinTestAiRequestService.updateByBo(coinAiTaskRequestBo);
            }
        }
        runExecutorService.submit(() -> {
            for (Object operateItem : data) {
                if (Objects.isNull(operateItem)) {
                    continue;
                }
                JSONObject operate = JSONObject.from(operateItem);
                AIOperateItem aiOperate = operate.toJavaObject(AIOperateItem.class);
                aiOperate.setUserId(coinsAiTaskVo.getCreateUserId());
                aiOperate.setTaskId(taskId);
                aiOperate.setTaskVo(coinsAiTaskVo);
                try {
                    this.doOperate(aiOperate, exchangeType, account, requestKey, details, taskId);
                } catch (Exception e) {
                    e.printStackTrace();
                    executorService.submit(() -> {
                        String markdown = "## 操作异常 \n>时间：" + dateTimeFormatter.format(LocalDateTime.now()) + "\n 错误:" + e.getMessage();

                        coinsNotifyService.sendNotification(coinsAiTaskVo.getCreateUserId(), markdown);

                    });
                    coinAiTaskRequestBo.setStatus(4L);
                    coinAiTaskRequestBo.setErrorMsg("API 操作异常" + e.getMessage());
                    coinTestAiRequestService.updateByBo(coinAiTaskRequestBo);
                }
            }
        });

        return "SUCCESS";
    }

    @Override
    public String aiTaskCallBackError(JSONObject data) {
        return "";
    }

    @Override
    @EventListener(RedisKeyExpireEvent.class)
    public void onRequestKeyExpire(RedisKeyExpireEvent redisKeyExpireEvent) {

        String key = redisKeyExpireEvent.getKey();
        if (key.startsWith("aiRequestKey:")) {
            String replaced = key.replace("aiRequestKey:", "");
            CoinAiTaskRequest coinAiTaskRequest = coinTestAiRequestService.queryByRequestKey(replaced);
            if (Objects.nonNull(coinAiTaskRequest)) {
                CoinAiTaskRequestBo convert = MapstructUtils.convert(coinAiTaskRequest, CoinAiTaskRequestBo.class);
                if (convert.getStatus().equals(1L)) {
                    convert.setStatus(3L);
                    convert.setErrorMsg("等待响应超时，您的提示词可能太长了，请优化提示词。(过长提示词AI 思考也会变长，会导致决策延迟过大与行情不匹配)");
                    coinTestAiRequestService.updateByBo(convert);
                }
            }
        }
    }

    /**
     * 执行任务
     *
     * @param coinsAiTask
     */
    @SneakyThrows
    public void runTask(CoinsAiTask coinsAiTask, CoinAiTaskRequestBo coinTestAiRequest) {
        String exchange = coinsAiTask.getExchange();

        ExchangeType exchangeType = ExchangeType.getExchangeType(exchange);
        if (Objects.isNull(exchangeType)) {
            throw new RuntimeException("不支持的交易所 " + exchange);
        }
        Long apiId = coinsAiTask.getApiId();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(apiId);
        if (Objects.isNull(coinsApiVo)) {
            throw new RuntimeException("API 未知 ID：" + apiId);
        }
        CoinsApiVo coinsApiVo1 = keyCryptoService.decryptApi(coinsApiVo);
        Account account = AccountUtils.coverToAccount(coinsApiVo1);


        //初始资金
        BigDecimal basePrice = coinsAiTask.getStartBalance();
        RAtomicLong invokedTimesRedis = redissonClient.getAtomicLong("invokedTimes:" + coinsAiTask.getId());
        long invokedTimes = invokedTimesRedis.incrementAndGet();

        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
        List<PositionWsData> positionWsData = exchangeService.queryContractPositionDetail(account, new PositionParams());
        AccountBalance balance = exchangeService.getBalance(account, "USDT");
        log.info("当前持仓:{}", JSONObject.toJSONString(positionWsData, JSONWriter.Feature.PrettyFormatWith2Space));

        JSONObject content = new JSONObject();
        //account
        JSONObject accountBody = new JSONObject();
        accountBody.put("available_usdt", balance.getFreeBalance());
        content.put("account", accountBody);
        JSONArray positions = new JSONArray();
        for (PositionWsData positionWsDatum : positionWsData) {
            JSONObject position = new JSONObject();
            position.put("symbol", positionWsDatum.getSymbol());
            position.put("size", exchangeService.calcShowSize(positionWsDatum.getSymbol(), positionWsDatum.getSize()) + positionWsDatum.getSymbol());
            position.put("open", positionWsDatum.getAvgPrice());
            position.put("unrealizedPL", (Objects.isNull(positionWsDatum.getUnrealizedProfit()) ? "-" : positionWsDatum.getUnrealizedPL()) + "USDT");
            position.put("achievedProfits", (Objects.isNull(positionWsDatum.getAchievedProfits()) ? "-" : positionWsDatum.getAchievedProfits()) + "USDT");
            position.put("totalFee", (Objects.isNull(positionWsDatum.getTotalFee()) ? BigDecimal.ZERO : positionWsDatum.getTotalFee().abs()) + "USDT");
//            position.put("liquidationPrice",positionWsDatum.getLiqPrice());
//            position.put("keepMarginRate",positionWsDatum.getKeepMarginRate());
            position.put("marginSize", (Objects.isNull(positionWsDatum.getMarginPrice()) ? BigDecimal.ZERO : positionWsDatum.getMarginPrice()) + "USDT");
            position.put("createPositionTime", simpleDateFormat.format(positionWsDatum.getCreateTime()));
            if (Objects.nonNull(positionWsDatum.getStopLoss())) {
                position.put("stopLoss", positionWsDatum.getStopLoss().stripTrailingZeros().toPlainString());
            } else {
                position.put("stopLoss", "-");
            }
            if (Objects.nonNull(positionWsDatum.getTakeProfit())) {
                position.put("takeProfit", positionWsDatum.getTakeProfit().stripTrailingZeros().toPlainString());
            } else {
                position.put("takeProfit", "-");
            }


            position.put("side", positionWsDatum.getHoldSide());
            position.put("leverage", positionWsDatum.getLeverage());


            RBucket<AISaveOperatePathData> bucket = redissonClient.getBucket(RedisConstant.AI_TASK_EXT_INFO_KEY + ":" + coinsAiTask.getId() + ":" + positionWsDatum.getSymbol().toUpperCase());

            if (bucket.isExists()) {
                AISaveOperatePathData aiSaveOperatePathData = bucket.get();
                position.put("exit_plan", aiSaveOperatePathData);
            } else {
                position.put("exit_plan", new JSONObject());
            }


            positions.add(position);
        }
        if (!positions.isEmpty()) {
            content.put("positions", positions);
        } else {
            content.put("positions", "no positions");
        }
        String[] split = coinsAiTask.getSymbols().split(",");
        if (split.length <= 0) {
            throw new RuntimeException("币对配置异常");
        }

        List<String> coinList = Arrays.stream(split).toList();
        ArrayList<CompletableFuture<MarketData>> futures = new ArrayList<>();
        for (String coin : coinList) {
            CompletableFuture<MarketData> future = CompletableFuture.supplyAsync(() -> {

                String shortGranularity = "30m";

                String longGranularity = "4H";
                if (StringUtils.isNotEmpty(coinsAiTask.getShortTermInterval())) {
                    shortGranularity = coinsAiTask.getShortTermInterval();
                }
                if (StringUtils.isNotEmpty(coinsAiTask.getLongTermInterval())) {
                    longGranularity = coinsAiTask.getLongTermInterval();
                }


                JSONArray marketCandlesShort = exchangeService.getMarketCandles(account, coin, shortGranularity, CommonConstant.DEFAULT_KLINE_LIMIT_SIZE_AI_TASK);
                BarSeries seriesShort = PromptUtils.getBarSeries(coin, marketCandlesShort);
                JSONArray marketCandlesLong = exchangeService.getMarketCandles(account, coin, longGranularity,  CommonConstant.DEFAULT_KLINE_LIMIT_SIZE_AI_TASK);
                BarSeries seriesLong = PromptUtils.getBarSeries(coin, marketCandlesLong);



                // 资金费
                SymbolFundingRate symbolFundingRate = exchangeService.getSymbolFundingRate(coin);
                //总持仓量
                BigDecimal openInterest = exchangeService.getOpenInterest(account, coin);

                TermData shortTermData = CalcUtils.calcShortTerm(seriesShort,  CommonConstant.DEFAULT_LIMIT_SIZE_AI_TASK);


                TermData longerTermData = CalcUtils.calcLongerTerm(seriesLong, CommonConstant.DEFAULT_LIMIT_SIZE_AI_TASK);

                MarketData marketData = new MarketData();
                TickerItem nowPrice = exchangeService.getNowPrice(account, coin, "");
                if (Objects.isNull(nowPrice)) {
                    return null;
                }
                marketData.setCurrentPrice(nowPrice.getMarkPrice());
                if (Objects.nonNull(nowPrice.getChange24H())) {
                    marketData.setChange24H(nowPrice.getChange24H().multiply(BigDecimal.valueOf(100)).toPlainString() + "%");
                } else {
                    marketData.setChange24H("Unknow");
                }
                //中期行情
                if (Objects.nonNull(coinsAiTask.getNeedMiddleTerm()) && coinsAiTask.getNeedMiddleTerm() == 1) {
                    String middleGranularity = coinsAiTask.getMiddleTermInterval();
                    JSONArray marketCandlesMiddle = exchangeService.getMarketCandles(account, coin, middleGranularity,  CommonConstant.DEFAULT_KLINE_LIMIT_SIZE_AI_TASK);
                    BarSeries seriesMiddle = PromptUtils.getBarSeries(coin, marketCandlesMiddle);
                    TermData middleTermData = CalcUtils.calcMiddleTerm(seriesMiddle, CommonConstant.DEFAULT_LIMIT_SIZE_AI_TASK);
                    marketData.setMiddleTermData(middleTermData);
                    marketData.setSeriesMiddle(seriesMiddle);
                }
                marketData.setShortTerm(shortTermData);
                marketData.setLongTerm(longerTermData);
                marketData.setFundingRate(symbolFundingRate);
                marketData.setOpenInterest(openInterest);
                marketData.setSymbol(coin);
                marketData.setSeriesShort(seriesShort);
                marketData.setSeriesLong(seriesLong);

                return marketData;
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        JSONObject market = new JSONObject();
        ArrayList<MarketData> marketDatas = new ArrayList<>();
        for (CompletableFuture<MarketData> future : futures) {
            MarketData data = future.get();
            marketDatas.add(data);
        }
        content.put("market", market);

        JSONObject preferences = new JSONObject();
//        preferences.put("leverage_range", List.of(1,10));
//        preferences.put("risk_level","moderate");
//        preferences.put("trading_horizon","short-term");
        content.put("preferences", preferences);

        List<HistoryPosition> historyPositions = exchangeService.queryContractHistoryPosition(account, 15L, new HistoryPositionQuery());
//        List<PositionSharpeCalculator.Position> poslist = historyPositions.stream().map(item -> {
//            return new PositionSharpeCalculator.Position(item.getSymbol(), item.getPnlRate().doubleValue(), item.getDurationHours());
//        }).collect(Collectors.toList());
//        double sharpeRatio = PositionSharpeCalculator.calculateWeightedSharpeRatio(poslist, 0.0d);

        Double sharpeRatio = apiPositionService.querySharpeRatioByApiIdAndStartTime(coinsAiTask.getApiId(), coinsAiTask.getCreateTime());

        Date startTime = coinsAiTask.getStartTime();
        LocalDateTime localDateTime = startTime.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        String extConfig = coinsAiTask.getExtConfig();

        List<ExtConfigItem> userPromptSetting = PromptUtils.getUserPromptSetting(extConfig);
        //组合用户自定义参数规则和数据
        boolean hasMiddle = Objects.nonNull(coinsAiTask.getNeedMiddleTerm()) && coinsAiTask.getNeedMiddleTerm() == 1;
        List<MarketDataPromptRule> marketDataPromptRules = PromptUtils.initRuleData(userPromptSetting, marketDatas ,hasMiddle);

        String userPrompt = getUserPrompt(marketDatas, localDateTime, invokedTimes, basePrice, balance, positions, sharpeRatio
            , historyPositions, coinsAiTask, marketDataPromptRules);


        log.info("content:\n {}", userPrompt);


        Map<String, Object> inputs = new HashMap<>();
        inputs.put("content", userPrompt);
        inputs.put("historyPosition", JSONObject.toJSONString(historyPositions));
        log.info("historyPosition {}", historyPositions);
        String snowflakeNextIdStr = IdUtil.getSnowflakeNextIdStr();
        inputs.put("requestKey", snowflakeNextIdStr);
        inputs.put("taskId", coinsAiTask.getId() + "");
        String systemPromptTmp = promptService.loadSystemPrompt();
        final String systemPrompt = promptService.formatPrompt(systemPromptTmp, coinsAiTask);
        inputs.put("systemPrompt", coinsAiTask.getSystemPrompt() + systemPrompt);


        executorService.submit(() -> {
            String markdown = "## 发起推测\n>时间：" + dateTimeFormatter.format(LocalDateTime.now()) + "\n" + userPrompt;
            String positionMsg = "## 当前仓位\n>时间：" + dateTimeFormatter.format(LocalDateTime.now()) + "\n" + JSONObject.toJSONString(positions, JSONWriter.Feature.PrettyFormatWith2Space);
            String balanceMsg = "## 余额 \n 时间:" + dateTimeFormatter.format(LocalDateTime.now()) + "\n" + JSONObject.toJSONString(balance, JSONWriter.Feature.PrettyFormatWith2Space);

//            coinsNotifyService.sendNotification(coinsAiTask.getCreateUserId(), markdown);
//            coinsNotifyService.sendNotification(coinsAiTask.getCreateUserId(), positionMsg);
//            coinsNotifyService.sendNotification(coinsAiTask.getCreateUserId(), balanceMsg);
            //存库
            CoinAITaskBalanceBo coinAITaskBalanceBo = new CoinAITaskBalanceBo();
            coinAITaskBalanceBo.setTime(new Date());
            coinAITaskBalanceBo.setEquity(balance.getEquity().setScale(2, BigDecimal.ROUND_HALF_UP));
            coinAITaskBalanceBo.setFreeBalance(balance.getFreeBalance().setScale(2, BigDecimal.ROUND_HALF_UP));
            coinAITaskBalanceBo.setTaskId(coinsAiTask.getId());
            coinAITaskBalanceBo.setCreateBy(coinsAiTask.getCreateUserId());
            coinAITaskBalanceBo.setCreateTime(new Date());
            coinTestAiService.insertByBo(coinAITaskBalanceBo);

            coinTestAiRequest.setRequestKey(snowflakeNextIdStr);
            coinTestAiRequest.setContent(userPrompt);
            coinTestAiRequest.setSysContent(coinsAiTask.getSystemPrompt());
            coinTestAiRequest.setTaskId(coinsAiTask.getId());
            coinTestAiRequest.setStatus(1L);
            coinTestAiRequest.setAiId(coinsAiTask.getAiWorkflowId());
            coinTestAiRequest.setCreateBy(coinsAiTask.getCreateUserId());
            coinTestAiRequest.setCreateTime(new Date());

            coinTestAiRequestService.updateByBo(coinTestAiRequest);
        });

        Long aiWorkflowId = coinsAiTask.getAiWorkflowId();
        CoinsAiConfig coinsAiConfig = coinsAiMapper.selectById(aiWorkflowId);
        if (Objects.isNull(coinsAiConfig)) {
            throw new RuntimeException("AI agent Config NULL");
        }
        log.info("coinsAiConfigVo:{}", JSONObject.toJSONString(coinsAiConfig, JSONWriter.Feature.PrettyFormatWith2Space));
        String url = coinsAiConfig.getUrl();
        String token = coinsAiConfig.getToken();
        inputs.put("callback", coinsAiConfig.getCallback());
//        String result = difyWorkflowUtils.runWorkflow("app-mMcTThYQFT1e8DmzxE76DN9D", inputs,"bitstrat server","blocking");
        String result = difyWorkflowUtils.runWorkflow(url, token, inputs, "bitstrat server", "blocking");
        log.info("result:{}", result);
        String key = "aiRequestKey:" + snowflakeNextIdStr;
        RBucket<String> bucket = redissonClient.getBucket(key);

        // 验证设置
        bucket.set(snowflakeNextIdStr, Duration.ofMinutes(30L));
        if (snowflakeNextIdStr.equals(bucket.get())) {
            log.error("✅ 设置成功，键: {}, TTL: {}", key, bucket.remainTimeToLive());
        } else {
            log.error("❌ 设置失败，值不匹配");
        }

    }

    private void saveExtInfo(AIOperateItem aiOperate) {
        AISaveOperatePathData aiSaveOperatePathData = new AISaveOperatePathData();
        aiSaveOperatePathData.setSymbol(aiOperate.getSymbol());
        aiSaveOperatePathData.setConfidence(aiOperate.getConfidence());
        aiSaveOperatePathData.setRiskUsd(aiOperate.getRiskUsd());
        aiSaveOperatePathData.setInvalidationCondition(aiOperate.getInvalidationCondition());
        RBucket<AISaveOperatePathData> bucket = redissonClient.getBucket(RedisConstant.AI_TASK_EXT_INFO_KEY + ":" + aiOperate.getTaskId() + ":" + aiOperate.getSymbol());
        bucket.set(aiSaveOperatePathData);
    }

    private void removeExtInfo(AIOperateItem aiOperate) {
        RBucket<AISaveOperatePathData> bucket = redissonClient.getBucket(RedisConstant.AI_TASK_EXT_INFO_KEY + ":" + aiOperate.getTaskId() + ":" + aiOperate.getSymbol());
        bucket.delete();
    }

    private void reducePosition(AIOperateItem aiOperate, Account account, ExchangeService exchangeService) {
        List<PositionWsData> positionWsData = exchangeService.queryContractPositionDetail(account, new PositionParams());
        for (PositionWsData positionWsDatum : positionWsData) {
            if (positionWsDatum.getSymbol().startsWith(aiOperate.getSymbol())) {
                if (positionWsDatum.getSide().equalsIgnoreCase(SideType.SHORT)) {
                    //空仓就买回来
                    OrderVo orderVo = new OrderVo();
                    orderVo.setSymbol(aiOperate.getSymbol());
//                    orderVo.setStopPrice(aiOperate.getStopLoss());
//                    orderVo.setStopLossPrice(aiOperate.getTakeProfit());
                    orderVo.setSize(exchangeService.calcOrderSize(aiOperate.getSymbol(), aiOperate.getSize()));
                    orderVo.setLeverage(aiOperate.getLeverage());
                    //            orderVo.setPrice(resultV.getInPrice());
                    orderVo.setOrderType(OrderType.MARKET);
                    orderVo.setReduceOnly(true);
                    exchangeService.buyContract(account, orderVo);

                } else if (positionWsDatum.getSide().equalsIgnoreCase(SideType.LONG)) {
                    //多仓就卖掉
                    OrderVo orderVo = new OrderVo();
                    orderVo.setSymbol(aiOperate.getSymbol());
                    orderVo.setSize(exchangeService.calcOrderSize(aiOperate.getSymbol(), aiOperate.getSize()));
                    orderVo.setLeverage(aiOperate.getLeverage());
                    //            orderVo.setPrice(resultV.getInPrice());
                    orderVo.setOrderType(OrderType.MARKET);
                    orderVo.setReduceOnly(true);
                    exchangeService.sellContract(account, orderVo);
                }

            }
        }

    }

    private static void setTpSl(AIOperateItem aiOperate, Account account, ExchangeService exchangeService) {
        List<PositionWsData> positionWsData = exchangeService.queryContractPositionDetail(account, new PositionParams());
        for (PositionWsData positionWsDatum : positionWsData) {
            if (positionWsDatum.getSymbol().startsWith(aiOperate.getSymbol())) {
                //先查询是否有计划委托
                List<? extends TpSlOrder> tpSlOrders = exchangeService.queryContractTpSlOrder(account, aiOperate.getSymbol());

                for (TpSlOrder tpSlOrder : tpSlOrders) {
                    if (tpSlOrder instanceof EntrustedData entrustedData) {
                        if (entrustedData.getSymbol().startsWith(aiOperate.getSymbol().toLowerCase())) {
                            //找到了
                            if (entrustedData.getPlanType().equalsIgnoreCase("pos_profit")) {
                                //仓位止盈
                                //修改计划委托

                                UpdateTpSl updateTpSl = new UpdateTpSl();
                                updateTpSl.setSymbol(aiOperate.getSymbol());
                                updateTpSl.setExecutePrice(aiOperate.getTakeProfit().toPlainString());
                                updateTpSl.setMarginCoin("USDT");
                                updateTpSl.setProductType("usdt-futures");
                                updateTpSl.setTriggerType("mark_price");
                                updateTpSl.setSize("");
                                updateTpSl.setOrderId(entrustedData.getOrderId());
                                exchangeService.updateContractTpSl(account, aiOperate.getSymbol(), updateTpSl);
                            }
                            if (entrustedData.getPlanType().equalsIgnoreCase("pos_loss")) {
                                //仓位止损
                                UpdateTpSl updateTpSl = new UpdateTpSl();
                                updateTpSl.setSymbol(aiOperate.getSymbol());
                                updateTpSl.setExecutePrice(aiOperate.getStopLoss().toPlainString());
                                updateTpSl.setMarginCoin("USDT");
                                updateTpSl.setProductType("usdt-futures");
                                updateTpSl.setTriggerType("mark_price");
                                updateTpSl.setSize("");
                                updateTpSl.setOrderId(entrustedData.getOrderId());
                                exchangeService.updateContractTpSl(account, aiOperate.getSymbol(), updateTpSl);
                            }

                        }
                    }

                    /**
                     * okx 仓位数据
                     * okx 止盈止损在一个订单上
                     */
                    if (tpSlOrder instanceof OkxTpSlOrderItem okxTpSlOrderItem) {
                        UpdateTpSlOkx updateTpSlOkx = new UpdateTpSlOkx();
                        updateTpSlOkx.setSymbol(aiOperate.getSymbol());
                        updateTpSlOkx.setAlgoId(okxTpSlOrderItem.getAlgoId());
                        updateTpSlOkx.setNewTpTriggerPx(aiOperate.getTakeProfit().toPlainString());
                        updateTpSlOkx.setNewSlTriggerPx(aiOperate.getStopLoss().toPlainString());
                        exchangeService.updateContractTpSl(account, aiOperate.getSymbol(), updateTpSlOkx);
                    }
                }
                if (CollectionUtils.isEmpty(tpSlOrders)) {
                    //没找到
                    //创建计划委托
                    CreateTpSlOnce createTpSlOnce = new CreateTpSlOnce();
                    createTpSlOnce.setSymbol(aiOperate.getSymbol());
                    createTpSlOnce.setProductType("usdt-futures");
                    createTpSlOnce.setMarginCoin("USDT");

                    //止盈
                    createTpSlOnce.setStopSurplusTriggerType("mark_price");
                    createTpSlOnce.setStopSurplusTriggerPrice(aiOperate.getTakeProfit().toPlainString());
                    createTpSlOnce.setStopSurplusSize(null);

                    //止损
                    createTpSlOnce.setStopLossTriggerType("mark_price");
                    createTpSlOnce.setStopLossTriggerPrice(aiOperate.getStopLoss().toPlainString());
                    createTpSlOnce.setStopLossSize(null);

                    //方向
                    createTpSlOnce.setHoldSide(positionWsDatum.getHoldSide());
                    createTpSlOnce.setSide(positionWsDatum.getSide());

                    exchangeService.createTpSl(account, aiOperate.getSymbol(), createTpSlOnce);
                }

            }
        }
    }


    private String getUserPrompt(ArrayList<MarketData> marketDatas, LocalDateTime startDate, long invokedTimes, BigDecimal basePrice, AccountBalance balance, JSONArray positions
        , double sharpeRatio, List<HistoryPosition> historyPositions, CoinsAiTask coinsAiTask, List<MarketDataPromptRule> marketDataPromptRules) {
        Map<String, MarketDataPromptRule> ruleMap = marketDataPromptRules.stream().collect(Collectors.toMap(MarketDataPromptRule::getSymbol, item -> item, (a, b) -> a));

        StringBuilder userPrompt = new StringBuilder();
        String prompt_1 = """
            It has been {minutes} minutes since you started trading. The current time is {currentTime} and you've been invoked {invokedTimes} times. Below, we are providing you with a variety of state data, price data, and predictive signals so you can discover alpha. Below that is your current account information, value, performance, positions, etc.
            """;
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(startDate, now);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        prompt_1 = prompt_1.replace("{minutes}", duration.toMinutes() + "");
        prompt_1 = prompt_1.replace("{currentTime}", dateTimeFormatter.format(now));
        prompt_1 = prompt_1.replace("{invokedTimes}", invokedTimes + "");
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
            MarketDataPromptRule symbolRoleData = ruleMap.get(marketData.getSymbol());


            TermData shortTerm = marketData.getShortTerm();
            TermData longTerm = marketData.getLongTerm();

            userPrompt.append("ALL " + marketData.getSymbol().toUpperCase() + " DATA").append("\n");
            String dataPath1 = """
                current_price = {current_price}""";
            dataPath1 = dataPath1.replace("{current_price}", marketData.getCurrentPrice());
            if (CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.EMA20, true)) {
                dataPath1 = dataPath1 + ", current_ema20 = "+shortTerm.getEma20Value();
            }
            if (CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.MACD, true)) {
                dataPath1 = dataPath1 + ", current_macd = "+shortTerm.getMacdValue();
            }
            if (CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI7, true)) {
                dataPath1 = dataPath1 + ", current_rsi (7 period) = "+shortTerm.getRsi7Value();
            }
            if (CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI14, true)) {
                dataPath1 = dataPath1 + ", current_rsi (14 period) = "+shortTerm.getRsi14Value();
            }
            userPrompt.append(dataPath1).append("\n");
            String title =  """
                In addition, here is the latest {symbol} open interest {fundRate} for perps (the instrument you are trading):
                """;
            title = title.replace("{symbol}", marketData.getSymbol().toUpperCase());
            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.FundingRate, true)){
                title = title.replace("{fundRate}","and funding rate");
            }
            userPrompt.append(title).append("\n");


            String dataPath2 = """
                Open Interest: Latest: {openInterest}
                """;
            dataPath2 = dataPath2.replace("{openInterest}", marketData.getOpenInterest().toPlainString());
            userPrompt.append(dataPath2).append("\n");

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.FundingRate, true)){
                String dataPath3 = """
                Funding Rate: {fundingRate} Next Funding Time: {nextPayTime} (Funding fee = Position value × Funding rate.)
                """;
                dataPath3 = dataPath3.replace("{fundingRate}", marketData.getFundingRate().getFundingRate().toPlainString());
                dataPath3 = dataPath3.replace("{nextPayTime}", marketData.getFundingRate().getNextFundingTime()+"");
                userPrompt.append(dataPath3).append("\n");
            }



            String dataPath3Title = """
                Intraday series (by {shortInterval}, oldest → latest):
                """;
            if (StringUtils.isNotEmpty(coinsAiTask.getShortTermInterval())) {
                dataPath3Title = dataPath3Title.replace("{shortInterval}", PromptUtils.convertInterval(coinsAiTask.getShortTermInterval()));
            } else {
                dataPath3Title = dataPath3Title.replace("{shortInterval}", "5-minute");
            }
            userPrompt.append(dataPath3Title).append("\n");
            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.Mid, true)){
                String datapath4 = """
                Mid prices: {Mid}
                """;
                datapath4 = datapath4.replace("{Mid}", shortTerm.getMidPriceString().toString());
                userPrompt.append(datapath4).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.EMA20, true)){
                String datapathEMAIndicators = """
                EMA indicators (20‑period): {ema20period}
                """;
                datapathEMAIndicators = datapathEMAIndicators.replace("{ema20period}", shortTerm.getEma20PeriodString().toString());
                userPrompt.append(datapathEMAIndicators).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.MACD, true)){
                String dataPathMACDIndicators = """
                MACD indicators: {macdIndicators}
                """;
                dataPathMACDIndicators = dataPathMACDIndicators.replace("{macdIndicators}", shortTerm.getMacdIndicatorsString().toString());
                userPrompt.append(dataPathMACDIndicators).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI7, true)){
                String dataPathRSIIndicators = """
                RSI indicators (7‑Period): {rsi7period}
                """;
                dataPathRSIIndicators = dataPathRSIIndicators.replace("{rsi7period}", shortTerm.getrsi7periodString().toString());
                userPrompt.append(dataPathRSIIndicators).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI14, true)){
                String dataPathRSIndicators14 = """
                RSI indicators (14‑Period): {rsi14period}
                """;
                dataPathRSIndicators14 = dataPathRSIndicators14.replace("{rsi14period}", shortTerm.getrsi14periodString().toString());
                userPrompt.append(dataPathRSIndicators14).append("\n");
            }
            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.BOLL, true)){
                userPrompt.append(longTerm.getBollingerBandsValue());
            }

            //------------------------自定义参数 short
            String shortPrompt = symbolRoleData.getShortPrompt();
            if (StringUtils.isNoneBlank(shortPrompt)) {
                userPrompt.append("\n");
                userPrompt.append(shortPrompt).append("\n");
            }
            //--------------------------中期行情开始

            if(Objects.nonNull(coinsAiTask.getNeedMiddleTerm()) && coinsAiTask.getNeedMiddleTerm() == 1){
                TermData middleTermData = marketData.getMiddleTermData();
                PromptUtils.generateMiddleTerm(coinsAiTask, userPrompt, symbolRoleData, middleTermData);
            }


            //--------------------------长期行情开始
            String dataPathTitle5 = """
                Longer‑term context ({longInterval} timeframe):
                """;
            if (StringUtils.isNoneEmpty(coinsAiTask.getLongTermInterval())) {
                dataPathTitle5 = dataPathTitle5.replace("{longInterval}", PromptUtils.convertInterval(coinsAiTask.getLongTermInterval()));
            } else {
                dataPathTitle5 = dataPathTitle5.replace("{longInterval}", "4-hour");
            }
            userPrompt.append(dataPathTitle5).append("\n");
            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.EMA20vsEMA50, true)){

                String dataPathLonggerEMA = """
                20‑Period EMA: {20PeriodEMA} vs. 50‑Period EMA: {50PeriodEMA}
                """;
                dataPathLonggerEMA = dataPathLonggerEMA.replace("{20PeriodEMA}", longTerm.getEma20Value());
                dataPathLonggerEMA = dataPathLonggerEMA.replace("{50PeriodEMA}", longTerm.getEma50Value());
                userPrompt.append(dataPathLonggerEMA).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.ATR3vsATR14, true)) {
                String dataPathATR = """
                    3‑Period ATR: {ATR3} vs. 14‑Period ATR: {ATR14}
                    """;
                dataPathATR = dataPathATR.replace("{ATR3}", longTerm.getAtr3().toPlainString());
                dataPathATR = dataPathATR.replace("{ATR14}", longTerm.getAtr14().toPlainString());
                userPrompt.append(dataPathATR).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.CVvsAV, true)){
                String dataPathVolume = """
                Current Volume: {volume} vs. Average Volume: {volumeAVG}
                """;
                dataPathVolume = dataPathVolume.replace("{volume}", longTerm.getCurrentVolume().toPlainString());
                dataPathVolume = dataPathVolume.replace("{volumeAVG}", longTerm.getAverageVolume().toPlainString());
                userPrompt.append(dataPathVolume).append("\n");
            }

            if (CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.MACD, true)) {
                String dataPathMACDIndicatorsLong = """
                MACD indicators (12,26): {macdIndicators}
                """;
                dataPathMACDIndicatorsLong = dataPathMACDIndicatorsLong.replace("{macdIndicators}", longTerm.getMacdIndicatorsString().toString());
                userPrompt.append(dataPathMACDIndicatorsLong).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI7, true)){
                String dataPathRSIndicators7Long = """
                RSI indicators (7‑Period): {rsi7period}
                """;
                dataPathRSIndicators7Long = dataPathRSIndicators7Long.replace("{rsi7period}", longTerm.getRsi7period().toString());
                userPrompt.append(dataPathRSIndicators7Long).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI14, true)){
                String dataPathRSIndicators14Long = """
                RSI indicators (14‑Period): {rsi14period}
                """;
                dataPathRSIndicators14Long = dataPathRSIndicators14Long.replace("{rsi14period}", longTerm.getRsi14period().toString());
                userPrompt.append(dataPathRSIndicators14Long).append("\n");
            }

            if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.BOLL, true)){
                userPrompt.append(longTerm.getBollingerBandsValue());
            }

            //------------------------自定义参数 long
            String longPrompt = symbolRoleData.getLongPrompt();
            if (StringUtils.isNoneBlank(longPrompt)) {
                userPrompt.append("\n");
                userPrompt.append(longPrompt).append("\n");
            }

        }

        //******* 开始当前持仓部分
        userPrompt.append("""
            HERE IS YOUR ACCOUNT INFORMATION & PERFORMANCE
            """).append("\n");

        //盈利状态
        // 计算 totalReturnPercent = (current / base - 1) * 100
        BigDecimal ratio = balance.getEquity().divide(basePrice, 10, RoundingMode.HALF_UP);
        BigDecimal totalReturn = ratio.subtract(BigDecimal.ONE)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP); // 保留两位小数
        String totalReturnPercent = """
            Current Total Return (percent): {percent}%
            Sharpe Ratio: {sharpe_ratio}
            """;
        totalReturnPercent = totalReturnPercent.replace("{percent}", totalReturn.toString());
        totalReturnPercent = totalReturnPercent.replace("{sharpe_ratio}", sharpeRatio + "");
        userPrompt.append(totalReturnPercent).append("\n");
        userPrompt.append("Available Cash: " + balance.getFreeBalance().toPlainString() + "USDT").append("\n");
        userPrompt.append("Current Account Value: " + balance.getEquity().toPlainString() + "USDT").append("\n");

        String positionStr = "Current live positions & performance: \n" + PromptUtils.covertPosition(positions);
        userPrompt.append(positionStr).append("\n");
        String positionHisStr = "History positions (limit ~10) & performance:\n";
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
            } else {
                item = item.replace("{openAvg}", "-");
            }
            if (Objects.nonNull(historyPosition.getCloseAvgPrice())) {
                item = item.replace("{closeAvg}", historyPosition.getCloseAvgPrice().toPlainString());
            } else {
                item = item.replace("{closeAvg}", "-");
            }
            if (Objects.nonNull(historyPosition.getPnl())) {
                item = item.replace("{pnl}", historyPosition.getPnl().toPlainString());
            } else {
                item = item.replace("{pnl}", "-");
            }
            if (Objects.nonNull(historyPosition.getNetProfit())) {
                item = item.replace("{netProfit}", historyPosition.getNetProfit().toPlainString());
            } else {
                item = item.replace("{netProfit}", "-");
            }

            if (Objects.nonNull(historyPosition.getTotalFunding())) {
                item = item.replace("{fundingFee}", historyPosition.getTotalFunding().toPlainString());
            } else {
                item = item.replace("{fundingFee}", "-");
            }

            if (Objects.nonNull(historyPosition.getOpenFee())) {
                item = item.replace("{openFee}", historyPosition.getOpenFee().toPlainString());
            } else {
                item = item.replace("{openFee}", "-");
            }
            if (Objects.nonNull(historyPosition.getCloseFee())) {
                item = item.replace("{closeFee}", historyPosition.getCloseFee().toPlainString());
            } else {
                item = item.replace("{closeFee}", "-");
            }
            item = item.replace("{createTime}", historyPosition.getCtimeFormat());
            item = item.replace("{closeTime}", historyPosition.getUtimeFormat());

            if (Objects.nonNull(historyPosition.getCloseTotalPos())) {
                item = item.replace("{closeSize}", historyPosition.getCloseTotalPos().toPlainString());
            } else {
                item = item.replace("{closeSize}", "-");
            }

            hispos.append(item).append("\n");
        }
        userPrompt.append(hispos.toString()).append("\n");


        return userPrompt.toString();
    }

}
