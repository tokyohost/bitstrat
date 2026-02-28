package com.bitstrat.strategy.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.annotation.AccountPaptrading;
import com.bitstrat.client.OkxRestClient;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.*;
import com.bitstrat.domain.okx.FundHistoryItem;
import com.bitstrat.domain.okx.OkxTpSlOrderItem;
import com.bitstrat.domain.okx.OkxTpSlOrderItemVo;
import com.bitstrat.domain.okx.SwapSymbol;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.domain.wsdomain.OkxOrderAlgo;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.ContractLotCalculator;
import com.bitstrat.utils.StringListUtil;
import com.bitstrat.wsClients.msg.OkxWsMsg;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import jakarta.ws.rs.NotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/18 16:46
 * @Content
 */
@Slf4j
@Service
@AccountPaptrading
public class OkxExchangeRestServiceImpl implements ExchangeService {
    OkxWsMsg okxWsMsg = new OkxWsMsg();
    @Autowired
    OkxRestClient okxRestClient;

    @Override
    public SubscriptMsgs getWsSubscriptMsgs() {

        return okxWsMsg;
    }

    @Override
    public String getExchangeName() {
        return ExchangeType.OKX.getName();
    }

    @Override
    @Cacheable(value = "okxContractInfo:cache#30s#60s#1000", key = "'okx'+':'+'contract:symbolInfo'+':'+#symbol", condition = "#symbol != null ")
    @AccountPaptrading
    public CoinContractInfomation getContractCoinInfo(Account account, String symbol) {
        symbol = checkSwapSymbol(symbol);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instType", "SWAP");
        queryParams.put("instId", symbol);
        ResponseEntity<String> noAuth = okxRestClient.getNoAuth("/api/v5/public/instruments", queryParams);
        JSONObject info = JSONObject.parseObject(noAuth.getBody());
        if (info.getInteger("code") == 0) {
            JSONArray dataArray = info.getJSONArray("data");
            for (Object o : dataArray) {
                JSONObject symbolItem = JSONObject.from(o);
                if (symbolItem.getString("instId").equalsIgnoreCase(symbol)) {
                    CoinContractInfomation coinContractInfomation = new CoinContractInfomation();
                    coinContractInfomation.setSymbol(symbol);
                    coinContractInfomation.setStep(symbolItem.getBigDecimal("ctVal").multiply(symbolItem.getBigDecimal("lotSz")));
                    coinContractInfomation.setOkxMinSz(symbolItem.getBigDecimal("minSz"));
                    coinContractInfomation.setMinSz(symbolItem.getBigDecimal("ctVal").multiply(symbolItem.getBigDecimal("lotSz")));
                    coinContractInfomation.setMaxMktSz(symbolItem.getBigDecimal("maxMktSz"));
                    coinContractInfomation.setCtMult(symbolItem.getBigDecimal("ctMult"));
                    coinContractInfomation.setContractValue(symbolItem.getBigDecimal("ctVal"));
                    coinContractInfomation.setMaxLeverage(symbolItem.getInteger("lever"));
                    coinContractInfomation.setMinLeverage(1);
                    coinContractInfomation.setCalcPlaces(symbolItem.getBigDecimal("lotSz").stripTrailingZeros().scale());
                    coinContractInfomation.setMaxLmtSz(symbolItem.getBigDecimal("maxLmtSz"));
                    return coinContractInfomation;

                }
            }
        }
        return null;
    }

    @Override
    public String setLeverage(Account account, Integer leverage, String symbol, String side) {
        symbol = checkSwapSymbol(symbol);
        JSONObject position = new JSONObject();
        /**
         * {
         *     "instId":"BTC-USDT-SWAP",
         *     "lever":"5",
         *     "mgnMode":"isolated"
         * }
         */
        position.put("instId", symbol);
        position.put("lever", leverage + "");
        if (StringUtils.isNotBlank(side)) {
            if (CrossContractSide.LONG.equalsIgnoreCase(side)) {
                position.put("posSide", CrossContractSide.LONG);
            } else if (CrossContractSide.SHORT.equalsIgnoreCase(side)) {
                position.put("posSide", CrossContractSide.SHORT);
            }
        }
//        position.put("posSide", "long");//"posSide":"long",
        position.put("mgnMode", "cross");
//        position.put("posMode", "long_short_mode");
        ResponseEntity<String> leverResut = okxRestClient.post("/api/v5/account/set-leverage", position.toJSONString(), account);
        JSONObject jsonObject = JSONObject.parseObject(leverResut.getBody());
        if (jsonObject.getInteger("code") == 0) {
            return CorssLeverageStatus.SUCCESS;
        } else {
            log.error("okx setLeverage error:{}", jsonObject);
            throw new RuntimeException("okx setLeverage error");
        }
    }


    @Override
    public boolean checkApi(Account account) {
        HashMap<String, String> queryParams = new HashMap<>();
        ResponseEntity<String> stringResponseEntity = okxRestClient.get("/api/v5/account/balance", queryParams, account);
        JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
        if (response.getInteger("code") == 0) {
            return true;
        }
        return false;
    }

    @Override
    public OrderOptStatus buyContract(Account account, OrderVo params) {
        String symbol = params.getSymbol();
        symbol = checkSwapSymbol(symbol);
        long l = IdUtil.getSnowflake().nextId();
        log.info("自定义订单id {}", l);
        JSONObject order = new JSONObject();
        order.put("instId", symbol);
        order.put("tdMode", "cross"); // 保证金模式：isolated：逐仓 ；cross：全仓
        order.put("ccy", "USDT");//保证金币种
        order.put("clOrdId", l + "");
        order.put("side", "buy");//buy 买入 sell
        /**
         * 	持仓方向
         * 在开平仓模式下必填，且仅可选择 long 或 short。 仅适用交割、永续。
         */
        order.put("posSide", "long");
        /**
         * 订单类型
         * market：市价单
         * limit：限价单
         * post_only：只做maker单
         * fok：全部成交或立即取消
         * ioc：立即成交并取消剩余
         * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
         * mmp：做市商保护(仅适用于组合保证金账户模式下的期权订单)
         * mmp_and_post_only：做市商保护且只做maker单(仅适用于组合保证金账户模式下的期权订单)
         */
        order.put("ordType", "market");
        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            order.put("ordType", "market");
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            order.put("ordType", "limit");
            order.put("px", params.getPrice().toPlainString());
        }
        //是否只减仓
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
            order.put("reduceOnly", true);

            //双向持仓模式买入 需要指定平仓的方向
            order.put("posSide", "short");
        }

        order = addTpSl(order, params);
        /**
         * 市价单委托数量sz的单位，仅适用于币币市价订单
         * base_ccy: 交易货币 ；quote_ccy：计价货币
         * 买单默认quote_ccy， 卖单默认base_ccy
         */

