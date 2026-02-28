package com.bitstrat.strategy.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.annotation.AccountPaptrading;
import com.bitstrat.constant.CorssLeverageStatus;
import com.bitstrat.constant.CrossOrderStatus;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.TickerItem;
import com.bitstrat.domain.bybit.LinerSymbolItem;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.service.ICoinGlassService;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.wsClients.msg.BybitWsMsg;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import com.bitstrat.wsClients.msg.receive.BybitClosePnlMessage;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.domain.asset.request.AssetDataRequest;
import com.bybit.api.client.domain.market.InstrumentStatus;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.PositionIdx;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.TimeInForce;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.*;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/17 14:44
 * @Content
 */

@Service
@Slf4j
@AccountPaptrading
public class BybitExchangeRestServiceImpl implements ExchangeService {

    BybitWsMsg bybitWsMsg = new BybitWsMsg();
    @Autowired
    ICoinGlassService iCoinGlassService;


    ExecutorService bybitThreadExecutor = Executors.newWorkStealingPool(2);

    @Override
    public SubscriptMsgs getWsSubscriptMsgs() {
        return bybitWsMsg;
    }

    @Override
    public String getExchangeName() {
        return ExchangeType.BYBIT.getName().toLowerCase();
    }

    public static void main(String[] args) {
        BybitExchangeRestServiceImpl bybitExchangeRestService = new BybitExchangeRestServiceImpl();
        SymbolFundingRate magicusdt = bybitExchangeRestService.getSymbolFundingRate("MAGICUSDT");
        log.info("magicusdt={}", magicusdt);


    }

