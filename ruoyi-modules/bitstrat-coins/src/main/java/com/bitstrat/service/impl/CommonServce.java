package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bitstrat.config.DeviceConnectionManager;
import com.bitstrat.config.ExchangeWebsocketProperties;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.MarketIntervalKV;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.bybit.StrategyConfig;
import com.bitstrat.domain.msg.SubscribeOrder;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.vo.*;
import com.bitstrat.wsClients.WsClusterManager;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsLossPointService;
import com.bitstrat.service.ICommonService;
import com.bitstrat.service.KeyCryptoService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.BitStratThreadFactory;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.SubscriptMsgType;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAsyncMarketDataRestClient;
import com.bybit.api.client.restApi.BybitApiCallback;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.bybit.api.client.websocket.callback.WebSocketMessageCallback;
import com.bybit.api.client.websocket.httpclient.WebsocketStreamClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.service.ConfigService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonServce implements ICommonService {
    //    @Value("${bybit.api_security}")
    String apiSercurity;
    //    @Value("${bybit.api_pwd}")
    String apiPwd;

    @Autowired
    private ExchangeWebsocketProperties exchangeWebsocketProperties;

    @Autowired
    private WsClusterManager wsClusterManager;
    @Autowired
    private ExchangeConnectionManager exchangeConnectionManager;
    @Autowired
    DeviceConnectionManager deviceConnectionManager;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
        @Lazy
    ICoinsLossPointService coinsLossPointService;

    @Autowired
    KeyCryptoService keyCryptoService;


    public CommonServce(ConfigService configService) {
        this.apiSercurity = configService.getConfigValue("apiSercurity");
        this.apiPwd = configService.getConfigValue("bybit_api_pwd");
        this.cateType = configService.getConfigValue("bybit_categoty");
        this.minOrderAmt = Double.valueOf(configService.getConfigValue("minOrderAmt"));
        log.info("已加载bybit 配置 apiSercurity:{} apiPwd:{} cateType:{} minOrderAmt:{}", apiSercurity, apiPwd, cateType, minOrderAmt);


    }

    String cateType;

    Double minOrderAmt;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> syncTaskSchedulerMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> taskSchedulerMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(BitStratThreadFactory.forName("task-scheduler"));
    private final ExecutorService syncExecutorService = Executors.newFixedThreadPool(1, BitStratThreadFactory.forName("sync-Cions-scheduler"));

    private final ConcurrentHashMap<String, ScheduledFuture<?>> schedulerMap = new ConcurrentHashMap<>();

    public ByBitAccount getByBitAccount() {
        ByBitAccount byBitAccount = new ByBitAccount();
        byBitAccount.setApiSecurity(apiSercurity);
        byBitAccount.setApiPwd(apiPwd);
        return byBitAccount;
    }

    public Double getMinOrderAmt() {
        return minOrderAmt;
    }

    public CategoryType getCateType() {
        CategoryType[] values = CategoryType.values();
        for (CategoryType value : values) {
            if (value.getCategoryTypeId().equalsIgnoreCase(this.cateType.trim())) {
                return value;
            }
        }
        return null;
    }

    public ExecutorService getSyncExecutorService() {
        return syncExecutorService;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public List<List<BigDecimal>> getKlinesData(ByBitAccount byBitAccount, String symbol, String interval) {
        CountDownLatch downLatch = new CountDownLatch(1);

        BybitApiClientFactory bybitApiClientFactory = BybitApiClientFactory
            .newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd());


        BybitApiAsyncMarketDataRestClient client = bybitApiClientFactory.newAsyncMarketDataRestClient();
        var marketKLineRequest = MarketDataRequest.builder().category(getCateType()).symbol(symbol)
            .marketInterval(MarketIntervalKV.getSourceByCode(interval)).build();
        List<List<BigDecimal>> kLines = new ArrayList<>();

        client.getMarketLinesData(marketKLineRequest, new BybitApiCallback<Object>() {
            @Override
            public void onResponse(Object response) {
                JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(response));
                if (data.getInteger("retCode") == 0) {
                    JSONObject result = data.getJSONObject("result");
                    JSONArray list = result.getJSONArray("list");
                    for (Object o : list) {
                        List<BigDecimal> kline = new ArrayList<>();
                        JSONArray item = (JSONArray) o;
                        String time = (String) item.get(0);
                        String openPrice = (String) item.get(1);
                        String highPrice = (String) item.get(2);
                        String lowPrice = (String) item.get(3);
                        String closePrice = (String) item.get(4);
                        String volume = (String) item.get(5);
                        kline.add(new BigDecimal(time));
                        kline.add(new BigDecimal(openPrice));
                        kline.add(new BigDecimal(highPrice));
                        kline.add(new BigDecimal(lowPrice));
                        kline.add(new BigDecimal(closePrice));
                        kline.add(new BigDecimal(volume));
                        kLines.add(kline);
                    }

                }
//                log.info(data.toJSONString());
                downLatch.countDown();
            }

            @Override
            public void onFailure(Throwable cause) {
                BybitApiCallback.super.onFailure(cause);
                log.error(cause.getMessage());
                downLatch.countDown();
            }
        });
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        kLines.sort(Comparator.comparing((item) -> {
            return item.get(0).longValue();
        }));
        return kLines;
    }

    @Override
    public ByBitAccount getBybitUserAccountByExchange(Long createBy) {
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CoinsApi::getCreateBy, createBy)
            .eq(CoinsApi::getExchangeName, ExchangeType.BYBIT.getName());
        CoinsApi coinsApi = coinsApiService.getBaseMapper().selectOne(queryWrapper);
        if(Objects.isNull(coinsApi)) {
            return null;
        }
        keyCryptoService.decryptApi(coinsApi);
        if (ExchangeType.BYBIT.getName().equalsIgnoreCase(coinsApi.getExchangeName())) {
            ByBitAccount byBitAccount = new ByBitAccount();
            byBitAccount.setApiSecurity(coinsApi.getApiKey());
            byBitAccount.setApiPwd(coinsApi.getApiSecurity());
            return byBitAccount;
        }
        return null;
    }

    @Override
    public R queryBalanceByEx(CoinsApi api,QueryBalanceBody body) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(body.getExchange());
        if(Objects.isNull(exchangeService)) {
            throw new RuntimeException(body.getExchange() + " unsupport");
        }
        Account account = AccountUtils.coverToAccount(api);
        AccountBalance balance = exchangeService.getBalance(account, body.getCoin());
        return R.ok(balance);
    }

    @Override
    public R queryFeeByExSymbol(CoinsApi coinsApi, QueryFeeBody body) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(body.getExchange());
        if(Objects.isNull(exchangeService)) {
            throw new RuntimeException(body.getExchange() + " unsupport");
        }
        Account account = AccountUtils.coverToAccount(coinsApi);
        SymbolFee fee = exchangeService.getFee(account, body.getSymbol());
        return R.ok(fee);
    }

    @Override
    public List<Account> getUserAccountByNodeClient(String clientId) {
        //根据客户端查分配到此节点的任务或滑点
        List<CoinsApi> coinsApis = coinsLossPointService.selectAccountByClientId(clientId);
        ArrayList<Account> accountList = new ArrayList<>();
        for (CoinsApi coinsApi : coinsApis) {
            Account account = AccountUtils.coverToAccount(coinsApi);
            accountList.add(account);
        }

        return accountList;
    }

    @Override
    @Cacheable(value = "querySymbolFundingRate:cache#10s#60s#20", key = "#exchange+':'+#symbol",condition = "#exchange != null && #symbol != null ")
    public R querySymbolFundingRate(String exchange, String symbol) {

        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchange);
        if(Objects.isNull(exchangeService)) {
            return R.fail("不支持的交易所 " + exchange);
        }
        SymbolFundingRate symbolFundingRate = exchangeService.getSymbolFundingRate(symbol);
        return R.ok(symbolFundingRate);
    }

    @Override
    public R querySymbolContractInfo(String exchange, String symbol,Long accountId) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchange);
        if(Objects.isNull(exchangeService)) {
            return R.fail("不支持的交易所 " + exchange);
        }
        CoinContractInfomation contractCoinInfo;
        if(Objects.nonNull(accountId)) {
            CoinsApiVo coinsApiVo = coinsApiService.queryById(accountId);
            Account account = AccountUtils.coverToAccount(coinsApiVo);
            contractCoinInfo = exchangeService.getContractCoinInfo(account, symbol);
        }else{
            contractCoinInfo = exchangeService.getContractCoinInfo(null, symbol);
        }


        return R.ok(contractCoinInfo);
    }

    @Override
    public R querySymbolMarketPrice(String exchange, String symbol) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchange);
        if(Objects.isNull(exchangeService)) {
            return R.fail("不支持的交易所 " + exchange);
        }
        BigDecimal nowPrice = exchangeService.getNowPrice(null, symbol);

        return R.ok(nowPrice);
    }

    @Override
    public List<LinerSymbol> getAllLinerSymbolByEx(String ex) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex.toLowerCase());
        if(Objects.isNull(exchangeService)) {
            return Collections.emptyList();
        }
        List<LinerSymbol> allLinerSymbol = exchangeService.getAllLinerSymbol();

        return allLinerSymbol;
    }

    @Override
    public Integer getSymbolFundingTimeIntervalByEx(String ex, String symbol) {
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex.toLowerCase());
        if(Objects.isNull(exchangeService)) {
            return 0;
        }
        return exchangeService.getLinerSymbolFundingRateInterval(symbol);

    }

    @Override
    public void createWebsocketConnections(List<String> exchanges) {
        Long userId = LoginHelper.getUserId();
        Set<String> exs = new HashSet<>(exchanges);
        for (String ex : exs) {
            List<CoinsApiVo> coinsApiVos = coinsApiService.queryApiByUserId(userId);
            for (CoinsApiVo coinsApiVo : coinsApiVos) {
                if(coinsApiVo != null) {
                    Account account = AccountUtils.coverToAccount(coinsApiVo);
                    APITypeHelper.set(account.getType());
                    try{
                        String urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(ex, WebSocketType.PRIVATE);
                        if(StringUtils.isEmpty(urlByExAndType)) {
                            log.error("urlByExAndType is empty userid{} ex {} type: {}",userId, ex, WebSocketType.PRIVATE);
                            continue;
                        }
                        try {
                            ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
                            SubscriptMsgs wsSubscriptMsgs = exchangeService.getWsSubscriptMsgs();
                            exchangeConnectionManager.createConnection(account,userId+"",ex,WebSocketType.PRIVATE, URI.create(urlByExAndType),null,null,true,(channel)->{
                                //监听仓位变动
                                exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptPositionMsg(), SubscriptMsgType.POSITION,channel);
                                //监听订单成交
                                exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptOrderMsg(),null,channel);
                                //监听账户变动
                                exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptAccountMsg(),null,channel);
                            });
                            

                        } catch (Exception e) {
                            log.error("ws 监听仓位失败 {} userid {} ex {} type {} url:{}",e.getMessage(),userId,ex,WebSocketType.PRIVATE,urlByExAndType,e);
                        }
                    }finally {
                        APITypeHelper.clear();
                    }

                }
            }

        }


    }

    @Override
    public void createWebsocketConnections(String ex, Long accountId) {
        Long userId = LoginHelper.getUserId();
        this.createWebsocketConnections(ex,accountId,userId,true);
    }
    public void createWebsocketConnections(String ex, Long accountId,Long userId) {
        this.createWebsocketConnections(ex,accountId,userId,true);
    }
    public void createWebsocketConnections(String ex, Long accountId,Long userId,boolean forceCreate) {

        CoinsApiVo coinsApiVo = coinsApiService.queryById(accountId);

        if(coinsApiVo != null) {
            Account account = AccountUtils.coverToAccount(coinsApiVo);
            APITypeHelper.set(account.getType());
            try {
                String urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(ex, WebSocketType.PRIVATE);
                if(StringUtils.isEmpty(urlByExAndType)) {
                    log.error("urlByExAndType is empty userid{} ex {} type: {}",userId, ex, WebSocketType.PRIVATE);
                }
                try {
                    ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
                    exchangeConnectionManager.createConnection(account,userId+"",ex,WebSocketType.PRIVATE, URI.create(urlByExAndType),null,null,forceCreate,(channel)->{
                        SubscriptMsgs wsSubscriptMsgs = exchangeService.getWsSubscriptMsgs();
                        //监听仓位变动
                        exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptPositionMsg(), SubscriptMsgType.POSITION,channel);
                        //监听订单成交
                        exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptOrderMsg(),null,channel);
                        //监听账户变动
                        exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptAccountMsg(),null,channel);
                    });



                } catch (Exception e) {
                    log.error("ws 监听仓位失败 {} userid {} ex {} type {} url:{}",e.getMessage(),userId,ex,WebSocketType.PRIVATE,urlByExAndType,e);
                }
            }finally {
                APITypeHelper.clear();
            }

        }
    }

    public List<List<BigDecimal>> getKlinesData(ByBitAccount byBitAccount, String symbol, String interval, Long size) {

        BybitApiClientFactory bybitApiClientFactory = BybitApiClientFactory
            .newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd());
        BybitApiMarketRestClient bybitApiMarketRestClient = bybitApiClientFactory.newMarketDataRestClient();
        var marketKLineRequest = MarketDataRequest.builder().category(getCateType()).symbol(symbol)
            .marketInterval(MarketIntervalKV.getSourceByCode(interval)).limit(Math.toIntExact(size)).build();
        List<List<BigDecimal>> kLines = new ArrayList<>();

        Object marketLinesData = bybitApiMarketRestClient.getMarketLinesData(marketKLineRequest);
        JSONObject data = JSONObject.from(marketLinesData);
        if (data.getInteger("retCode") == 0) {
            JSONObject result = data.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            for (Object o : list) {
                List<BigDecimal> kline = new ArrayList<>();
                JSONArray item = (JSONArray) o;
                String time = (String) item.get(0);
                String openPrice = (String) item.get(1);
                String highPrice = (String) item.get(2);
                String lowPrice = (String) item.get(3);
                String closePrice = (String) item.get(4);
                String volume = (String) item.get(5);
                kline.add(new BigDecimal(time));
                kline.add(new BigDecimal(openPrice));
                kline.add(new BigDecimal(highPrice));
                kline.add(new BigDecimal(lowPrice));
                kline.add(new BigDecimal(closePrice));
                kline.add(new BigDecimal(volume));
                kLines.add(kline);
            }

        }
        return kLines;
    }

    public List<List<BigDecimal>> getKlinesData(ByBitAccount byBitAccount, String symbol, String interval, Long start, Long end) {

        BybitApiClientFactory bybitApiClientFactory = BybitApiClientFactory
            .newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd());
        BybitApiMarketRestClient bybitApiMarketRestClient = bybitApiClientFactory.newMarketDataRestClient();
        var marketKLineRequest = MarketDataRequest.builder().category(getCateType()).symbol(symbol)
            .marketInterval(MarketIntervalKV.getSourceByCode(interval)).start(start).end(end).build();
        List<List<BigDecimal>> kLines = new ArrayList<>();

        Object marketLinesData = bybitApiMarketRestClient.getMarketLinesData(marketKLineRequest);
        JSONObject data = JSONObject.from(marketLinesData);
        if (data.getInteger("retCode") == 0) {
            JSONObject result = data.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            for (Object o : list) {
                List<BigDecimal> kline = new ArrayList<>();
                JSONArray item = (JSONArray) o;
                String time = (String) item.get(0);
                String openPrice = (String) item.get(1);
                String highPrice = (String) item.get(2);
                String lowPrice = (String) item.get(3);
                String closePrice = (String) item.get(4);
                String volume = (String) item.get(5);
                kline.add(new BigDecimal(time));
                kline.add(new BigDecimal(openPrice));
                kline.add(new BigDecimal(highPrice));
                kline.add(new BigDecimal(lowPrice));
                kline.add(new BigDecimal(closePrice));
                kline.add(new BigDecimal(volume));
                kLines.add(kline);
            }

        }
        return kLines;
    }

    public BarSeries getBarSeries(List<List<BigDecimal>> klines, String symbol, Long interval) {
        // 创建 BarSeries
        BarSeries series = new BaseBarSeriesBuilder().withName(symbol).build();

        // 添加 K 线数据，使用收盘价（close）
        for (List<BigDecimal> kline : klines) {
            ZonedDateTime startTime = Instant.ofEpochMilli(kline.get(0).longValue()).atZone(ZoneId.systemDefault());
            ZonedDateTime endTime = startTime.plusMinutes(interval);
            Duration duration = Duration.between(startTime, endTime);
            double open = kline.get(1).doubleValue();
            double hight = kline.get(2).doubleValue();
            double low = kline.get(3).doubleValue();
            double close = kline.get(4).doubleValue();
            double volume = kline.get(5).doubleValue();
            BaseBar baseBar = new BaseBar(duration, endTime, open, hight, low, close, volume);
            series.addBar(baseBar);
        }
        return series;
    }

    public StrategyConfig parsRole(String strategyConfig) {
        StrategyConfig config = JSONObject.parseObject(strategyConfig, StrategyConfig.class);
        return config;
    }

    public JSONObject queryOrderStatus(String orderId, ByBitAccount byBitAccount) {
        //查询订单状态
        TradeOrderRequest build = TradeOrderRequest.builder().category(getCateType()).orderId(orderId).build();
        BybitApiClientFactory bybitApiClientFactory = BybitApiClientFactory
            .newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd());
        var client = bybitApiClientFactory.newTradeRestClient();
        var openLinearOrdersResult = client.getOpenOrders(build);
        JSONObject from = JSONObject.from(openLinearOrdersResult);

        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list != null && list.size() > 0) {
                JSONObject order = list.getJSONObject(0);
                return order;
            } else {
                throw new RuntimeException("Can't find open linear orders");
            }
        } else {
            throw new RuntimeException(from.getString("retMsg"));
        }
    }

    public JSONObject queryPosition(String symbol, ByBitAccount byBitAccount) {
        //查询订单状态
        var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd()).newAsyncPositionRestClient();
        var positionListRequest = PositionDataRequest.builder().category(getCateType()).symbol(symbol).build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<JSONObject> result = new AtomicReference<>();
        AtomicReference<Boolean> fail = new AtomicReference<>();
        AtomicReference<String> failMsg = new AtomicReference<>();


        client.getPositionInfo(positionListRequest, new BybitApiCallback() {
            @Override
            public void onResponse(Object response) {
                result.set(JSONObject.from(response));
//                log.info(response.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Throwable cause) {
                BybitApiCallback.super.onFailure(cause);
                fail.set(true);
                failMsg.set(cause.getMessage());
                countDownLatch.countDown();
                throw new RuntimeException(cause);
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (fail.get() != null && fail.get()) {
            throw new RuntimeException(failMsg.get());
        }

        JSONObject from = JSONObject.from(result.get());

        if (from.getInteger("retCode") == 0) {
            JSONObject data = from.getJSONObject("result");
            JSONArray list = data.getJSONArray("list");
            if (list != null && list.size() > 0) {
                JSONObject order = list.getJSONObject(0);
                return order;
            } else {
                throw new RuntimeException("Can't find open positions");
            }
        } else {
            throw new RuntimeException(from.getString("retMsg"));
        }
    }

    public JSONObject queryPositionSpot(String symbol, ByBitAccount byBitAccount) {
        //查询订单状态
        var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd()).newAsyncPositionRestClient();
        var positionListRequest = PositionDataRequest.builder().category(getCateType()).symbol(symbol).build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<JSONObject> result = new AtomicReference<>();
        AtomicReference<Boolean> fail = new AtomicReference<>();
        AtomicReference<String> failMsg = new AtomicReference<>();


        client.getPositionInfo(positionListRequest, new BybitApiCallback() {
            @Override
            public void onResponse(Object response) {
                result.set(JSONObject.from(response));
//                log.info(response.toString());
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Throwable cause) {
                BybitApiCallback.super.onFailure(cause);
                fail.set(true);
                failMsg.set(cause.getMessage());
                countDownLatch.countDown();
                throw new RuntimeException(cause);
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (fail.get() != null && fail.get()) {
            throw new RuntimeException(failMsg.get());
        }

        JSONObject from = JSONObject.from(result.get());

        if (from.getInteger("retCode") == 0) {
            JSONObject data = from.getJSONObject("result");
            JSONArray list = data.getJSONArray("list");
            if (list != null && list.size() > 0) {
                JSONObject order = list.getJSONObject(0);
                return order;
            } else {
                throw new RuntimeException("Can't find open positions");
            }
        } else {
            throw new RuntimeException(from.getString("retMsg"));
        }
    }

    public ConcurrentHashMap<String, ScheduledFuture<?>> getSchedulerMap() {
        return schedulerMap;
    }

    public ConcurrentHashMap<Long, ScheduledFuture<?>> getSyncTaskSchedulerMap() {
        return syncTaskSchedulerMap;
    }

    public ConcurrentHashMap<Long, ScheduledFuture<?>> getTaskSchedulerMap() {
        return taskSchedulerMap;
    }

    public void initWebsocket() {
        ByBitAccount byBitAccount = this.getByBitAccount();
        //查询订单状态
        WebsocketStreamClient websocketStreamClient = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd()).newWebsocketClient(new WebSocketMessageCallback() {
            @Override
            public void onMessage(String message) throws JsonProcessingException {
                log.info("websocket data {}", message);
                JSONObject parse = JSONObject.parse(message);
                if ("position".equalsIgnoreCase(parse.getString("topic"))) {
                    JSONArray data = parse.getJSONArray("data");
                    for (Object item : data) {
                        JSONObject position = JSONObject.from(item);
                        log.info("ws {}", position.toString());

                    }

                }

            }
        });
        WebSocket connect = websocketStreamClient.connect();
        websocketStreamClient.sendSubscribeMessage(connect, Arrays.asList("position", "execution.fast"));
        log.info("websocket connect success");


    }

    /**
     * 检查上次下单时间 经过coldSec 冷却时间后现在能不能下单
     *
     * @param lastOrderTime
     * @param coldSec
     * @return true 可以下单，false 冷却时间没过
     */
    public boolean checkColdSec(Date lastOrderTime, Long coldSec) {
        if (lastOrderTime == null || coldSec == null) {
            return true; // 如果没有上次下单时间或冷却时间，则默认允许下单
        }

        long currentTime = System.currentTimeMillis(); // 当前时间（毫秒）
        long lastOrderMillis = lastOrderTime.getTime(); // 上次下单时间（毫秒）
        long coldMillis = coldSec * 1000; // 冷却时间转换为毫秒

        return (currentTime - lastOrderMillis) >= coldMillis;

        //v2
//        return lastOrderTime == null || Instant.now().isAfter(lastOrderTime.plus(coldSec, ChronoUnit.SECONDS));
    }

    public JSONObject getMarketInfo(ByBitAccount account, String symbol) {

        BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newMarketDataRestClient();
        MarketDataRequest marketDataRequest = MarketDataRequest.builder().symbol(symbol).category(this.getCateType())
            .build();
        Object marketTickers = bybitApiMarketRestClient.getMarketTickers(marketDataRequest);
        JSONObject from = JSONObject.from(marketTickers);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list != null && list.size() > 0) {
                JSONObject order = list.getJSONObject(0);
                return order;
            }
        }
        return null;
    }

    public JSONObject queryTickers(String symbol, ByBitAccount account) {
        BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newMarketDataRestClient();
        MarketDataRequest marketDataRequest = MarketDataRequest.builder().symbol(symbol).category(this.getCateType())
            .build();
        Object marketTickers = bybitApiMarketRestClient.getMarketTickers(marketDataRequest);

        JSONObject from = JSONObject.from(marketTickers);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list != null && list.size() > 0) {
                JSONObject order = list.getJSONObject(0);
                return order;
            }
        }
        throw new RuntimeException(from.getString("retMsg"));
    }

    public void doOrderSubscript(String exchangeName, String clientId, CoinsLossPointVo coinsLossPointVo) {

        Channel deviceChannel = deviceConnectionManager.getDeviceChannel(clientId);
        Message message = new Message();
        message.setTimestamp(System.currentTimeMillis());
        message.setType(MessageType.SUBSCRIPTION_ORDER);
        SubscribeOrder subscribeOrder = new SubscribeOrder();
        ByBitAccount bybitUserAccountByExchange = getBybitUserAccountByExchange(coinsLossPointVo.getCreateBy());
        subscribeOrder.setAccount(bybitUserAccountByExchange);
        message.setData(subscribeOrder);
        message.setExchangeName(exchangeName);
        ChannelFuture channelFuture = deviceChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
        channelFuture.addListener(result -> {
            if (result.cause() != null) {
                //error
                result.cause().printStackTrace();

            } else {
                //success
                log.warn("已下发订单状态订阅");
            }
        });
    }

    public R setLeverageBody(SetLeverageBody body) {
        if (StringUtils.isEmpty(body.getCategory())) {
            body.setCategory(CategoryType.LINEAR.getCategoryTypeId());
        }

        ByBitAccount byBitAccount = getBybitUserAccountByExchange(LoginHelper.getUserId());
        var client = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd()).newPositionRestClient();
        var setLeverageRequest = PositionDataRequest.builder().category(CategoryType.LINEAR)
            .symbol(body.getSymbol())
            .buyLeverage(String.valueOf(body.getBuyLeverage()))
            .sellLeverage(String.valueOf(body.getSellLeverage())).build();
        Object result = client.setPositionLeverage(setLeverageRequest);
        JSONObject from = JSONObject.from(result);
        if (from.getInteger("retCode") == 0) {
            return R.ok();
        } else {
            throw new RuntimeException(from.getString("retMsg"));
        }
    }

    public List<Account> getUserAccountByExchange(String exchangeName) {
        //目前只有bybit 后续可扩展多租户
        if (StringUtils.isNotEmpty(exchangeName)) {
            CoinsApiBo coinsApiBo = new CoinsApiBo();
            coinsApiBo.setExchangeName(exchangeName);
            List<CoinsApiVo> coinsApiVos = coinsApiService.queryList(coinsApiBo);


//            if (ExchangeType.BYBIT.getName().equalsIgnoreCase(exchangeName)) {
//                return List.of(this.getByBitAccount());
//            }
            List<Account> byBitAccountList = coinsApiVos.stream().map((item) -> {
                if (ExchangeType.BYBIT.getName().equalsIgnoreCase(exchangeName)) {
                    ByBitAccount byBitAccount = new ByBitAccount();
                    byBitAccount.setApiSecurity(item.getApiKey());
                    byBitAccount.setApiPwd(item.getApiSecurity());
                    return (Account) byBitAccount;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return byBitAccountList;
        }
        return List.of();
    }
    public List<Account> getUserAccountByExchange(Long userId,String exchangeName) {
        //目前只有bybit 后续可扩展多租户
        if (StringUtils.isNotEmpty(exchangeName)) {
            CoinsApiBo coinsApiBo = new CoinsApiBo();
            coinsApiBo.setExchangeName(exchangeName);
            coinsApiBo.setUserId(userId);
            List<CoinsApiVo> coinsApiVos = coinsApiService.queryList(coinsApiBo);


//            if (ExchangeType.BYBIT.getName().equalsIgnoreCase(exchangeName)) {
//                return List.of(this.getByBitAccount());
//            }
            List<Account> byBitAccountList = coinsApiVos.stream().map((item) -> {
                if (ExchangeType.BYBIT.getName().equalsIgnoreCase(exchangeName)) {
                    ByBitAccount byBitAccount = new ByBitAccount();
                    byBitAccount.setApiSecurity(item.getApiKey());
                    byBitAccount.setApiPwd(item.getApiSecurity());
                    return (Account) byBitAccount;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return byBitAccountList;
        }
        return List.of();
    }

}