//        order.put("tgtCcy", "base_ccy");
        order.put("sz", params.getSize());

        ResponseEntity<String> post = okxRestClient.post("/api/v5/trade/order", order.toJSONString(), account);
        log.info(post.getBody());
        JSONObject jsonObject = JSONObject.parseObject(post.getBody());
        if (jsonObject.getInteger("code") == 0) {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (Object o : dataArray) {
                JSONObject orderResult = JSONObject.from(o);
                boolean b = orderResult.containsKey("ordId");
                if (b) {
                    OrderOptStatus orderOptStatus = new OrderOptStatus();
                    orderOptStatus.setOrderId(orderResult.getString("ordId"));
                    orderOptStatus.setSymbol(params.getSymbol());
                    orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
                    return orderOptStatus;
                }
            }
        }
        throw new RuntimeException("创建订单失败"+jsonObject.toJSONString());
    }

    @Override
    public OrderOptStatus sellContract(Account account, OrderVo params) {
        String symbol = params.getSymbol();
        symbol = checkSwapSymbol(symbol);
        long l = IdUtil.getSnowflake().nextId();
        log.info("自定义订单id {}", l);
        JSONObject order = new JSONObject();
        order.put("instId", symbol);
        order.put("tdMode", "cross"); // 保证金模式：isolated：逐仓 ；cross：全仓
        order.put("ccy", "USDT");//保证金币种
        order.put("clOrdId", l + "");
        order.put("side", "sell");//buy 买入 sell
        /**
         * 	持仓方向
         * 在开平仓模式下必填，且仅可选择 long 或 short。 仅适用交割、永续。
         */
        order.put("posSide", "short");
        /**
         * 订单类型
         * market：市价单
         * limit：限价单
         * post_only：只做maker单
         * fok：全部成交或立即取消
         * ioc：立即成交并取消剩余
         * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
         * mmp：做市商保护(仅适用于组合保证金账户模式下的期权订单)
         * mmp_and_post_only：做市商保护且只做maker单(仅适用于组合保证金账户模式下的期权订单)
         */
//        order.put("ordType", "market");
        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            order.put("ordType", "market");
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            order.put("ordType", "limit");
            order.put("px", params.getPrice().toPlainString());
        }

        //是否只减仓
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
            order.put("reduceOnly", true);
            //双向持仓模式买入 需要指定平仓的方向
            order.put("posSide", "long");
        }


        order = addTpSl(order, params);
        /**
         * 市价单委托数量sz的单位，仅适用于币币市价订单
         * base_ccy: 交易货币 ；quote_ccy：计价货币
         * 买单默认quote_ccy， 卖单默认base_ccy
         */