    @Override
    @Cacheable(value = "bybitContractInfo:cache#60s#60s#20", key = "'bybit'+':'+'contract:symbolInfo'+':'+#symbol", condition = "#symbol != null ")
    public CoinContractInfomation getContractCoinInfo(Account account, String symbol) {
        symbol = checkSymbolLiner(symbol);
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        MarketDataRequest build = MarketDataRequest.builder().category(CategoryType.LINEAR)
            .symbol(checkSymbolLiner(symbol)).build();
        Object instrumentsInfo = client.getInstrumentsInfo(build);
        JSONObject from = JSONObject.from(instrumentsInfo);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            for (Object o : list) {
                JSONObject symbolInfo = JSONObject.from(o);
                if (symbolInfo.getString("symbol").equalsIgnoreCase(symbol)) {
                    CoinContractInfomation coinContractInfomation = new CoinContractInfomation();
                    JSONObject leverageFilter = symbolInfo.getJSONObject("leverageFilter");

                    coinContractInfomation.setMaxLeverage(leverageFilter.getInteger("maxLeverage"));
                    coinContractInfomation.setMinLeverage(leverageFilter.getInteger("minLeverage"));
                    JSONObject lotSizeFilter = symbolInfo.getJSONObject("lotSizeFilter");
                    //订单信息
                    coinContractInfomation.setMaxMktSz(lotSizeFilter.getBigDecimal("maxOrderQty"));
                    coinContractInfomation.setMinSz(lotSizeFilter.getBigDecimal("minOrderQty"));
                    //最小下单量，就是最小的面值
                    coinContractInfomation.setContractValue(lotSizeFilter.getBigDecimal("minOrderQty"));
                    //乘数就是步长
                    coinContractInfomation.setStep(lotSizeFilter.getBigDecimal("qtyStep"));
                    coinContractInfomation.setCtMult(BigDecimal.ONE);
                    coinContractInfomation.setMaxLmtSz(lotSizeFilter.getBigDecimal("maxOrderQty"));
                    coinContractInfomation.setFundingInterval(symbolInfo.getBigDecimal("fundingInterval"));
                    coinContractInfomation.setSymbol(symbol);
                    coinContractInfomation.setCalcPlaces(symbolInfo.getInteger("priceScale"));
//                    coinContractInfomation.set
//                    coinContractInfomation.setMaxMktSz();

                    //1000,1; 做空 1000,1.1 做多
                    return coinContractInfomation;
                }


            }
        }
        return null;
    }

    @Override
    public String setLeverage(Account account, Integer leverage, String symbol, String side) {
        symbol = checkSymbolLiner(symbol);
        var client = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newPositionRestClient();
        var setLeverageRequest = PositionDataRequest.builder().category(CategoryType.LINEAR)
            .symbol(symbol)
            .buyLeverage(String.valueOf(leverage))
            .sellLeverage(String.valueOf(leverage)).build();
        Object result = client.setPositionLeverage(setLeverageRequest);
        JSONObject from = JSONObject.from(result);
        if (from.getInteger("retCode") == 0 || from.getInteger("retCode") == 110043) {
            return CorssLeverageStatus.SUCCESS;
        } else {
            throw new RuntimeException(from.getString("retMsg"));
        }
    }

    @NotNull
    private static String checkSymbolLiner(String symbol) {
        if (!symbol.endsWith("USDT")) {
            symbol = symbol + "USDT";
        }
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/", "");
        }
        return symbol;
    }

    private static String deSymbolLiner(String symbol) {
        if (!symbol.endsWith("USDT")) {
            symbol = symbol.replaceAll("USDT", "");
        }
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/USDT", "");
        }
        return symbol;
    }

    @Override
    public boolean checkApi(Account account) {
        BybitApiAccountRestClient bybitApiAccountRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAccountRestClient();
        Object accountInfo = bybitApiAccountRestClient.getAccountInfo();
        JSONObject from = JSONObject.from(accountInfo);
        if (from.getInteger("retCode") == 0) {
            return true;
        }
        return false;
    }

    @Override
    public OrderOptStatus buyContract(Account account, OrderVo params) {
        BybitApiTradeRestClient bybitApiTradeRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newTradeRestClient();
        String symbol = params.getSymbol();
        symbol = checkSymbolLiner(symbol);
        String size = params.getSize().toPlainString();

        long orderId = IdUtil.getSnowflake().nextId();
        TradeOrderRequest.TradeOrderRequestBuilder builder = TradeOrderRequest.builder();
        builder.category(CategoryType.LINEAR) // 创建合约单
            .symbol(symbol)
            .side(Side.BUY)
            .marketUnit("baseCoin") //单位是对应币种
            .qty(size);
//            .timeInForce(TimeInForce.IOC);
        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            builder.orderType(TradeOrderType.MARKET); //市价入场
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            builder.orderType(TradeOrderType.LIMIT)
                .timeInForce(TimeInForce.GTC)
                .price(params.getPrice().toPlainString());
        }
        //是否只减仓
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
            builder.reduceOnly(true);
        }

        TradeOrderRequest build = builder
            .positionIdx(PositionIdx.ONE_WAY_MODE)
            .orderLinkId(orderId + "")
            .build();
        Object order = bybitApiTradeRestClient.createOrder(build);
        JSONObject from = JSONObject.from(order);
        log.info("bybit api trade:{}", from);
        if (from.getInteger("retCode") == 0) {
            //订单已被接收
            JSONObject result = from.getJSONObject("result");
            String bybitOrderId = result.getString("orderId");
            OrderOptStatus orderOptStatus = new OrderOptStatus();
            orderOptStatus.setOrderId(bybitOrderId);
            orderOptStatus.setSymbol(params.getSymbol());
            orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
            return orderOptStatus;
        }
        throw new RuntimeException("bybit 下单失败 " + from.getString("retMsg"));
    }

    @Override
    public OrderOptStatus sellContract(Account account, OrderVo params) {
        BybitApiTradeRestClient bybitApiTradeRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newTradeRestClient();
        String symbol = params.getSymbol();
        symbol = checkSymbolLiner(symbol);
        String size = params.getSize().toPlainString();

        long orderId = IdUtil.getSnowflake().nextId();
        TradeOrderRequest.TradeOrderRequestBuilder builder = TradeOrderRequest.builder();
        builder.category(CategoryType.LINEAR) // 创建合约单
            .symbol(symbol)
            .side(Side.SELL)
            .marketUnit("baseCoin") //单位是对应币种
            .qty(size);
//            .timeInForce(TimeInForce.IOC);
        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            builder.orderType(TradeOrderType.MARKET); //市价入场
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            builder.orderType(TradeOrderType.LIMIT)
                .timeInForce(TimeInForce.GTC)
                .price(params.getPrice().toPlainString());
        }

        //是否只减仓
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
            builder.reduceOnly(true);
        }

        TradeOrderRequest build = builder //价格
            .positionIdx(PositionIdx.ONE_WAY_MODE)
            .orderLinkId(orderId + "")
            .build();
        Object order = bybitApiTradeRestClient.createOrder(build);
        JSONObject from = JSONObject.from(order);
        log.info("bybit api trade:{}", from);
        if (from.getInteger("retCode") == 0) {
            //订单已被接收
            JSONObject result = from.getJSONObject("result");
            String bybitOrderId = result.getString("orderId");
            OrderOptStatus orderOptStatus = new OrderOptStatus();
            orderOptStatus.setOrderId(bybitOrderId);
            orderOptStatus.setSymbol(params.getSymbol());
            orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
            return orderOptStatus;
        }
        throw new RuntimeException("bybit 下单失败 " + from.getString("retMsg"));

    }

    @Override
    public String buySpot(Account account, JSONObject params) {
        throw new UnsupportedOperationException("unsupport");
    }

    @Override
    public String sellSpot(Account account, JSONObject params) {
        throw new UnsupportedOperationException("unsupport");
    }

    @Override
    public String checkContractOrder(Account account, OrderInfo params) {
        //先查订单状态
        BybitApiTradeRestClient tradeClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newTradeRestClient();
        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
            .category(CategoryType.LINEAR)
            .orderLinkId(params.getOrderId()).build();
        Object openOrders = tradeClient.getOpenOrders(tradeOrderRequest);
        JSONObject from = JSONObject.from(openOrders);
        log.info("bybit 查询订单状态 {} {}", params.getOrderId(), from);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list.isEmpty()) {

            } else {
                for (Object o : list) {
                    JSONObject ordered = JSONObject.from(o);
                    String orderStatus = ordered.getString("orderStatus");
                    List<String> cancelStatus = OrderStatusConstant.bybitCancelStatus;
                    if (cancelStatus.contains(orderStatus)) {
                        //已取消
                        return CrossOrderStatus.CANCEL;
                    } else if (orderStatus.equalsIgnoreCase("Filled")) {
                        //成交
                        //继续往下
                        return CrossOrderStatus.SUCCESS;
                    } else {
                        return CrossOrderStatus.UNKNOW;
                    }
                }
            }
        } else {
            return CrossOrderStatus.UNKNOW;
        }
        return CrossOrderStatus.UNKNOW;
    }

    public JSONObject checkContractOrder(Account account, String orderId) {
        //先查订单状态
        BybitApiTradeRestClient tradeClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newTradeRestClient();
        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
            .category(CategoryType.LINEAR)
            .orderId(orderId).build();
        Object openOrders = tradeClient.getOpenOrders(tradeOrderRequest);
        JSONObject from = JSONObject.from(openOrders);
        log.info("bybit 查询订单状态 {} {}", orderId, from);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list.isEmpty()) {

            } else {
                for (Object o : list) {
                    JSONObject ordered = JSONObject.from(o);
                    String orderStatus = ordered.getString("orderStatus");
                    return ordered;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    @Override
    public void checkSpotOrder(Account account, JSONObject params) {

    }

    @Override
    public void cancelSpotOrder(Account account, OrderInfo params) {
//        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());
//        //先查订单状态
//        BybitApiTradeRestClient tradeClient = factory.newTradeRestClient();
//        TradeOrderRequest orderRequest = TradeOrderRequest.builder().orderId(params.getOrderId());
//        tradeClient.cancelOrder()

    }

    @Override
    public String cancelContractOrder(Account account, OrderOptStatus order) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());
        //先查订单状态
        BybitApiTradeRestClient tradeClient = factory.newTradeRestClient();
        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
            .category(CategoryType.LINEAR)
            .orderId(order.getOrderId()).build();
        Object openOrders = tradeClient.cancelOrder(tradeOrderRequest);
        JSONObject from = JSONObject.from(openOrders);
        log.info("bybit 取消订单状态 {} {}", order.getOrderId(), from);
        if (from.getInteger("retCode") == 0) {
            return CrossOrderStatus.SUCCESS;
        }
        return CrossOrderStatus.FAIL;
    }

    @Override
    public OrderCloseResult closeContractPosition(Account account, OrderPosition params) {
        BybitApiTradeRestClient bybitApiTradeRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newTradeRestClient();
        String symbol = params.getSymbol();
        symbol = checkSymbolLiner(symbol);


        TradeOrderRequest.TradeOrderRequestBuilder builder = TradeOrderRequest.builder();
        builder.category(CategoryType.LINEAR) // 创建合约单
            .symbol(symbol)
            .side(params.getSide().equalsIgnoreCase("long") ? Side.SELL : Side.BUY)
            .marketUnit("baseCoin") //单位是对应币种
            .qty("0")
//            .timeInForce(TimeInForce.IOC)
            .reduceOnly(true);
        builder.orderType(TradeOrderType.MARKET); //市价入场
        TradeOrderRequest build = builder
            .positionIdx(PositionIdx.ONE_WAY_MODE)
            .build();
        Object order = bybitApiTradeRestClient.createOrder(build);
        JSONObject from = JSONObject.from(order);
        if (from.getInteger("retCode") == 0) {
            //订单已被接收
            JSONObject result = from.getJSONObject("result");
            String bybitOrderId = result.getString("orderId");
            log.info("平仓订单 {} {}", bybitOrderId, from);
            OrderCloseResult orderCloseResult = new OrderCloseResult();
            orderCloseResult.setBody(bybitOrderId);
            orderCloseResult.setStatus(CrossOrderStatus.SUCCESS);
            return orderCloseResult;
        } else {
            log.info("平仓失败 : {}", from);
            OrderCloseResult orderCloseResult = new OrderCloseResult();
            orderCloseResult.setBody(from.toJSONString());
            orderCloseResult.setStatus(CrossOrderStatus.FAIL);
            return orderCloseResult;
        }
    }

    @Override
    public OrderPosition queryContractPosition(Account account, String sourceSymbol, PositionParams params) {
        String symbol = checkSymbolLiner(sourceSymbol);
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());

        PositionDataRequest positionData = PositionDataRequest.builder()
            .category(CategoryType.LINEAR)
            .symbol(symbol).build();
        BybitApiPositionRestClient client = factory.newPositionRestClient();
        Object positions = client.getPositionInfo(positionData);
        JSONObject positionObject = JSONObject.from(positions);
        log.info("查询持仓状态 {}", positionObject);
        if (positionObject.getInteger("retCode") == 0) {
            JSONObject result = positionObject.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list.isEmpty()) {

            } else {
                for (Object o : list) {
                    JSONObject ordered = JSONObject.from(o);
                    String symbolPosition = ordered.getString("symbol");
                    if (symbolPosition.equals(symbol)) {
//                        if (ordered.getBigDecimal("size").doubleValue() == 0d) {
//                            log.error("无持仓 {}", symbolPosition);
//                            //可能平仓了
//
//                        } else {
                        //正常
                        OrderPosition orderPosition = new OrderPosition();
                        orderPosition.setSymbol(symbolPosition);
                        orderPosition.setAvgPrice(ordered.getBigDecimal("avgPrice"));
                        //Buy: 多头; Sell: 空头
                        orderPosition.setSide(ordered.getString("side").equalsIgnoreCase("buy") ? "long" : "short");
                        orderPosition.setSize(ordered.getBigDecimal("size"));
                        orderPosition.setEx(ExchangeType.BYBIT.getName());
                        orderPosition.setLever(ordered.getBigDecimal("leverage"));
                        orderPosition.setLiqPx(ordered.getBigDecimal("liqPrice"));
                        orderPosition.setFee(null);
                        orderPosition.setFundingFee(ordered.getBigDecimal("curRealisedPnl"));
                        orderPosition.setRealizedPnl(ordered.getBigDecimal("curRealisedPnl"));
                        BigDecimal unrealisedPnl = ordered.getBigDecimal("unrealisedPnl");
                        BigDecimal curRealisedPnl = ordered.getBigDecimal("curRealisedPnl");
                        if (Objects.nonNull(unrealisedPnl) && Objects.nonNull(curRealisedPnl)) {
                            orderPosition.setProfit(unrealisedPnl.add(curRealisedPnl));
                        } else {
                            orderPosition.setProfit(BigDecimal.ZERO);
                        }
                        orderPosition.setSettledPnl(ordered.getBigDecimal("curRealisedPnl"));
//                            orderPosition.setFee(BigDecimal.ZERO);
//                            orderPosition.setFundingFee(BigDecimal.ZERO);
                        orderPosition.setSymbol(sourceSymbol);

                        orderPosition.setAccountId(account.getId());
                        return orderPosition;
//                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查询平仓收益
     *
     * @param account
     * @param symbol
     * @param params
     * @return
     */
    @Override
    public BigDecimal queryClosePositionProfit(Account account, String symbol, PositionParams params) {
        if (Objects.isNull(params.getCloseOrderIds())) {
            throw new RuntimeException("bybit 平仓订单不能为空");
        }
        List<String> closeOrderIds = params.getCloseOrderIds();


        var client = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newPositionRestClient();
        var closPnlRequest = PositionDataRequest.builder().category(CategoryType.LINEAR)
            .startTime(params.getStartTime())
            .endTime(params.getEndTime()).symbol(checkSymbolLiner(symbol)).build();
        //查询出范围内所有的订单


        Object closePnlList = client.getClosePnlList(closPnlRequest);
        List<BybitOrderCloseItem> all = new ArrayList<>();
        JSONObject data = JSONObject.from(closePnlList);
        if (data.getInteger("retCode") == 0) {
            JSONObject result = data.getJSONObject("result");
            List<BybitOrderCloseItem> bybitorders = JSONArray.parseArray(result.getJSONArray("list").toJSONString(), BybitOrderCloseItem.class);
            all.addAll(bybitorders);
            if (result.containsKey("nextPageCursor")) {
                //还有下一页
                while (true) {
                    PositionDataRequest nextPageCursor = PositionDataRequest.builder()
                        .category(CategoryType.LINEAR).startTime(params.getStartTime())
                        .endTime(params.getEndTime()).symbol(checkSymbolLiner(symbol))
                        .cursor(result.getString("nextPageCursor")).build();
                    Object nextCloseList = client.getClosePnlList(nextPageCursor);
                    JSONObject from = JSONObject.from(nextCloseList);
                    log.info("nextPageCursor ｛｝", result.getString("nextPageCursor"));
                    if (from.getInteger("retCode") == 0) {
                        result = from.getJSONObject("result");
                        bybitorders = JSONArray.parseArray(result.getJSONArray("list").toJSONString(), BybitOrderCloseItem.class);
                        all.addAll(bybitorders);
                        if (StringUtils.isEmpty(result.getString("nextPageCursor"))) {
                            break;
                        }
                    } else {
                        break;
                    }
                }

            }

        } else {
            throw new RuntimeException(data.getString("retMsg"));
        }
        //开始统计盈亏
        BigDecimal totalProfit = BigDecimal.ZERO;
        Map<String, BybitOrderCloseItem> orderCloseItemMap = all.stream().collect(Collectors.toMap(BybitOrderCloseItem::getOrderId, o -> o));
        for (String closeOrderId : closeOrderIds) {
            if (orderCloseItemMap.containsKey(closeOrderId)) {
                BybitOrderCloseItem bybitOrderCloseItem = orderCloseItemMap.get(closeOrderId);
                log.info("找到平仓订单 ｛｝ {}", closeOrderId, JSONObject.toJSONString(bybitOrderCloseItem));
                if (Objects.nonNull(bybitOrderCloseItem.getClosedPnl())) {
                    totalProfit = totalProfit.add(bybitOrderCloseItem.getClosedPnl());
                }
            }
        }
        //广播bybit 平仓盈亏pnl
        for (BybitOrderCloseItem bybitOrderCloseItem : all) {
            BybitClosePnlMessage bybitClosePnlMessage = new BybitClosePnlMessage();
            bybitClosePnlMessage.setOrderId(bybitOrderCloseItem.getOrderId());
            bybitClosePnlMessage.setPnlAmount(bybitOrderCloseItem.getClosedPnl());
            bybitClosePnlMessage.setAccount(account);
            SpringUtils.getApplicationContext().publishEvent(bybitClosePnlMessage);
        }

        return totalProfit;
    }


    @Override
    public SymbolFundingRate getSymbolFundingRate(String sourceSymbol) {
        String symbol = checkSymbolLiner(sourceSymbol);

        CompletableFuture<SymbolFundingRate> bybitFundingRateFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory.newInstance().newMarketDataRestClient();
            MarketDataRequest build = MarketDataRequest.builder().category(CategoryType.LINEAR).symbol(symbol).limit(1).build();
            Object fundingHistory = bybitApiMarketRestClient.getFundingHistory(build);
            JSONObject from = JSONObject.from(fundingHistory);
            if (from.getInteger("retCode") == 0) {
                JSONObject result = from.getJSONObject("result");
                JSONArray list = result.getJSONArray("list");
                for (Object o : list) {
                    JSONObject fundingItem = JSONObject.from(o);
                    String fundingSymbol = fundingItem.getString("symbol");
                    if (symbol.equalsIgnoreCase(fundingSymbol)) {
                        SymbolFundingRate symbolFundingRate = new SymbolFundingRate();
                        Long fundingRateTimestamp = fundingItem.getLong("fundingRateTimestamp");
                        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(null, sourceSymbol);
                        if (Objects.nonNull(contractCoinInfo.getFundingInterval())) {
                            fundingRateTimestamp = fundingRateTimestamp + TimeUnit.MINUTES.toMillis(contractCoinInfo.getFundingInterval().longValue());
                        }
                        BigDecimal fundingRate = fundingItem.getBigDecimal("fundingRate");
                        symbolFundingRate.setNextFundingTime(fundingRateTimestamp);
                        symbolFundingRate.setFundingRate(fundingRate);
                        return symbolFundingRate;
                    }
                }

            }
            return null;
        }, bybitThreadExecutor);


        CompletableFuture<SymbolFundingRate> coinsGlassFundingRateFuture = CompletableFuture.supplyAsync(() -> {
            BigDecimal fundingRate = iCoinGlassService.queryFundingRateBySymbol(sourceSymbol, ExchangeType.BYBIT.getName());
            SymbolFundingRate symbolFundingRate = new SymbolFundingRate();
            symbolFundingRate.setFundingRate(fundingRate);
            return symbolFundingRate;
        }, bybitThreadExecutor);

        SymbolFundingRate bybitFundingRate = bybitFundingRateFuture.join();
        SymbolFundingRate coinsGlassFundingRate = coinsGlassFundingRateFuture.join();

        if (Objects.isNull(bybitFundingRate)) {
            SymbolFundingRate symbolFundingRate = new SymbolFundingRate();
            return symbolFundingRate;
        }
        if (Objects.isNull(coinsGlassFundingRate)) {
            return bybitFundingRate;
        }
        bybitFundingRate.setFundingRate(coinsGlassFundingRate.getFundingRate().divide(new BigDecimal(100), 8, BigDecimal.ROUND_HALF_UP));

        return bybitFundingRate;
    }


    public AccountBalance getBalancebk(Account account, String coin) {

        CompletableFuture<JSONObject> unifiedFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiAssetRestClient bybitApiAssetRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAssetRestClient();
            AssetDataRequest request = AssetDataRequest.builder().accountType(AccountType.UNIFIED).coin(coin).build();
            Object walletBalance = bybitApiAssetRestClient.getAssetAllCoinsBalance(request);
            JSONObject balance = JSONObject.from(walletBalance);
            if (balance.getInteger("retCode") == 0) {
                //success
                JSONObject result = balance.getJSONObject("result");
                JSONArray list = result.getJSONArray("balance");
                for (Object o : list) {
                    JSONObject coinData = JSONObject.from(o);

                    if (coinData.getString("coin").equalsIgnoreCase(coin)) {
                        return coinData;

                    }
                }


            }
            log.error("bybit {}", balance.getString("retMsg"));
            return null;
        });

        CompletableFuture<JSONObject> fundFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiAssetRestClient bybitApiAssetRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAssetRestClient();
            AssetDataRequest request = AssetDataRequest.builder().accountType(AccountType.FUND).coin(coin).build();
            Object walletBalance = bybitApiAssetRestClient.getAssetAllCoinsBalance(request);
            JSONObject balance = JSONObject.from(walletBalance);
            if (balance.getInteger("retCode") == 0) {
                //success
                JSONObject result = balance.getJSONObject("result");
                JSONArray array = result.getJSONArray("balance");
                for (Object o : array) {
                    JSONObject coinData = JSONObject.from(o);
                    if (coinData.getString("coin").equalsIgnoreCase(coin)) {
                        return coinData;
                    }
                }
            }
            log.error("bybit {}", balance.getString("retMsg"));
            return null;
        });


        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setSymbol(coin);
        JSONObject unifiedData = unifiedFuture.join();
        if (unifiedData != null) {
            BigDecimal equity = unifiedData.getBigDecimal("walletBalance");
            accountBalance.setBalance(equity);
        }
        JSONObject optionData = fundFuture.join();
        if (optionData != null) {
            BigDecimal equity = optionData.getBigDecimal("walletBalance");
            accountBalance.setUsdtBalance(equity);
        }


        return accountBalance;
    }

    @Override
    public AccountBalance getBalance(Account account, String coin) {


        CompletableFuture<JSONObject> freeFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiAccountRestClient bybitApiAccountRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAccountRestClient();
            AccountDataRequest request = AccountDataRequest.builder().accountType(AccountType.UNIFIED).coin(coin).build();
            Object walletBalance = bybitApiAccountRestClient.getWalletBalance(request);
            JSONObject balance = JSONObject.from(walletBalance);
            if (balance.getInteger("retCode") == 0) {
                //success
                JSONObject result = balance.getJSONObject("result");
                JSONArray list = result.getJSONArray("list");
                for (Object o : list) {
                    JSONObject coinData = JSONObject.from(o);
                    JSONArray coins = coinData.getJSONArray("coin");
                    for (Object object : coins) {
                        JSONObject coinDataItem = JSONObject.from(object);
                        if (coinDataItem.getString("coin").equalsIgnoreCase(coin)) {
                            coinDataItem.put("totalEquity", coinData.getBigDecimal("totalEquity"));
                            return coinDataItem;
                        }
                    }
                }


            }
            log.error("bybit {}", balance.getString("retMsg"));
            return null;
        });
        CompletableFuture<JSONObject> fundFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiAssetRestClient bybitApiAssetRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAssetRestClient();
            AssetDataRequest request = AssetDataRequest.builder().accountType(AccountType.FUND).coin(coin).build();
            Object walletBalance = bybitApiAssetRestClient.getAssetAllCoinsBalance(request);
            JSONObject balance = JSONObject.from(walletBalance);
            if (balance.getInteger("retCode") == 0) {
                //success
                JSONObject result = balance.getJSONObject("result");
                JSONArray array = result.getJSONArray("balance");
                for (Object o : array) {
                    JSONObject coinData = JSONObject.from(o);
                    if (coinData.getString("coin").equalsIgnoreCase(coin)) {
                        return coinData;
                    }
                }
            }
            log.error("bybit {}", balance.getString("retMsg"));
            return null;
        });


        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setSymbol(coin);
        JSONObject unifiedData = freeFuture.join();
        if (unifiedData != null) {
            // walletBalance - totalPositionIM - totalOrderIM - locked - bonus
            BigDecimal totalEquity = unifiedData.getBigDecimal("totalEquity");
            BigDecimal walletBalance = unifiedData.getBigDecimal("walletBalance");
            BigDecimal totalPositionIM = unifiedData.getBigDecimal("totalPositionIM");
            BigDecimal totalOrderIM = unifiedData.getBigDecimal("totalOrderIM");
            BigDecimal bonus = unifiedData.getBigDecimal("bonus");
            BigDecimal locked = unifiedData.getBigDecimal("locked");

            accountBalance.setBalance(totalEquity);
            accountBalance.setEquity(totalEquity);
            accountBalance.setFreeBalance(walletBalance.subtract(totalPositionIM).subtract(totalOrderIM).subtract(locked).subtract(bonus));
        }
        JSONObject optionData = fundFuture.join();
        if (optionData != null) {
            BigDecimal equity = optionData.getBigDecimal("walletBalance");
            accountBalance.setUsdtBalance(equity);
        }
        accountBalance.setApiId(account.getId());
        accountBalance.setApiName(account.getName());
        return accountBalance;
    }

    /**
     * {
     * "retCode": 0,
     * "retMsg": "OK",
     * "result": {
     * "list": [
     * {
     * "symbol": "ETHUSDT",
     * "takerFeeRate": "0.0006",
     * "makerFeeRate": "0.0001"
     * }
     * ]
     * },
     * "retExtInfo": {},
     * "time": 1676360412576
     * }
     *
     * @param account
     * @param symbol
     * @return
     */
    @Override
    public SymbolFee getFee(Account account, String symbol) {
        CompletableFuture<JSONObject> linearFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiAccountRestClient bybitApiAccountRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAccountRestClient();
            AccountDataRequest request = AccountDataRequest.builder().category(CategoryType.LINEAR).symbol(symbol + "USDT").build();
            Object walletBalance = bybitApiAccountRestClient.getAccountFreeRate(request);
            JSONObject response = JSONObject.from(walletBalance);
            if (response.getInteger("retCode") == 0) {
                JSONObject result = response.getJSONObject("result");
                JSONArray list = result.getJSONArray("list");
                for (Object item : list) {
                    JSONObject feeData = JSONObject.from(item);
                    return feeData;
                }
            }
            return null;
        });
        CompletableFuture<JSONObject> sportFuture = CompletableFuture.supplyAsync(() -> {
            BybitApiAccountRestClient bybitApiAccountRestClient = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd()).newAccountRestClient();
            AccountDataRequest request = AccountDataRequest.builder().category(CategoryType.SPOT).baseCoin("USDT").symbol(symbol + "USDT").build();
            Object walletBalance = bybitApiAccountRestClient.getAccountFreeRate(request);
            JSONObject response = JSONObject.from(walletBalance);
            if (response.getInteger("retCode") == 0) {
                JSONObject result = response.getJSONObject("result");
                JSONArray list = result.getJSONArray("list");
                for (Object item : list) {
                    JSONObject feeData = JSONObject.from(item);
                    String feeSymbol = symbol + "USDT";
                    if (feeSymbol.equalsIgnoreCase(feeData.getString("symbol"))) {
                        return feeData;
                    }
                }
            }
            return null;
        });
        JSONObject linerFee = linearFuture.join();
        JSONObject sportFee = sportFuture.join();
        SymbolFee symbolFee = new SymbolFee();
        if (Objects.nonNull(linerFee)) {
            symbolFee.setLinerMakerFeeRate(linerFee.getBigDecimal("makerFeeRate"));
            symbolFee.setLinerTakerFeeRate(linerFee.getBigDecimal("takerFeeRate"));
        } else {
            symbolFee.setLinerMakerFeeRate(null);
            symbolFee.setLinerTakerFeeRate(null);
        }
        if (Objects.nonNull(sportFee)) {
            symbolFee.setSportMakerFeeRate(sportFee.getBigDecimal("makerFeeRate"));
            symbolFee.setSportTakerFeeRate(sportFee.getBigDecimal("takerFeeRate"));
        } else {
            symbolFee.setSportMakerFeeRate(null);
            symbolFee.setSportTakerFeeRate(null);
        }


        return symbolFee;
    }

    @Override
    @Cacheable(value = "bybitNowPrice:cache#1s#5s#2000", key = "'bybit'+':'+'contract'+':'+#symbol", condition = "#symbol != null ")
    public BigDecimal getNowPrice(Account account, String symbol) {
        symbol = checkSymbolLiner(symbol);
        BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        MarketDataRequest build = MarketDataRequest.builder().category(CategoryType.LINEAR).symbol(symbol).limit(1).build();
        Object marketTickers = bybitApiMarketRestClient.getMarketTickers(build);
        JSONObject response = JSONObject.from(marketTickers);
        if (response.getInteger("retCode") == 0) {
            JSONObject result = response.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list != null && list.size() > 0) {
                JSONObject order = list.getJSONObject(0);
                BigDecimal markPrice = order.getBigDecimal("markPrice");
                return markPrice;
            }
        }
        return null;
    }

    @Override
    public TickerItem getNowPrice(Account account, String symbol, String bitgetOnly) {
        return null;
    }

    @Override
    public OrderVo calcOrderSize(OrderVo longOrder) {

        return longOrder;
    }

    @Override
    public BigDecimal calcOrderSize(String symbol, BigDecimal size) {
        return size;
    }

    @Override
    public BigDecimal calcShowSize(String symbol, BigDecimal size) {
        return size;
    }

    @Override
    public OrderOptStatus queryContractOrderStatus(Account account, OrderOptStatus order) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());
        //先查订单状态
        BybitApiTradeRestClient tradeClient = factory.newTradeRestClient();
        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
            .category(CategoryType.LINEAR)
            .symbol(checkSymbolLiner(order.getSymbol()))
            .orderId(order.getOrderId()).build();
        Object openOrders = tradeClient.getOpenOrders(tradeOrderRequest);
        JSONObject from = JSONObject.from(openOrders);
        log.info("查询订单状态 {} {}", order.getOrderId(), from);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list.isEmpty()) {

            } else {
                for (Object o : list) {
                    JSONObject ordered = JSONObject.from(o);
                    String orderStatus = ordered.getString("orderStatus");
                    List<String> bybitProcessStatus = OrderStatusConstant.bybitProcessStatus;
                    List<String> bybitEndStatus = OrderStatusConstant.bybitEndStatus;
                    if (bybitProcessStatus.contains(orderStatus)) {
                        //中间态
                        OrderOptStatus orderOptStatus = new OrderOptStatus();
                        orderOptStatus.setOrderId(order.getOrderId());

                        orderOptStatus.setStatus(CrossOrderStatus.PROCESS);
                        return orderOptStatus;
                    } else if (bybitEndStatus.contains(orderStatus)) {
                        //成交,查询持仓
                        OrderOptStatus orderOptStatus = new OrderOptStatus();
                        orderOptStatus.setOrderId(order.getOrderId());
                        orderOptStatus.setFee(ordered.getBigDecimal("cumExecFee"));
                        orderOptStatus.setAvgPrice(ordered.getBigDecimal("avgPrice"));
                        orderOptStatus.setStatus(CrossOrderStatus.END);
                        return orderOptStatus;
                    }
                }
            }
        }
        OrderOptStatus orderOptStatus = new OrderOptStatus();
        orderOptStatus.setOrderId(order.getOrderId());
        orderOptStatus.setStatus(CrossOrderStatus.UNKNOW);
        return orderOptStatus;
    }

    @Override
    public List<ContractOrder> queryContractOrdersByIds(Account account, List<String> orderIds, String symbol) {
        symbol = checkSymbolLiner(symbol);
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());
        //先查订单状态
        BybitApiTradeRestClient tradeClient = factory.newTradeRestClient();
        ArrayList<ContractOrder> orderDatas = new ArrayList<>();
        HashSet<String> findOrders = new HashSet<>();

        TradeOrderRequest request = TradeOrderRequest.builder()
            .category(CategoryType.LINEAR).symbol(symbol).limit(50).build();
