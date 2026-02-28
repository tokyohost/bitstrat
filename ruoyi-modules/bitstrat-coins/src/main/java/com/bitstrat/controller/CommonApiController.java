package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bitstrat.config.DeviceConnectionManager;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.bybit.BybitSymbolInfo;
import com.bitstrat.domain.bybit.LinerSymbolItem;
import com.bitstrat.domain.msg.SubscribeSymbol;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.vo.*;
import com.bitstrat.init.SyncCoinsRank;
import com.bitstrat.service.*;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.strategy.*;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.restApi.BybitApiCallback;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.event.OperLogEvent;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.service.ISysConfigService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.bitstrat.constant.ServiceConstant.EXCHANGE_ATTR;

/**
 * @author tokyohostcoder
 * @Version 1.0
 * @date 2025/4/1 10:14
 * @Content
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonApiController extends BaseController {
    LoadingCache<String, List<LinerSymbol>> exLinerSymbolCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build(new CacheLoader<String, List<LinerSymbol>>() {
            @Override
            public List<LinerSymbol> load(String ex) throws Exception {
                // 如果缓存中没有该值，自动调用 load 方法加载值
                List<LinerSymbol> allLinerSymbolByEx = commonServce.getAllLinerSymbolByEx(ex);
                if(CollectionUtils.isEmpty(allLinerSymbolByEx)){
                    throw new RuntimeException("获取币对为空");
                }
                return allLinerSymbolByEx;
            }
        });
    LoadingCache<String, Integer> exSymbolFundingIntervalCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build(new CacheLoader<String,Integer>() {
            @Override
            public Integer load(String ex) throws Exception {
                // 如果缓存中没有该值，自动调用 load 方法加载值
                String[] split = ex.split(":");

                return commonServce.getSymbolFundingTimeIntervalByEx(split[0],split[1]);
            }
        });
    @Autowired
    BybitService bybitService;

    @Autowired
    StrategyManager strategyManager;
    @Autowired
    PositionStrategyManager positionStrategyManager;
    @Autowired
    private DeviceConnectionManager connectionManager;
    @Autowired
    private KeyCryptoService keyCryptoService;


    @Autowired
    ICoinsTaskService taskService;

    @Autowired
    ICommonService commonServce;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    ISysConfigService sysConfigService;




    @Autowired
    ICoinsApiService coinsApiService;

    @GetMapping("/bybit/symbols")
    public R<List<BybitSymbolInfo>> bybitSymbols(HttpServletRequest request, HttpServletResponse response) {
        List<BybitSymbolInfo> symbols = bybitService.getSymbolsLiner();
        return R.ok(symbols);
    }

    @GetMapping("/bybit/supportNormalStrategy")
    public R<List<StrategyVo>> supportNormalStrategy() {
        List<NormalStrategy> strategyList = strategyManager.getStrategyList();
        ArrayList<StrategyVo> vos = new ArrayList<>();
        for (NormalStrategy normalStrategy : strategyList) {
            StrategyVo strategyVo = new StrategyVo();
            strategyVo.setId(normalStrategy.typeId());
            strategyVo.setName(MessageUtils.message("bybit.task.normal."+normalStrategy.typeName()));
            vos.add(strategyVo);
        }
        return R.ok(vos);
    }
    @GetMapping("/bybit/supportPositionStrategy")
    public R<List<PositionStrategyVo>> supportPositionStrategy() {
        List<PositionStrategy> strategyList = positionStrategyManager.getStrategyList();
        ArrayList<PositionStrategyVo> vos = new ArrayList<>();
        for (PositionStrategy strategy : strategyList) {
            PositionStrategyVo strategyVo = new PositionStrategyVo();
            strategyVo.setId(strategy.typeId());
            strategyVo.setName(MessageUtils.message("bybit.task.position."+strategy.typeName()));
            strategyVo.setDescription(MessageUtils.message("bybit.task.position."+strategy.desc()));
            vos.add(strategyVo);
        }
        return R.ok(vos);
    }

    @GetMapping("/bybit/marketInterval")
    public R<List<MarketIntervalVo>> marketInterval() {
        MarketIntervalKV[] values = MarketIntervalKV.values();

        ArrayList<MarketIntervalVo> vos = new ArrayList<>();
        for (MarketIntervalKV value : values) {
            MarketIntervalVo vo = new MarketIntervalVo();
            vo.setInterval(value.getCode());
            vo.setName(MessageUtils.message("bybit.task.market.interval."+value.getCode()));
            vos.add(vo);
        }
        return R.ok(vos);
    }

    @GetMapping("/getSupportExchange")
    public R<List<ExchangeTypeVo>> getSupportExchange() {
        ExchangeType[] values = ExchangeType.values();
        ArrayList<ExchangeTypeVo> exchangeTypeVos = new ArrayList<>();
        for (ExchangeType value : values) {
            if (value.getDisabled()) {
                continue;
            }
            ExchangeTypeVo exchangeTypeVo = new ExchangeTypeVo();
            exchangeTypeVo.setName(value.getName());
            exchangeTypeVo.setDesc(value.getDesc());
            exchangeTypeVos.add(exchangeTypeVo);

        }

        return R.ok(exchangeTypeVos);
    }

    @GetMapping("/bybit/testMa")
    @SaIgnore
    public R<JSONObject> testMa() {
        List<List<BigDecimal>> trxusdt = commonServce.getKlinesData(commonServce.getByBitAccount(), "TRXUSDT", "15");

        // 创建 BarSeries
        BarSeries series = new BaseBarSeriesBuilder().withName("TRXUSDT").build();

        // 添加 K 线数据，使用收盘价（close）
        for (List<BigDecimal> kline : trxusdt) {
            ZonedDateTime startTime = Instant.ofEpochMilli(kline.get(0).longValue()).atZone(ZoneId.systemDefault());
            ZonedDateTime endTime = startTime.plusMinutes(15);
            Duration duration = Duration.between(startTime, endTime);
            double open = kline.get(1).doubleValue();
            double hight = kline.get(2).doubleValue();
            double low = kline.get(3).doubleValue();
            double close = kline.get(4).doubleValue();
            double volume = kline.get(5).doubleValue();
            BaseBar baseBar = new BaseBar(duration, endTime, open, hight, low, close, volume);
            series.addBar(baseBar);
        }

        // 计算 MA7
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator ma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator ma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator ma20 = new SMAIndicator(closePrice, 20);
        SMAIndicator ma30 = new SMAIndicator(closePrice, 30);

        // 4. 获取最新均线值
        int lastIndex = series.getBarCount() - 1;
        JSONObject data = new JSONObject();
        data.put("ma5", ma5.getValue(lastIndex));
        data.put("ma10", ma10.getValue(lastIndex));
        data.put("ma20", ma20.getValue(lastIndex));
        data.put("ma30", ma30.getValue(lastIndex));
        return R.ok(data);
    }

    @GetMapping("/bybit/testTask")
    @SaIgnore
    public R<JSONObject> testTask(Long id,Integer stype) {
        CoinsTaskVo coinsTaskVo = taskService.queryById(id);
        NormalStrategy strategy = strategyManager.getStrategy(stype);
        strategy.run(coinsTaskVo,new OperLogEvent());

        return R.ok();
    }
    @GetMapping("/bybit/testMarket")
    @SaIgnore
    public R<JSONObject> testMarket(Long id,Integer stype) {
        JSONObject btcusdt = commonServce.getMarketInfo(commonServce.getByBitAccount(), "BTCUSDT");
        log.info(btcusdt.toJSONString());
        return R.ok(btcusdt);
    }

    @Autowired
    SyncCoinsRank syncCoinsRank;
    @GetMapping("/bybit/testsync")
    @SaIgnore
    public R<JSONObject> testsync(Long id,Integer stype) throws Exception {
        syncCoinsRank.run();
        return R.ok();
    }

    @GetMapping("/bybit/queryOrder")
    @SaIgnore
    public R<JSONObject> queryOrder(String orderId) {
        JSONObject jsonObject = commonServce.queryOrderStatus(orderId, commonServce.getByBitAccount());

        return R.ok(jsonObject);
    }
    @GetMapping("/bybit/queryPosition")
    @SaIgnore
    public R<JSONObject> queryPosition(String symbol) {
        ByBitAccount byBitAccount = commonServce.getByBitAccount();
        var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(),byBitAccount.getApiPwd()).newAsyncPositionRestClient();
        var positionListRequest = PositionDataRequest.builder().category(CategoryType.SPOT).symbol(symbol).build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<JSONObject> result = new AtomicReference<>();
        client.getPositionInfo(positionListRequest, new BybitApiCallback() {
            @Override
            public void onResponse(Object response) {
                result.set(JSONObject.from(response));
                log.info(response.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Throwable cause) {
                BybitApiCallback.super.onFailure(cause);
                countDownLatch.countDown();
                throw new RuntimeException(cause);
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return R.ok(result.get());
    }



    @GetMapping("/bybit/queryInstrumentsInfo")
    @SaIgnore
    public R<JSONObject> queryInstrumentsInfo(String orderId) {
        ByBitAccount byBitAccount = commonServce.getByBitAccount();
        var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(),byBitAccount.getApiPwd()).newMarketDataRestClient();
        MarketDataRequest marketDataRequest = MarketDataRequest.builder().category(commonServce.getCateType()).build();
        Object instrumentsInfo = client.getInstrumentsInfo(marketDataRequest);
        JSONObject from = JSONObject.from(instrumentsInfo);
        return R.ok(from);
    }
    @GetMapping("/queryNodeStatus")
    @SaIgnore
    public R<List<DeviceInfo>> queryNodeStatus() {
        List<DeviceInfo> allDeviceInfo = connectionManager.getAllDeviceInfo();
        R<List<DeviceInfo>> listR = new R<>();
        listR.setData(allDeviceInfo);
        return listR;
    }
    @GetMapping("/queryExchangeStatus")
    @SaIgnore
    public R<List<MergedExchangeData>> queryExchangeStatus() {
        List<ExchangeData> allDeviceInfo = connectionManager.getAllExchangeInfo();
        List<MergedExchangeData> merged = allDeviceInfo.stream()
            .collect(Collectors.groupingBy(ExchangeData::getClientId))
            .entrySet()
            .stream()
            .map(entry -> {
                String clientId = entry.getKey();
                List<ExchangeData> groupList = entry.getValue();
                String exchangeNames = groupList.stream()
                    .map(ExchangeData::getExchangeName)
                    .distinct() // 可选，去重
                    .collect(Collectors.joining(","));
                String nodeName = groupList.get(0).getNodeName(); // 假设相同 clientId 的 nodeName 是一样的
                Long delay = groupList.get(0).getDelay(); // 同理
                String ip = groupList.get(0).getIp(); // 同理
                String status = groupList.get(0).getStatus(); // 同理

                return new MergedExchangeData(exchangeNames, nodeName, clientId, delay,ip,status);
            })
            .collect(Collectors.toList());
        R<List<MergedExchangeData>> listR = new R<>();
        listR.setData(merged);
        return listR;
    }
    @GetMapping("/getMarketData")
    @SaIgnore
    public R getMarketData() {
        Channel bybit = connectionManager.getExchangeNode("bybit");
        if(bybit != null) {
            SubscribeSymbol subscribeSymbol = new SubscribeSymbol();
            subscribeSymbol.setSymbolType(SymbolType.LINER);
            ArrayList<String> symbols = new ArrayList<>();
            symbols.add("BTCUSDT");
            subscribeSymbol.setSymbols(symbols);
            Message message = new Message();
            message.setType(MessageType.SUBSCRIPTION_SYMBOL);
            message.setData(subscribeSymbol);
            message.setTimestamp(System.currentTimeMillis());
            message.setExchangeName(bybit.attr(EXCHANGE_ATTR).get());

            bybit.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
        }

        return R.ok();
    }

    @SaCheckLogin
    @PostMapping("/setLeverageBody")
    public R stop(@RequestBody SetLeverageBody body) {
        return commonServce.setLeverageBody(body);
    }


    /**
     * 查自己那个交易所的指定币种余额
     * @param body
     * @return
     */
    @SaCheckLogin
    @PostMapping("/queryBalanceByEx")
    public R queryBalanceByEx(@RequestBody QueryBalanceBody body) {
        Long userId = LoginHelper.getUserId();
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<CoinsApi> eq = queryWrapper.lambda().eq(CoinsApi::getUserId, userId)
            .eq(CoinsApi::getExchangeName, body.getExchange().toLowerCase())
            .eq(Objects.nonNull(body.getApiId()),CoinsApi::getId, body.getApiId())
            .last("limit 1");
        CoinsApi coinsApi = coinsApiService.getBaseMapper().selectOne(eq);
        keyCryptoService.decryptApi(coinsApi);
        return commonServce.queryBalanceByEx(coinsApi,body);
    }

    /**
     * 查自己那个交易所的指定币种余额
     * @param body
     * @return
     */
    @SaCheckLogin
    @PostMapping("/queryFeeByExSymbol")
    public R queryFeeByExSymbol(@RequestBody QueryFeeBody body) {
        Long userId = LoginHelper.getUserId();
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<CoinsApi> eq = queryWrapper.lambda().eq(CoinsApi::getUserId, userId)
            .eq(CoinsApi::getExchangeName, body.getExchange())
            .eq(Objects.nonNull(body.getApiId()),CoinsApi::getId, body.getApiId())
            .last("limit 1");
        CoinsApi coinsApi = coinsApiService.getBaseMapper().selectOne(eq);
        keyCryptoService.decryptApi(coinsApi);
        return commonServce.queryFeeByExSymbol(coinsApi,body);
    }
    /**
     * 查自己那个交易所的指定币种实时资金费率
     * @return
     */
    @SaCheckLogin
    @GetMapping("/querySymbolFundingRate")
    public R querySymbolFundingRate(String exchange, String symbol) {


        return commonServce.querySymbolFundingRate(exchange,symbol);
    }

    /**
     * 查指定交易所指定币种合约的详情
     * @return
     */
    @SaCheckLogin
    @GetMapping("/querySymbolContractInfo")
    public R querySymbolContractInfo(String exchange, String symbol,Long accountId) {


        return commonServce.querySymbolContractInfo(exchange,symbol,accountId);
    }
    /**
     * 查指定交易所指定币种合约的详情
     * @return
     */
    @SaCheckLogin
    @GetMapping("/querySymbolMarketPrice")
    public R querySymbolMarketPrice(String exchange, String symbol) {
        return commonServce.querySymbolMarketPrice(exchange,symbol);
    }

    /**
     * 查指定交易所合约所有币种
     * @return
     */
    @SneakyThrows
    @SaCheckLogin
    @GetMapping("/queryLinerSymbolsByEx")
    public R queryLinerSymbolsByEx(@Validated @NotNull(message = "exchange cannot NULL") String exchange) {

        try {
            List<LinerSymbol> linerSymbols = exLinerSymbolCache.get(exchange);
            return R.ok(linerSymbols);
        } catch (ExecutionException e) {
            return R.fail(e.getMessage());
        }
    }
    /**
     * 查指定交易所指定合约资金费结算时间间隔
     * @return
     */
    @SneakyThrows
    @SaCheckLogin
    @GetMapping("/queryLinerSymbolsFundingTimeInterval")
    public R queryLinerSymbolsFundingTimeInterval(@Validated @NotNull(message = "exchange cannot NULL") String exchange,@Validated @NotNull(message = "symbol cannot NULL")String symbol) {

        try {
            Integer interval = exSymbolFundingIntervalCache.get(exchange + ":" + symbol);
            return R.ok(interval);
        } catch (ExecutionException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 手动建立socket连接
     *
     * @return
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/connectSocketByEx")
    public R connectSocketByEx(@RequestBody ConnectSocketBody connectSocketBody) {
        if (CollectionUtils.isEmpty(connectSocketBody.getExchanges())) {
            return R.ok();
        }
        commonServce.createWebsocketConnections(connectSocketBody.getExchanges());
        return R.ok();
    }

    /**
     * 手动建立socket连接
     *
     * @return
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/connectSocketByExAndAccountId")
    public R connectSocketByExAndAccountId(@RequestBody ConnectSocketMutiBody connectSocketBody) {
        if (CollectionUtils.isEmpty(connectSocketBody.getExchanges())) {
            return R.ok();
        }
        List<ExchangeAccountVo> exchanges = connectSocketBody.getExchanges();
        List<CoinsApiVo> coinsApiVos = coinsApiService.queryByIds(exchanges.stream().filter(Objects::nonNull).map(ExchangeAccountVo::getAccountId)
            .filter(Objects::nonNull).collect(Collectors.toList()));


        Map<Long, CoinsApiVo> apiVoMap = coinsApiVos.stream().collect(Collectors.toMap(CoinsApiVo::getId, item -> item));
        for (ExchangeAccountVo exchange : exchanges) {
            CoinsApiVo coinsApiVo = apiVoMap.get(exchange.getAccountId());
            if(Objects.nonNull(coinsApiVo)) {
                commonServce.createWebsocketConnections(exchange.getExchange(),exchange.getAccountId());
            }
        }

        return R.ok();
    }

    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/queryPositionTpslBySymbol")
    public R<List<? extends TpSlOrder>> queryPositionTpslBySymbol(@RequestBody HistoryPositionTpslQuery historyPositionTpslQuery) {
        ExchangeType exchangeType = ExchangeType.getExchangeType(historyPositionTpslQuery.getExchange());
        if (Objects.isNull(exchangeType)) {
            return R.ok("params error",List.of());
        }
        Long userId = LoginHelper.getUserId();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(historyPositionTpslQuery.getApiId());
        if(coinsApiVo.getUserId().intValue() != userId.intValue()) {
            return R.fail("Error api");
        }

        Account account = AccountUtils.coverToAccount(coinsApiVo);
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
        if (Objects.nonNull(exchangeService)) {
            List<? extends TpSlOrder> tpSlOrders = exchangeService.queryContractTpSlOrder(account, historyPositionTpslQuery.getSymbol());
            return R.ok("ok", tpSlOrders);
        }else{
            return R.fail("Error exchange service");
        }
    }

    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/closePositionApi")
    public R<?> closePositionApi(@RequestBody PositionCloseParam closeParam) {
        ExchangeType exchangeType = ExchangeType.getExchangeType(closeParam.getExchange());
        if (Objects.isNull(exchangeType)) {
            return R.ok("params error",List.of());
        }
        Long userId = LoginHelper.getUserId();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(closeParam.getApiId());
        if(coinsApiVo.getUserId().intValue() != userId.intValue()) {
            return R.fail("Error api");
        }

        Account account = AccountUtils.coverToAccount(coinsApiVo);
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
        if (Objects.nonNull(exchangeService)) {
            OrderPosition position = exchangeService.queryContractPosition(account, closeParam.getSymbol(), new PositionParams());
            if (Objects.nonNull(position)) {
                //仓位没有平
                OrderPosition orderPosition = new OrderPosition();
                orderPosition.setSymbol(closeParam.getSymbol());
                orderPosition.setSide(position.getSide());
                orderPosition.setSize(position.getSize());
                OrderCloseResult optResult = exchangeService.closeContractPosition(account, orderPosition);
                if (!CrossOrderStatus.SUCCESS.equalsIgnoreCase(optResult.getStatus())) {
                    throw new RuntimeException("平仓异常："+optResult.getMsg()+"  "+optResult.getBody());
                }
                log.info("已平仓 {}", closeParam.getSymbol());
                return R.ok("OK");
            } else {
                log.warn("仓位已平，无需重复平仓 {}", closeParam.getSymbol());
                return R.ok("OK");
            }
        }else{
            return R.fail("Error exchange service");
        }
    }
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/queryHistoryPositionByPage")
    public R<List<HistoryPositionVo>> queryHistoryPositionByPage(@RequestBody HistoryPositionQuery historyPositionQuery) {
        ExchangeType exchangeType = ExchangeType.getExchangeType(historyPositionQuery.getExchange());
        if (Objects.isNull(exchangeType)) {
            return R.ok("ok",List.of());
        }
        Long userId = LoginHelper.getUserId();
        CoinsApiVo coinsApiVo = coinsApiService.queryById(historyPositionQuery.getApiId());
        if(coinsApiVo.getUserId().intValue() != userId.intValue()) {
            return R.fail("Error api");
        }

        Account account = AccountUtils.coverToAccount(coinsApiVo);
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
        if (Objects.nonNull(exchangeService)) {
            List<HistoryPosition> historyPositions = exchangeService.queryContractHistoryPosition(account, historyPositionQuery.getSize(), historyPositionQuery);
            return R.ok("ok", MapstructUtils.convert(historyPositions,HistoryPositionVo.class));
        }else{
            return R.fail("Error exchange service");
        }
    }

    @GetMapping("/modifyConfig")
    @SaCheckLogin
    public R<String> getModifyConfig() {
        String defualtUserPrompt = sysConfigService.selectConfigByKey("defualt_user_prompt");
        String modifyPrompt = sysConfigService.selectConfigByKey("modify_prompt");
        JSONObject result = new JSONObject();
        if (JSON.isValid(defualtUserPrompt)) {
            result.put("defualtUserPrompt", JSON.parseArray(defualtUserPrompt));
        }
        if(JSON.isValid(modifyPrompt)){
            result.put("modifyPrompt", JSON.parseObject(modifyPrompt));
        }

        return R.ok("config error",result.toJSONString());
    }


}