//        order.put("tgtCcy", "quote_ccy");
//        order.put("tgtCcy", "base_ccy");
        order.put("sz", params.getSize());

        ResponseEntity<String> post = okxRestClient.post("/api/v5/trade/order", order.toJSONString(), account);
        log.info(post.getBody());
        JSONObject jsonObject = JSONObject.parseObject(post.getBody());
        if (jsonObject.getInteger("code") == 0) {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (Object o : dataArray) {
                JSONObject orderResult = JSONObject.from(o);
                boolean b = orderResult.containsKey("ordId");
                if (b) {
                    OrderOptStatus orderOptStatus = new OrderOptStatus();
                    orderOptStatus.setOrderId(orderResult.getString("ordId"));
                    orderOptStatus.setSymbol(params.getSymbol());
                    orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
                    return orderOptStatus;
                }
            }
        }
        if (jsonObject.getInteger("code") == 51010) {
            throw new NotAllowedException("创建订单失败 请检查您的账户模式设置是否设置为<合约模式> " + jsonObject.getString("msg"));
        }
        throw new RuntimeException("创建订单失败 " + jsonObject.toJSONString());
    }

    private JSONObject addTpSl(JSONObject order, OrderVo params) {
        boolean hasTpsl = false;
        JSONObject algoOrd = new JSONObject();
        //是否止盈价格
        if (params.getTakeProfitPrice() != null) {
            algoOrd.put("tpTriggerPx", params.getTakeProfitPrice());
            algoOrd.put("tpOrdPx", "-1");
            hasTpsl = true;
        }
        //是否止盈 止损
        if (params.getStopLossPrice() != null) {
            algoOrd.put("slTriggerPx", params.getStopLossPrice());
            algoOrd.put("slOrdPx", "-1");
            hasTpsl = true;
        }
        if (hasTpsl) {
            JSONArray attachAlgoOrds = new JSONArray();
            attachAlgoOrds.add(algoOrd);
            order.put("attachAlgoOrds", attachAlgoOrds);
        }

        return order;
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
        throw new UnsupportedOperationException("unsupport");
    }

    @Override
    public void checkSpotOrder(Account account, JSONObject params) {

    }

    @Override
    public void cancelSpotOrder(Account account, OrderInfo params) {

    }

    @Override
    public String cancelContractOrder(Account account, OrderOptStatus orderOptStatus) {
        String symbol = checkSwapSymbol(orderOptStatus.getSymbol());
        //取消订单
        JSONObject order = new JSONObject();
        order.put("instId", symbol);
        order.put("ordId", orderOptStatus.getOrderId());

        ResponseEntity<String> responseEntity = okxRestClient.post("/api/v5/trade/cancel-order", order.toJSONString(), account);
        log.info("okx 取消订单结果 {}", responseEntity.getBody());
        JSONObject jsonObject = JSONObject.parseObject(responseEntity.getBody());
        if (jsonObject.getInteger("code") == 0) {
            return CrossOrderStatus.SUCCESS;
        }
        log.error("okx 取消订单报错 {}", jsonObject.getInteger("msg"));
        return CrossOrderStatus.FAIL;

    }

    @Override
    public OrderCloseResult closeContractPosition(Account account, OrderPosition params) {
        String symbol = params.getSymbol();
        symbol = checkSwapSymbol(symbol);
        long l = IdUtil.getSnowflake().nextId();
        log.info("自定义订单id {}", l);
        JSONObject order = new JSONObject();
        order.put("instId", symbol);
        order.put("tdMode", "cross"); // 保证金模式：isolated：逐仓 ；cross：全仓
        order.put("ccy", "USDT");//保证金币种
        order.put("reduceOnly", "true");//平仓
        order.put("clOrdId", l + "");
        String side = params.getSide().equalsIgnoreCase("long") ? "sell" : "buy";
        order.put("side", side);//buy 买入 sell
        /**
         * 	持仓方向
         * 在开平仓模式下必填，且仅可选择 long 或 short。 仅适用交割、永续。
         */
        order.put("posSide", params.getSide());
        /**
         * 订单类型
         * market：市价单
         * limit：限价单
         * post_only：只做maker单
         * fok：全部成交或立即取消
         * ioc：立即成交并取消剩余
         * optimal_limit_ioc：市价委托立即成交并取消剩余（仅适用交割、永续）
         * mmp：做市商保护(仅适用于组合保证金账户模式下的期权订单)
         * mmp_and_post_only：做市商保护且只做maker单(仅适用于组合保证金账户模式下的期权订单)
         */
        order.put("ordType", "market");
//        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
//            order.put("ordType", "market");
//        }else if(OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())){
//            order.put("ordType", "ioc");
//        }
        /**
         * 市价单委托数量sz的单位，仅适用于币币市价订单
         * base_ccy: 交易货币 ；quote_ccy：计价货币
         * 买单默认quote_ccy， 卖单默认base_ccy
         */
//        order.put("tgtCcy", "quote_ccy");
        order.put("sz", params.getSize());

        ResponseEntity<String> post = okxRestClient.post("/api/v5/trade/order", order.toJSONString(), account);
        log.info("okx 平仓 {}", post.getBody());
        JSONObject jsonObject = JSONObject.parseObject(post.getBody());
        OrderCloseResult result = new OrderCloseResult();
        if (jsonObject.getInteger("code") == 0) {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (Object o : dataArray) {
                JSONObject orderResult = JSONObject.from(o);
                boolean b = orderResult.containsKey("ordId");
                if (b) {
                    log.info("okx 平仓订单号 ：{}", orderResult.getString("ordId"));
                    result.setStatus(CrossOrderStatus.SUCCESS);
                    result.setMsg("平仓成功");

                    return result;
                }
            }
        }
        result.setStatus(CrossOrderStatus.FAIL);
        result.setMsg("平仓失败");
        result.setBody(post.getBody());
        return result;
    }

    @Override
    public OrderPosition queryContractPosition(Account account, String sourceSymbol, PositionParams params) {
        String symbol = checkSwapSymbol(sourceSymbol);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instId", symbol);
        ResponseEntity<String> response = okxRestClient.get("/api/v5/account/positions", queryParams, account);
        log.info("okx查询持仓: {}", response.getBody());
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject.getInteger("code") == 0) {
            JSONArray dataList = jsonObject.getJSONArray("data");
            for (Object o : dataList) {
                JSONObject position = JSONObject.from(o);
                if (position.getString("instId").equalsIgnoreCase(symbol)) {
                    OrderPosition orderPosition = new OrderPosition();
                    orderPosition.setPositionId(position.getString("posId"));
                    orderPosition.setSize(position.getBigDecimal("pos").abs());
                    if (orderPosition.getSize().doubleValue() == 0d) {
                        if (StringUtils.isNotEmpty(params.getPosId()) && params.getPosId().equalsIgnoreCase(orderPosition.getPositionId())) {
                            //已平仓位
                            log.info("仓位已平 ｛｝", orderPosition.getPositionId());
                        } else {

//                            continue;
                        }

                    }
                    orderPosition.setMarketPrice(position.getBigDecimal("markPx"));
                    orderPosition.setFee(position.getBigDecimal("fee"));
                    orderPosition.setLever(position.getBigDecimal("lever"));
                    orderPosition.setEx(ExchangeType.OKX.getName());
                    orderPosition.setSymbol(sourceSymbol);
                    orderPosition.setFundingFee(position.getBigDecimal("fundingFee"));
                    orderPosition.setMgnRatio(position.getBigDecimal("mgnRatio"));
                    orderPosition.setRealizedPnl(position.getBigDecimal("realizedPnl"));
                    orderPosition.setProfit(position.getBigDecimal("upl"));
                    orderPosition.setSettledPnl(position.getBigDecimal("settledPnl"));
                    orderPosition.setAvgPrice(position.getBigDecimal("avgPx"));
                    orderPosition.setLiqPx(position.getBigDecimal("liqPx"));
                    /**
                     * 持仓方向
                     * long：开平仓模式开多，pos为正
                     * short：开平仓模式开空，pos为正
                     * net：买卖模式（交割/永续/期权：pos为正代表开多，pos为负代表开空。币币杠杆时，pos均为正，posCcy为交易货币时，代表开多；posCcy为计价货币时，代表开空。）
                     */
                    orderPosition.setSide(position.getString("posSide"));
                    orderPosition.setAccountId(account.getId());
                    return orderPosition;

                }

            }
        }
        return null;
    }

    @Override
    public BigDecimal queryClosePositionProfit(Account account, String symbol, PositionParams params) {
        if (StringUtils.isNotEmpty(params.getPosId())) {
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("posId", params.getPosId());
            ResponseEntity<String> response = okxRestClient.get("/api/v5/account/positions-history", queryParams, account);
            log.info("okx查询历史持仓: {}", response.getBody());
            JSONObject jsonObject = JSONObject.parseObject(response.getBody());
            if (jsonObject.getInteger("code") == 0) {
                JSONArray dataList = jsonObject.getJSONArray("data");
                for (Object o : dataList) {
                    JSONObject position = JSONObject.from(o);
                    if (position.getString("posId").equalsIgnoreCase(params.getPosId())) {
                        return position.getBigDecimal("realizedPnl");
                    }
                }
            }

        }
        return null;
    }


    @Override
    /**
     *  入参格式！PROMPT-USDT-SWAP
     */
    @AccountPaptrading
    public SymbolFundingRate getSymbolFundingRate(String symbol) {
        symbol = checkSwapSymbol(symbol);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instId", symbol);
        ResponseEntity<String> stringResponseEntity = okxRestClient.getNoAuth("/api/v5/public/funding-rate", queryParams);
        JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
        if (response.getInteger("code") == 0) {
            JSONArray data = response.getJSONArray("data");
            for (Object datum : data) {
                JSONObject fundingItem = JSONObject.from(datum);

                String instId = fundingItem.getString("instId");
                if (instId.equalsIgnoreCase(symbol)) {
                    SymbolFundingRate symbolFundingRate = new SymbolFundingRate();
                    Long nextFundingTime = fundingItem.getLong("fundingTime");
                    BigDecimal fundingRate = fundingItem.getBigDecimal("fundingRate");
                    symbolFundingRate.setNextFundingTime(nextFundingTime);
                    symbolFundingRate.setFundingRate(fundingRate);
                    return symbolFundingRate;
                }
            }
        }
        return null;
    }

    @NotNull
    private static String checkSwapSymbol(String symbol) {
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/", "");
        }
        if(symbol.endsWith("-USDT-SWAP")){
            return symbol;
        }
        symbol = symbol + "-USDT-SWAP";
        return symbol;
    }

    @NotNull
    private static String deSwapSymbol(String symbol) {
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/USDT", "");
        }
        if (symbol.endsWith("-USDT")) {
            symbol = symbol.replace("-USDT", "");
        }
        symbol = symbol.replaceAll("-USDT-SWAP", "");
        return symbol;
    }

    @Override
    public AccountBalance getBalance(Account account, String coin) {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("ccy", coin);
        ResponseEntity<String> stringResponseEntity = okxRestClient.get("/api/v5/account/balance", queryParams, account);
        JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
        if (response.getInteger("code") == 0) {
            JSONArray data = response.getJSONArray("data");
            for (Object accitem : data) {
                JSONObject accFrom = JSONObject.from(accitem);
                JSONArray ccyItems = accFrom.getJSONArray("details");
                if (ccyItems.isEmpty()) {
                    //没有余额
                    AccountBalance accountBalance = new AccountBalance();
                    accountBalance.setSymbol(coin);
                    accountBalance.setBalance(BigDecimal.ZERO);
                    accountBalance.setEquity(BigDecimal.ZERO);
                    accountBalance.setFreeBalance(BigDecimal.ZERO);
                    accountBalance.setCashBalance(BigDecimal.ZERO);

                    accountBalance.setApiId(account.getId());
                    accountBalance.setApiName(account.getName());
                    return accountBalance;
                }

                for (Object ccyItem : ccyItems) {
                    JSONObject ccyFrom = JSONObject.from(ccyItem);
                    if (ccyFrom.getString("ccy").equalsIgnoreCase(coin)) {
                        AccountBalance accountBalance = new AccountBalance();
                        accountBalance.setSymbol(coin);
                        accountBalance.setBalance(ccyFrom.getBigDecimal("cashBal"));
                        accountBalance.setEquity(ccyFrom.getBigDecimal("eq"));
//                        accountBalance.setEquity(ccyFrom.getBigDecimal("availEq"));
                        accountBalance.setFreeBalance(ccyFrom.getBigDecimal("availBal"));
                        accountBalance.setCashBalance(ccyFrom.getBigDecimal("cashBal"));

                        accountBalance.setApiId(account.getId());
                        accountBalance.setApiName(account.getName());
                        return accountBalance;
                    }
                }

            }
        }

        return null;
    }

    @Override
    @Cacheable(value = "okxFeeInfo:cache#5s#60s#1000", key = "'okx'+':'+'contract:feeInfo'+':'+#coin", condition = "#coin != null ")
    public SymbolFee getFee(Account account, String coin) {
        CompletableFuture<JSONObject> sportFuture = CompletableFuture.supplyAsync(() -> {
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("instType", "SPOT");
            queryParams.put("instId", coin + "-USDT");
//        queryParams.put("ruleType", "normal");
            ResponseEntity<String> stringResponseEntity = okxRestClient.get("/api/v5/account/trade-fee", queryParams, account);
            JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
            Integer code = response.getInteger("code");
            if (Objects.nonNull(code) && code == 0) {
                JSONArray data = response.getJSONArray("data");
                if (!data.isEmpty()) {
                    return JSONObject.from(data.get(0));
                }
            }
            return null;
        });

        CompletableFuture<JSONObject> swapFuture = CompletableFuture.supplyAsync(() -> {
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("instType", "SWAP");
//        queryParams.put("instId", coin + "-USDT");
//        queryParams.put("ruleType", "normal");
            ResponseEntity<String> stringResponseEntity = okxRestClient.get("/api/v5/account/trade-fee", queryParams, account);
            JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
            Integer code = response.getInteger("code");
            if (Objects.nonNull(code) && code == 0) {
                JSONArray data = response.getJSONArray("data");
                if (!data.isEmpty()) {
                    return JSONObject.from(data.get(0));
                }
            }
            return null;
        });

        JSONObject spotFee = sportFuture.join();
        JSONObject swapFee = swapFuture.join();
        SymbolFee symbolFee = new SymbolFee();
        if (Objects.nonNull(swapFee)) {
            symbolFee.setLinerMakerFeeRate(swapFee.getBigDecimal("maker").negate());
            symbolFee.setLinerTakerFeeRate(swapFee.getBigDecimal("taker").negate());
        } else {
            symbolFee.setLinerMakerFeeRate(null);
            symbolFee.setLinerTakerFeeRate(null);
        }
        if (Objects.nonNull(spotFee)) {
            symbolFee.setSportMakerFeeRate(spotFee.getBigDecimal("maker").negate());
            symbolFee.setSportTakerFeeRate(spotFee.getBigDecimal("taker").negate());
        } else {
            symbolFee.setSportMakerFeeRate(null);
            symbolFee.setSportTakerFeeRate(null);
        }

        return symbolFee;
    }

    @Override