//                .orderId(orderId).build();
        Object openOrders = tradeClient.getOrderHistory(request);
        JSONObject from = JSONObject.from(openOrders);
        log.info("查询订单状态 {}", from);
        if (from.getInteger("retCode") == 0) {
            JSONObject result = from.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list.isEmpty()) {

            } else {
                for (Object o : list) {
                    JSONObject ordered = JSONObject.from(o);
                    String orderId = ordered.getString("orderId");
                    if (orderIds.contains(orderId) && !findOrders.contains(orderId)) {
                        findOrders.add(orderId);
                        //找到需要查询的订单了
                        String orderStatus = ordered.getString("orderStatus");

                        ContractOrder contractOrder = new ContractOrder();
                        contractOrder.setOrderId(orderId);
                        contractOrder.setFee(ordered.getBigDecimal("cumExecFee").negate());
                        contractOrder.setAvgPrice(ordered.getBigDecimal("avgPrice"));
                        contractOrder.setEx(ExchangeType.BYBIT.getName());
                        contractOrder.setSize(ordered.getBigDecimal("qty"));
//                        String side = ordered.getString("side");
//                        if (StringUtils.isNotEmpty(side)) {
//                            contractOrder.setSide("Buy".equalsIgnoreCase(side) ? SideType.LONG : SideType.SHORT);
//                        }
                        contractOrder.setPrice(ordered.getBigDecimal("price"));
                        contractOrder.setStatus(orderStatus.toLowerCase());
                        contractOrder.setLeavesQty(ordered.getBigDecimal("leavesQty"));
                        contractOrder.setLeavesValue(ordered.getBigDecimal("leavesValue"));
                        contractOrder.setCumExecQty(ordered.getBigDecimal("cumExecQty"));
                        contractOrder.setCumExecValue(ordered.getBigDecimal("cumExecValue"));

                        List<String> bybitProcessStatus = OrderStatusConstant.bybitProcessStatus;
                        List<String> bybitEndStatus = OrderStatusConstant.bybitEndStatus;
                        if (bybitProcessStatus.contains(orderStatus)) {
                            //中间状态，还没成交
                            contractOrder.setOrderEnd(false);
                        } else if (bybitEndStatus.contains(orderStatus)) {
                            //最终态
                            contractOrder.setOrderEnd(true);
                        }
                        orderDatas.add(contractOrder);

                    }

                }
            }
        }

        //查剩余的没找到的
        for (String orderId : orderIds) {
            if (!findOrders.contains(orderId)) {
                //没找到
                log.info("bybit 订单 {} 没找到", orderId);
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOrderId(orderId);
                JSONObject ordered = this.checkContractOrder(account, orderId);
                if (Objects.isNull(ordered)) {
                    continue;
                }
                String orderStatus = ordered.getString("orderStatus");
                ContractOrder contractOrder = new ContractOrder();
                contractOrder.setOrderId(orderId);
                contractOrder.setFee(ordered.getBigDecimal("cumExecFee").negate());
                contractOrder.setAvgPrice(ordered.getBigDecimal("avgPrice"));
                contractOrder.setEx(ExchangeType.BYBIT.getName());
                contractOrder.setSize(ordered.getBigDecimal("qty"));
//                String side = ordered.getString("side");
//                if (StringUtils.isNotEmpty(side)) {
//                    contractOrder.setSide("Buy".equalsIgnoreCase(side) ? SideType.LONG : SideType.SHORT);
//                }
                contractOrder.setPrice(ordered.getBigDecimal("price"));
                contractOrder.setStatus(orderStatus.toLowerCase());
                contractOrder.setLeavesQty(ordered.getBigDecimal("leavesQty"));
                contractOrder.setLeavesValue(ordered.getBigDecimal("leavesValue"));
                contractOrder.setCumExecQty(ordered.getBigDecimal("cumExecQty"));
                contractOrder.setCumExecValue(ordered.getBigDecimal("cumExecValue"));

                List<String> bybitProcessStatus = OrderStatusConstant.bybitProcessStatus;
                List<String> bybitEndStatus = OrderStatusConstant.bybitEndStatus;
                if (bybitProcessStatus.contains(orderStatus)) {
                    //中间状态，还没成交
                    contractOrder.setOrderEnd(false);
                } else if (bybitEndStatus.contains(orderStatus)) {
                    //最终态
                    contractOrder.setOrderEnd(true);
                }
                orderDatas.add(contractOrder);

            }
        }

        return orderDatas;
    }

    @Override
    public OrderVo updateContractOrder(Account account, OrderVo vo) {

        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());
        //先查订单状态
        BybitApiTradeRestClient tradeClient = factory.newTradeRestClient();
        TradeOrderRequest.TradeOrderRequestBuilder builder = TradeOrderRequest.builder()
            .category(CategoryType.LINEAR)
            .symbol(checkSymbolLiner(vo.getSymbol()))
            .orderId(vo.getOrderId());

        /**
         * qty	false	string	修改后的订单数量. 若不修改，请不要传该字段
         * price	false	string	修改后的订单价格. 若不修改，请不要传该字段
         */
        if (Objects.nonNull(vo.getPrice())) {
            builder.price(vo.getPrice().toPlainString());
        }
        if (Objects.nonNull(vo.getSize())) {
            builder.qty(vo.getSize().toPlainString());
        }
        TradeOrderRequest build = builder.build();


        Object amendOrder = tradeClient.amendOrder(build);
        JSONObject from = JSONObject.from(amendOrder);
        log.info("订单改价 {} {}", vo.getOrderId(), from);
        if (from.getInteger("retCode") == 0 || from.getInteger("retCode") == 10001) {
            log.info("订单改价成功！");
            return vo;
        } else {
            throw new RuntimeException(from.getString("retMsg"));
        }

    }

    @Override
    public void preCheckOrder(OrderVo order) {
        if (Objects.nonNull(order.getReduceOnly()) && order.getReduceOnly()) {
            return;
        }
        if(OrderType.MARKET.equalsIgnoreCase(order.getOrderType())) {
            return;
        }
        BigDecimal total = order.getPrice().multiply(order.getSize()).multiply(order.getLeverage());
        if (total.doubleValue() < 5) {
            throw new RuntimeException("Bybit 每单价值至少5USDT,请重新填写下单数量");
        }
    }

    @Override
    public List<LinerSymbol> getAllLinerSymbol() {
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        var instrumentInfoRequest = MarketDataRequest.builder().category(CategoryType.LINEAR).instrumentStatus(InstrumentStatus.TRADING).limit(1000).build();
        Object instrumentsInfo = client.getInstrumentsInfo(instrumentInfoRequest);

        List<LinerSymbol> all = new ArrayList<>();
        JSONObject data = JSONObject.from(instrumentsInfo);
        if (data.getInteger("retCode") == 0) {
            JSONObject result = data.getJSONObject("result");
            List<LinerSymbolItem> linerSymbolItems = JSONArray.parseArray(result.getJSONArray("list").toJSONString(), LinerSymbolItem.class);
//            all.addAll(new LinerSymbol());
            all.addAll(formateLinerSymbol(linerSymbolItems));
            if (result.containsKey("nextPageCursor") && linerSymbolItems.size() == 1000) {
                //还有下一页
                while (true) {
                    MarketDataRequest nextPageCursor = MarketDataRequest.builder().category(CategoryType.LINEAR).instrumentStatus(InstrumentStatus.TRADING)
                        .limit(1000).cursor(result.getString("nextPageCursor")).build();
                    Object nextCloseList = client.getInstrumentsInfo(nextPageCursor);
                    JSONObject from = JSONObject.from(nextCloseList);
                    log.info("nextPageCursor ｛｝", result.getString("nextPageCursor"));
                    if (from.getInteger("retCode") == 0) {
                        result = from.getJSONObject("result");
                        linerSymbolItems = JSONArray.parseArray(result.getJSONArray("list").toJSONString(), LinerSymbolItem.class);
                        all.addAll(formateLinerSymbol(linerSymbolItems));
                        if (StringUtils.isEmpty(result.getString("nextPageCursor")) || linerSymbolItems.size() < 1000) {
                            break;
                        }
                    } else {
                        break;
                    }
                }

            }

        } else {
            throw new RuntimeException(data.getString("retMsg"));
        }
        return all;
    }

    @Override
    public Integer getLinerSymbolFundingRateInterval(String symbol) {
        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(null, symbol);
        BigDecimal fundingInterval = contractCoinInfo.getFundingInterval();
        int interval = fundingInterval.intValue() / 60;
        return interval;
    }

    @Override
    public ContractOrder formateOrderBySyncOrderInfo(OrderOptStatus orderStatus, Account account, SyncOrderDetail syncOrderDetail) {
        return null;
    }

    @Override
    public List<PositionWsData> queryContractPositionDetail(Account account, PositionParams params) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(account.getApiSecurity(), account.getApiPwd());
        PositionDataRequest request = PositionDataRequest.builder()
            .category(CategoryType.LINEAR)
            .settleCoin("USDT").build();

        List<PositionWsData> wsDatas = new ArrayList<>();

        Object positionInfo = factory.newPositionRestClient().getPositionInfo(request);
        JSONObject positionObject = JSONObject.from(positionInfo);
        log.info("查询持仓状态 {}", positionObject);
        if (positionObject.getInteger("retCode") == 0) {
            JSONObject result = positionObject.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            if (list.isEmpty()) {

            } else {
                for (Object o : list) {
                    JSONObject ordered = JSONObject.from(o);
                    String symbolPosition = ordered.getString("symbol");
//                        if (ordered.getBigDecimal("size").doubleValue() == 0d) {
//                            log.error("无持仓 {}", symbolPosition);
//                            //可能平仓了
//
//                        } else {
                    //正常
                    PositionWsData orderPosition = new PositionWsData();
                    orderPosition.setSymbol(symbolPosition);
                    orderPosition.setAvgPrice(ordered.getBigDecimal("avgPrice"));
                    //Buy: 多头; Sell: 空头
                    orderPosition.setSide(ordered.getString("side").equalsIgnoreCase("buy") ? "long" : "short");
                    orderPosition.setMarginType(ordered.getInteger("tradeMode") == 0?"cross":"isolated");
                    orderPosition.setSize(ordered.getBigDecimal("size"));
                    orderPosition.setExchange(ExchangeType.BYBIT.getName());
                    orderPosition.setLeverage(ordered.getBigDecimal("leverage"));
                    orderPosition.setLiqPrice(ordered.getBigDecimal("liqPrice"));
                    orderPosition.setFee(null);
                    orderPosition.setFundingFee(ordered.getBigDecimal("curRealisedPnl"));
                    orderPosition.setProfit(ordered.getBigDecimal("curRealisedPnl"));
                    BigDecimal unrealisedPnl = ordered.getBigDecimal("unrealisedPnl");
                    orderPosition.setUnrealizedProfit(unrealisedPnl);
                    orderPosition.setSymbol(deSymbolLiner(ordered.getString("symbol")));

                    orderPosition.setUpdateTime(new Date(ordered.getLong("updatedTime")));
                    orderPosition.setServerTime(new Date());
                    orderPosition.setAccountId(account.getId());
                    wsDatas.add(orderPosition);
                }
            }
        }

        return wsDatas;
    }

    private Collection<? extends LinerSymbol> formateLinerSymbol(List<LinerSymbolItem> linerSymbolItems) {
        List<LinerSymbol> results = new ArrayList<>();
        for (LinerSymbolItem linerSymbolItem : linerSymbolItems) {
            LinerSymbol linerSymbol = new LinerSymbol();
            linerSymbol.setCoin(linerSymbolItem.getBaseCoin());
            linerSymbol.setSymbol(linerSymbolItem.getSymbol());
            linerSymbol.setFundingInterval(linerSymbolItem.getFundingInterval() / 60);
            results.add(linerSymbol);
        }
        return results;
    }
}