//    @Cacheable(value = "okxNowPrice:cache#1s#5s#1000", key = "'okx'+':'+'contract'+':'+#symbol", condition = "#symbol != null ")
    @AccountPaptrading
    public BigDecimal getNowPrice(Account account, String symbol) {
        symbol = checkSwapSymbol(symbol);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instId", symbol);
        ResponseEntity<String> noAuth = okxRestClient.getNoAuth("/api/v5/market/ticker", queryParams);
        JSONObject response = JSONObject.parseObject(noAuth.getBody());
        Integer code = response.getInteger("code");
        if (Objects.nonNull(code) && code == 0) {
            JSONArray data = response.getJSONArray("data");
            if (!data.isEmpty()) {
                for (Object datum : data) {
                    JSONObject datumFrom = JSONObject.from(datum);
                    BigDecimal last = datumFrom.getBigDecimal("last");
                    return last;
                }
            }
        }

        return null;
    }

    @Override
    public TickerItem getNowPrice(Account account, String symbol, String bitgetOnly) {
        BigDecimal nowPrice = this.getNowPrice(account, symbol);

        TickerItem tickerItem = new TickerItem();
        tickerItem.setSymbol(symbol);
        tickerItem.setChange24H(null);
        tickerItem.setHigh24H(null);
        tickerItem.setLow24H(null);
        tickerItem.setMarkPrice(nowPrice.toPlainString());
        return tickerItem;
    }

    @Override
    public OrderVo calcOrderSize(OrderVo longOrder) {
        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(null, longOrder.getSymbol());
        BigDecimal mult = contractCoinInfo.getContractValue().multiply(contractCoinInfo.getCtMult());

        //此时size 为对应币的数量，需要转换为张数
        ContractLotCalculator.OrderResult orderResult = ContractLotCalculator.calculateLots(longOrder.getSize(), contractCoinInfo.getContractValue(), contractCoinInfo.getOkxMinSz());
        if (orderResult.isValid()) {
            longOrder.setSize(orderResult.getLots());
            return longOrder;
        } else {
            throw new RuntimeException("不满足最小下单数量");
        }


    }

    @Override
    public BigDecimal calcOrderSize(String symbol, BigDecimal size) {
        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(null, symbol);
        BigDecimal mult = contractCoinInfo.getContractValue().multiply(contractCoinInfo.getCtMult());
        //此时size 为对应币的数量，需要转换为张数
        ContractLotCalculator.OrderResult orderResult = ContractLotCalculator.calculateLots(size, contractCoinInfo.getContractValue(), contractCoinInfo.getOkxMinSz());
        if (orderResult.isValid()) {
            return orderResult.getLots();
        } else {
            throw new RuntimeException("不满足最小下单数量");
        }
    }

    @Override
    public BigDecimal calcShowSize(String symbolOrigin, BigDecimal size) {

        String symbol = checkSwapSymbol(symbolOrigin);
        if (Objects.isNull(size)) {
            return BigDecimal.ZERO;
        }

        /**
         * okx 下单是张，有的币一张就是10，或者100，具体的币数量就是 ctmult * contractValue * 张数
         */
        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(null, symbol);
        if (Objects.isNull(contractCoinInfo)) {
            log.error("查询合约状态失败");
            return size;
        }
        BigDecimal mult = contractCoinInfo.getContractValue().multiply(contractCoinInfo.getCtMult());
        BigDecimal mutiSize = size.multiply(mult);
        return mutiSize;
    }

    @Override
    public OrderOptStatus queryContractOrderStatus(Account account, OrderOptStatus order) {
        String symbol = order.getSymbol();
        symbol = checkSwapSymbol(symbol);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instId", symbol);
        queryParams.put("ordId", order.getOrderId());
        ResponseEntity<String> stringResponseEntity = okxRestClient.get("/api/v5/trade/order", queryParams, account);
        JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
        if (response.getInteger("code") == 0) {
            JSONArray data = response.getJSONArray("data");
            if (!data.isEmpty()) {
                for (Object datum : data) {
                    JSONObject datumFrom = JSONObject.from(datum);
                    if (datumFrom.getString("ordId").equalsIgnoreCase(order.getOrderId())) {
                        //订单状态
                        /**
                         * 订单状态
                         * canceled：撤单成功
                         * live：等待成交
                         * partially_filled：部分成交
                         * filled：完全成交
                         * mmp_canceled：做市商保护机制导致的自动撤单
                         */
                        String state = datumFrom.getString("state");
                        if (state.equalsIgnoreCase("filled")) {
                            OrderOptStatus orderOptStatus = new OrderOptStatus();
                            orderOptStatus.setSymbol(order.getSymbol());
                            orderOptStatus.setOrderId(order.getOrderId());
                            orderOptStatus.setSide(orderOptStatus.getSide());
                            orderOptStatus.setFee(datumFrom.getBigDecimal("fee"));
                            orderOptStatus.setAvgPrice(datumFrom.getBigDecimal("avgPx"));
                            orderOptStatus.setStatus(CrossOrderStatus.END);
                            return orderOptStatus;
                        } else if (state.equalsIgnoreCase("canceled")) {
                            OrderOptStatus orderOptStatus = new OrderOptStatus();
                            orderOptStatus.setSymbol(order.getSymbol());
                            orderOptStatus.setOrderId(order.getOrderId());
                            orderOptStatus.setSide(orderOptStatus.getSide());
                            orderOptStatus.setStatus(CrossOrderStatus.END);
                            return orderOptStatus;
                        }
                    }
                }
            }
        }
        OrderOptStatus orderOptStatus = new OrderOptStatus();
        orderOptStatus.setSymbol(order.getSymbol());
        orderOptStatus.setOrderId(order.getOrderId());
        orderOptStatus.setSide(orderOptStatus.getSide());
        orderOptStatus.setStatus(CrossOrderStatus.PROCESS);
        return orderOptStatus;
    }

    @Override
    public List<ContractOrder> queryContractOrdersByIds(Account account, List<String> orderIds, String symbol) {
        symbol = checkSwapSymbol(symbol);

        ArrayList<ContractOrder> orderDatas = new ArrayList<>();
        HashSet<String> findOrders = new HashSet<>();
        //中间态订单状态
        List<String> okxProcessStatus = OrderStatusConstant.okxProcessStatus;
        List<String> okxEndStatus = OrderStatusConstant.okxEndStatus;
        for (String orderId : orderIds) {
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("instId", symbol);
            queryParams.put("ordId", orderId);
            ResponseEntity<String> stringResponseEntity = okxRestClient.get("/api/v5/trade/order", queryParams, account);
            JSONObject response = JSONObject.parseObject(stringResponseEntity.getBody());
            log.info("查询订单结果 {}", response);
            if (response.getInteger("code") == 0) {
                JSONArray data = response.getJSONArray("data");
                if (!data.isEmpty()) {
                    for (Object datum : data) {
                        JSONObject datumFrom = JSONObject.from(datum);

                        if (datumFrom.getString("ordId").equalsIgnoreCase(orderId)) {
                            //订单状态
                            /**
                             * 订单状态
                             * canceled：撤单成功
                             * live：等待成交
                             * partially_filled：部分成交
                             * filled：完全成交
                             * mmp_canceled：做市商保护机制导致的自动撤单
                             */
                            String state = datumFrom.getString("state");
                            ContractOrder contractOrder = new ContractOrder();
                            contractOrder.setEx(ExchangeType.OKX.getName());
                            contractOrder.setOrderId(orderId);
//                            String posSide = datumFrom.getString("posSide");
//                            if(StringUtils.isNotBlank(posSide)){
//                                contractOrder.setSide("long".equalsIgnoreCase(posSide) ? SideType.LONG : SideType.SHORT);
//                            }else{
//                                String orderSide = datumFrom.getString("side");
//                                if(StringUtils.isNotBlank(orderSide)){
//                                    contractOrder.setSide("buy".equalsIgnoreCase(orderSide) ? SideType.BUY : SideType.SELL);
//                                }
//                            }
                            contractOrder.setPrice(datumFrom.getBigDecimal("px"));
                            contractOrder.setSize(datumFrom.getBigDecimal("sz"));
                            contractOrder.setStatus(datumFrom.getString("state").toLowerCase());
                            //累计成交数量
                            contractOrder.setCumExecQty(datumFrom.getBigDecimal("accFillSz"));
                            contractOrder.setAvgPrice(datumFrom.getBigDecimal("avgPx"));
                            contractOrder.setFee(datumFrom.getBigDecimal("fee"));


                            if (StringListUtil.containsIgnoreCase(okxProcessStatus, state)) {
                                //中间态
                                contractOrder.setOrderEnd(false);
                            } else if (StringListUtil.containsIgnoreCase(okxEndStatus, state)) {
                                //结束态
                                contractOrder.setOrderEnd(true);
                                if (Objects.nonNull(contractOrder.getAvgPrice())) {
                                    contractOrder.setCumExecValue(contractOrder.getSize().multiply(contractOrder.getAvgPrice()));
                                }

                            }
                            orderDatas.add(contractOrder);
                        }
                    }
                }
            }
        }

        return orderDatas;
    }

    @Override
    public OrderVo updateContractOrder(Account account, OrderVo vo) {
        String symbol = checkSwapSymbol(vo.getSymbol());
        JSONObject order = new JSONObject();
        order.put("instId", symbol);
        order.put("ordId", vo.getOrderId());
        if (Objects.nonNull(vo.getPrice())) {
            //新价格
            order.put("newPx", vo.getPrice());
        }

        if (Objects.nonNull(vo.getSize())) {
            //新数量
            order.put("newSz", vo.getSize());
        }

        ResponseEntity<String> responseEntity = okxRestClient.post("/api/v5/trade/amend-order", order.toJSONString(), account);
        JSONObject response = JSONObject.parseObject(responseEntity.getBody());
        log.info("修改订单结果 {}", response);
        if (response.getInteger("code") == 0) {
            log.info("修改成功");
            return vo;
        } else {
            log.info("修改失败 {}", response.getString("msg"));
            throw new RuntimeException("修改失败 " + response.getString("msg"));
        }
    }

    @Override
    public void preCheckOrder(OrderVo longOrder) {

    }

    @Override
    public List<LinerSymbol> getAllLinerSymbol() {
        ResponseEntity<String> clientNoAuth = okxRestClient.getNoAuth("/api/v5/public/instruments", Map.of("instType", "SWAP"));
        JSONObject response = JSONObject.parseObject(clientNoAuth.getBody());
        List<LinerSymbol> all = new ArrayList<>();
        if (response.getInteger("code") == 0) {
            List<SwapSymbol> swapSymbols = response.getJSONArray("data").toJavaList(SwapSymbol.class);
            for (SwapSymbol swapSymbol : swapSymbols) {
                if (swapSymbol.getInstId().endsWith("-USDT-SWAP")) {
                    LinerSymbol linerSymbol = new LinerSymbol();
                    linerSymbol.setCoin(swapSymbol.getInstId().replace("-USDT-SWAP", ""));
                    linerSymbol.setSymbol(swapSymbol.getInstId());
                    all.add(linerSymbol);
                }
            }
            return all;
        }

        return List.of();
    }

    @Override
    @Cacheable(value = "okxSymbolFundingRateTime:cache#10s#60s#1000", key = "'okx'+':'+'contract'+':'+#symbol", condition = "#symbol != null ")
    public Integer getLinerSymbolFundingRateInterval(String souceSymbol) {
        String symbol = checkSwapSymbol(souceSymbol);
        ResponseEntity<String> noAuth = okxRestClient.getNoAuth("/api/v5/public/funding-rate-history", Map.of("instId", symbol, "limit", "5"));
        JSONObject response = JSONObject.parseObject(noAuth.getBody());
        if (response.getInteger("code") == 0) {
            List<FundHistoryItem> datas = response.getJSONArray("data").toList(FundHistoryItem.class);
            if (datas.size() >= 2) {
                FundHistoryItem old = datas.get(0);
                FundHistoryItem newer = datas.get(1);

                // 计算时间戳差值（毫秒）
                long diffMillis = Math.abs(old.getFundingTime() - newer.getFundingTime());

                // 将毫秒转换为小时
                long diffHours = diffMillis / (1000 * 60 * 60);  // 1000ms * 60s * 60m
                return (int) diffHours;
            }
        }

        return 0;
    }

    @Override
    public ContractOrder formateOrderBySyncOrderInfo(OrderOptStatus orderStatus, Account account, SyncOrderDetail syncOrderDetail) {
        return null;
    }

    @Override
    public List<PositionWsData> queryContractPositionDetail(Account account, PositionParams params) {
        OkxExchangeRestServiceImpl bean = SpringUtils.getBean(this.getClass());

        List<PositionWsData> wsDatas = new ArrayList<>();
        HashMap<String, String> queryParams = new HashMap<>();
        ResponseEntity<String> response = okxRestClient.get("/api/v5/account/positions", queryParams, account);
        log.info("okx查询持仓: {}", response.getBody());
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject.getInteger("code") == 0) {
            JSONArray dataList = jsonObject.getJSONArray("data");
            for (Object o : dataList) {
                JSONObject position = JSONObject.from(o);
                PositionWsData orderPosition = new PositionWsData();
//                    orderPosition.set(position.getString("posId"));
                orderPosition.setSize(bean.calcShowSize(position.getString("instId"), position.getBigDecimal("pos").abs()));
//                    orderPosition.setMarketPrice(position.getBigDecimal("markPx"));
                orderPosition.setFee(position.getBigDecimal("fee"));
                orderPosition.setLeverage(position.getBigDecimal("lever"));
                orderPosition.setExchange(ExchangeType.OKX.getName());
                orderPosition.setAccountId(account.getId());
                orderPosition.setAccountName(account.getName());
                orderPosition.setSymbol(deSwapSymbol(position.getString("instId")));
                orderPosition.setFundingFee(position.getBigDecimal("fundingFee"));
                orderPosition.setMarginRatio(position.getBigDecimal("mgnRatio"));
                orderPosition.setMarginType(position.getString("mgnMode"));
                orderPosition.setUnrealizedProfit(position.getBigDecimal("upl"));
                orderPosition.setAchievedProfits(position.getBigDecimal("realizedPnl"));
                orderPosition.setProfit(position.getBigDecimal("realizedPnl"));
                orderPosition.setAvgPrice(position.getBigDecimal("avgPx"));
                orderPosition.setLiqPrice(position.getBigDecimal("liqPx"));
                orderPosition.setMarginPrice(position.getBigDecimal("mmr"));
                BigDecimal totalFee = BigDecimal.ZERO;
                if(Objects.nonNull(orderPosition.getFee())){
                    totalFee = totalFee.add(orderPosition.getFee());
                }
                if(Objects.nonNull(orderPosition.getFundingFee())){
                    totalFee = totalFee.add(orderPosition.getFundingFee());
                }
                orderPosition.setTotalFee(totalFee);
                /**
                 * 持仓方向
                 * long：开平仓模式开多，pos为正
                 * short：开平仓模式开空，pos为正
                 * net：买卖模式（交割/永续/期权：pos为正代表开多，pos为负代表开空。币币杠杆时，pos均为正，posCcy为交易货币时，代表开多；posCcy为计价货币时，代表开空。）
                 */
                orderPosition.setSide(position.getString("posSide"));
                orderPosition.setHoldSide(orderPosition.getSide());
                orderPosition.setAccountId(account.getId());
                orderPosition.setUpdateTime(new Date());
                orderPosition.setCreateTime(new Date(position.getLongValue("cTime")));

                List<OkxOrderAlgo> closeOrderAlgo = position.getList("closeOrderAlgo", OkxOrderAlgo.class);
                if(CollectionUtils.isEmpty(closeOrderAlgo)){
                    //单独查止盈止损订单
                    List<? extends TpSlOrder> tpSlOrders = this.queryContractTpSlOrder(account, orderPosition.getSymbol());
                    if(CollectionUtils.isNotEmpty(tpSlOrders)){
                        for (TpSlOrder tpSlOrder : tpSlOrders) {
                            if (tpSlOrder instanceof OkxTpSlOrderItem okxTpSlOrderItem) {
                                if(orderPosition.getSymbol().equalsIgnoreCase(okxTpSlOrderItem.getSymbol())){
                                    orderPosition.setTakeProfit(okxTpSlOrderItem.getTpTriggerPx());
                                    orderPosition.setStopLoss(okxTpSlOrderItem.getSlTriggerPx());
                                }
                            }
                        }
                    }
                }else{
                    orderPosition = processTpSl(orderPosition, position.getList("closeOrderAlgo", OkxOrderAlgo.class));
                }

                wsDatas.add(orderPosition);
            }
        }

        List<String> hasPositionSymbols = wsDatas.stream().map(PositionWsData::getSymbol).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<CompletableFuture<? extends List<? extends TpSlOrder>>> futures = new ArrayList<>();
        for (String symbol : hasPositionSymbols) {
            CompletableFuture<? extends List<? extends TpSlOrder>> future = CompletableFuture.supplyAsync(() -> {
                return bean.queryContractTpSlOrder(account, symbol);
            }).exceptionally((e) -> {
                e.printStackTrace();
                log.warn("获取仓位止盈止损异常:{}", e.getMessage());
                return List.of();
            });
            futures.add(future);
        }
        //等待执行完毕
        // 等待所有完成
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 等待完成后统一收集结果
        allDone.join(); // 阻塞直到所有任务执行完毕

        // 收集所有查询结果
        List<TpSlOrder> result = futures.stream()
            .map(CompletableFuture::join) // 每个 future 的 List<TpSlOrder>
            .flatMap(List::stream)        // 展开成单个 TpSlOrder
            .collect(Collectors.toList());

        Map<String, List<OkxTpSlOrderItem>> tpslOrders = result.stream().map(item -> {
                if (item instanceof OkxTpSlOrderItem item1) {
                    return item1;
                } else {
                    return null;
                }
            }).filter(Objects::nonNull)
            .collect(Collectors.groupingBy(item -> deSwapSymbol(item.getInstId())));
        for (PositionWsData wsData : wsDatas) {
            String symbol = wsData.getSymbol();
            if (tpslOrders.containsKey(symbol)) {
                List<OkxTpSlOrderItem> okxTpSlOrderItems = tpslOrders.get(symbol);
                List<OkxTpSlOrderItemVo> converted = MapstructUtils.convert(okxTpSlOrderItems, OkxTpSlOrderItemVo.class);
                wsData.setTpSlOrders(converted);
            }
        }

        return wsDatas;
    }


    private PositionWsData processTpSl(PositionWsData okxPositionItem,List<OkxOrderAlgo> okxOrderAlgos) {
        if (CollectionUtils.isNotEmpty(okxOrderAlgos)) {
            for (OkxOrderAlgo orderAlgo : okxOrderAlgos) {
                if(Objects.nonNull(orderAlgo.getSlTriggerPx())){
                    okxPositionItem.setStopLoss(orderAlgo.getSlTriggerPx().stripTrailingZeros());
                }
                if(Objects.nonNull(orderAlgo.getTpTriggerPx())){
                    okxPositionItem.setTakeProfit(orderAlgo.getTpTriggerPx().stripTrailingZeros());
                }
            }
        }
        return okxPositionItem;

    }

    @Override
    public List<? extends TpSlOrder> queryContractTpSlOrder(Account account, String symbolOrigin) {
        String symbol = checkSwapSymbol(symbolOrigin);
        HashMap<String,String> queryParams =  new HashMap<String,String>();
        queryParams.put("instType", "SWAP");
        queryParams.put("instId", symbol);
        queryParams.put("ordType", "oco");
        queryParams.put("limit", "50");
        ResponseEntity<String> responseEntity = okxRestClient.get("/api/v5/trade/orders-algo-pending", queryParams, account);
        log.info("okx止盈止损单列表: {}", responseEntity.getBody());
        JSONObject response = JSONObject.parseObject(responseEntity.getBody());
//        log.info("修改止盈止损单 {}", response);
        if (response.getInteger("code") == 0) {
            JSONArray dataList = response.getJSONArray("data");
            List<OkxTpSlOrderItem> list = dataList.toJavaList(OkxTpSlOrderItem.class);
            for (OkxTpSlOrderItem okxTpSlOrderItem : list) {
                okxTpSlOrderItem.setSymbol(symbolOrigin);
                okxTpSlOrderItem.setTakeProfitPrice(okxTpSlOrderItem.getTpTriggerPx());
                okxTpSlOrderItem.setStopLossPrice(okxTpSlOrderItem.getSlTriggerPx());
            }
            return list;
        }else{
            return List.of();
        }

    }

    @Override
    public void updateContractTpSl(Account account, String symbolOrigin, TpSlOrder tpSlOrder) {
        if (tpSlOrder instanceof UpdateTpSlOkx updateTpSl) {

            String symbol = checkSwapSymbol(symbolOrigin);

            JSONObject queryParams = new JSONObject();
//        queryParams.put("instType", "SWAP");
            queryParams.put("instId", symbol);
            queryParams.put("tdMode", "cross");
            queryParams.put("algoId", updateTpSl.getAlgoId());
//        queryParams.put("tdMode", "cross");
//        queryParams.put("side", "sell");
//        queryParams.put("posSide", "long");
//        queryParams.put("ordType", "oco");
//        queryParams.put("sz", "0.31");
            queryParams.put("closeFraction", "1");
            queryParams.put("newTpTriggerPx", updateTpSl.getNewTpTriggerPx()); //止盈触发价格（直接在主参数中）
            queryParams.put("newTpOrdPx", "-1"); //止盈委托价格 -1市价
            queryParams.put("newSlTriggerPx", updateTpSl.getNewSlTriggerPx()); //止盈触发价格（直接在主参数中）
            queryParams.put("newSlOrdPx", "-1"); //止损委托价格-1市价
            queryParams.put("cxlOnClosePos", "true"); //决定用户所下的止盈止损订单是否与该交易产品对应的仓位关联。若关联，仓位被全平时，该止盈止损订单会被同时撤销；若不关联，仓位被撤销时，该止盈止损订单不受影响

            ResponseEntity<String> responseEntity = okxRestClient.post("/api/v5/trade/amend-algos", queryParams.toJSONString(), account);
            log.info("修改okx止盈止损单: {}", responseEntity.getBody());
            JSONObject response = JSONObject.parseObject(responseEntity.getBody());
//        log.info("修改止盈止损单 {}", response);
            if (response.getInteger("code") == 0) {
                log.info("okx止盈止损修改成功：{}", response.getJSONArray("data").toJSONString());
            } else {
                log.info("okx止盈止损修改失败 {}", response.getString("msg"));
                throw new RuntimeException("okx止盈止损修改失败 " + response.getString("msg"));
            }
        }
    }

    @Override
    public void createTpSl(Account account, String symbolOrigin, CreateTpSlOnce createTpSlOnce) {
        String symbol = checkSwapSymbol(symbolOrigin);

        JSONObject queryParams = new JSONObject();
//        queryParams.put("instType", "SWAP");
        queryParams.put("instId", symbol);
        queryParams.put("tdMode", "cross");
        if (SideType.LONG.equalsIgnoreCase(createTpSlOnce.getSide())) {
            queryParams.put("side", "sell");
        }else{
            queryParams.put("side", "buy");
        }
        if (SideType.LONG.equalsIgnoreCase(createTpSlOnce.getSide())) {
            queryParams.put("posSide", "long");
        }else{
            queryParams.put("posSide", "short");
        }
        queryParams.put("ordType", "oco");
//        queryParams.put("sz", "0.31");
        queryParams.put("closeFraction", "1");
        queryParams.put("tpTriggerPx", createTpSlOnce.getStopSurplusTriggerPrice()); //止盈触发价格（直接在主参数中）
        queryParams.put("tpOrdPx", "-1"); //止盈委托价格 -1市价
        queryParams.put("slTriggerPx", createTpSlOnce.getStopLossTriggerPrice()); //止盈触发价格（直接在主参数中）
        queryParams.put("slOrdPx", "-1"); //止损委托价格-1市价
        queryParams.put("cxlOnClosePos", "true"); //决定用户所下的止盈止损订单是否与该交易产品对应的仓位关联。若关联，仓位被全平时，该止盈止损订单会被同时撤销；若不关联，仓位被撤销时，该止盈止损订单不受影响

        ResponseEntity<String> responseEntity = okxRestClient.post("/api/v5/trade/order-algo", queryParams.toJSONString(), account);
        log.info("okx止盈止损单: {}", responseEntity.getBody());
        JSONObject response = JSONObject.parseObject(responseEntity.getBody());
//        log.info("修改止盈止损单 {}", response);
        if (response.getInteger("code") == 0) {
            log.info("okx止盈止损下单成功：{}", response.getJSONObject("data").toJSONString());
        } else {
            log.info("okx止盈止损下单失败 {}", response.getString("msg"));
            throw new RuntimeException("okx止盈止损下单失败 " + response.getString("msg"));
        }
    }

    /**
     * 时间粒度，默认值1m
     * 如 [1m/3m/5m/15m/30m/1H/2H/4H]
     * 6H/12H/1D/2D/3D/1W/1M/3M
     * @param account
     * @param symbolOrigin
     * @param granularity
     * @param limit
     * @return
     */
    @Override
    public JSONArray getMarketCandles(Account account, String symbolOrigin, String granularity,Long limit) {
        String symbol = checkSwapSymbol(symbolOrigin);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instId", symbol);
        queryParams.put("bar", granularity);
        queryParams.put("limit", limit+"");
        ResponseEntity<String> response = okxRestClient.get("/api/v5/market/candles", queryParams, account);
        log.info("okx查询K线数据: {}", response.getStatusCode());
        JSONObject responseBody = JSONObject.parseObject(response.getBody());
        if (responseBody.getInteger("code") == 0) {
            JSONArray dataList = responseBody.getJSONArray("data");
            /**
             * String time = (String) item.get(0);
             *             String openPrice = (String) item.get(1);
             *             String highPrice = (String) item.get(2);
             *             String lowPrice = (String) item.get(3);
             *             String closePrice = (String) item.get(4);
             *             String volume = (String) item.get(5); // index[5]	String	交易币成交量
             */
            JSONArray result = new JSONArray();
            for (Object item : dataList) {
                JSONArray itemArray = JSONArray.from(item);
                JSONArray formatedItem = new JSONArray();
                formatedItem.add(itemArray.get(0)); //time
                formatedItem.add(itemArray.get(1));//openPrice
                formatedItem.add(itemArray.get(2));//hightPrice
                formatedItem.add(itemArray.get(3));//lowPrice
                formatedItem.add(itemArray.get(4));//closePrice
                formatedItem.add(itemArray.get(6));//volume 交易币成交量
                result.add(formatedItem);
            }
            return result;

        }else{
            log.error("okx 报错:{}",responseBody.getString("msg"));
            throw new RuntimeException("请求OKX 报错:"+responseBody.toJSONString());
        }
    }

    @Override
    public BigDecimal getOpenInterest(Account account, String symbolOrigin) {
//        /api/v5/public/open-interest
        String symbol = checkSwapSymbol(symbolOrigin);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instType", "SWAP");
        queryParams.put("instId", symbol);
        ResponseEntity<String> response = okxRestClient.get("/api/v5/public/open-interest", queryParams, account);
        log.info("okx查询K线数据: {}", response.getStatusCode());
        JSONObject responseBody = JSONObject.parseObject(response.getBody());
        if (responseBody.getInteger("code") == 0) {
            /**
             * {"code":"0","data":[{"instId":"BTC-USDT-SWAP","instType":"SWAP","oi":"96399695.809999716","oiCcy":"963996.95809999716","oiUsd":"88957542893.771927925084","ts":"1764906116945"}],"msg":""}
             */
            JSONArray dataList = responseBody.getJSONArray("data");
            if(Objects.nonNull(dataList) && dataList.size()>0){
                Object o = dataList.get(0);
                JSONObject item = JSONObject.from(o);
                return item.getBigDecimal("oiCcy");
            }


        }
        return null;
    }

    @Override
    public List<HistoryPosition> queryContractHistoryPosition(Account account, Long size, HistoryPositionQuery query) {

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instType", "SWAP");
        queryParams.put("limit", size+"");
        if (Objects.nonNull(query)) {
            if(Objects.nonNull(query.getIdLessThan())){
                queryParams.put("after", query.getIdLessThan());
            }else{
                if(Objects.nonNull(query.getStartTime())){
                    queryParams.put("after", query.getStartTime().getTime()+"");
                }
            }


            if(Objects.nonNull(query.getEndTime())){
                queryParams.put("before", query.getEndTime().getTime()+"");
            }
            if(StringUtils.isNotEmpty(query.getSymbol())){
                String symbol = checkSwapSymbol(query.getSymbol());
                queryParams.put("instId", symbol);
            }
        }

        ResponseEntity<String> response = okxRestClient.get("/api/v5/account/positions-history", queryParams, account);
        log.info("okx查询历史持仓: {}", response.getBody());
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if (jsonObject.getInteger("code") == 0) {
            JSONArray dataList = jsonObject.getJSONArray("data");
            List<HistoryPosition> items = new ArrayList<>();
            for (Object o : dataList) {
                //https://www.okx.com/docs-v5/zh/#trading-account-rest-api-get-positions-history
                JSONObject from = JSONObject.from(o);
                HistoryPosition historyPosition = new HistoryPosition();
                historyPosition.setSymbol(from.getString("instId"));

                historyPosition.setMarginMode(from.getString("mgnMode"));
                historyPosition.setCtime(from.getString("cTime"));
                historyPosition.setUtime(from.getString("uTime"));
                historyPosition.setOpenAvgPrice(from.getBigDecimal("openAvgPx"));
                historyPosition.setCloseAvgPrice(from.getBigDecimal("closeAvgPx"));
                historyPosition.setPositionId(from.getString("posId"));
                historyPosition.setCloseTotalPos(from.getBigDecimal("closeTotalPos"));
                historyPosition.setNetProfit(from.getBigDecimal("realizedPnl"));
                historyPosition.setTotalFunding(from.getBigDecimal("fundingFee"));
                historyPosition.setHoldSide(from.getString("direction"));
                historyPosition.setPosSide(from.getString("posSide"));
                historyPosition.setLeverage(from.getString("lever"));
                historyPosition.setMarginCoin(from.getString("ccy"));
                historyPosition.setPnl(from.getBigDecimal("pnl"));

                BigDecimal fee = from.getBigDecimal("fee");
                if (Objects.nonNull(fee)) {
                    historyPosition.setOpenFee(fee.divide(BigDecimal.valueOf(2), 8, BigDecimal.ROUND_HALF_UP));
                    historyPosition.setCloseFee(fee.divide(BigDecimal.valueOf(2), 8, BigDecimal.ROUND_HALF_UP));
                }else{
                    historyPosition.setOpenFee(BigDecimal.ZERO);
                    historyPosition.setCloseFee(BigDecimal.ZERO);
                }

                historyPosition.setIdLessThan(from.getString("uTime"));

                items.add(historyPosition);
            }
            return items;
        }
        throw new RuntimeException("okx 获取历史仓位出错:"+(jsonObject.getString("msg")));
    }

    @Override
    public String getPositionId(HistoryPosition historyPosition) {
        //16541771744191654177174419BTC
        String ctime = historyPosition.getCtime();
        String symbol = historyPosition.getSymbol();
        String utime = historyPosition.getUtime();
        return ctime + utime + symbol;
    }

}
