package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.Event.AckOrderPlaceErrorEvent;
import com.bitstrat.domain.bo.CoinsCrossExchangeArbitrageTaskBo;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.domain.vo.*;
import com.bitstrat.service.ExOrderService;
import com.bitstrat.service.ICoinsAbOrderLogService;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.BigDecimalUtils;
import com.bitstrat.utils.PriceCalculator;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/9 17:55
 * @Content
 */

@Service
@Slf4j
public class ExOrderServiceImpl implements ExOrderService {
    @Autowired
    private ExchangeApiManager exchangeApiManager;
    @Autowired
    private ICoinsApiService coinsApiService;

    @Autowired
    private ICoinsAbOrderLogService abOrderLogService;

    @Autowired
    private ICoinsOrderService coinsOrderService;
    @Autowired
    private ExecuteService executeService;

    @Override
    public void oncePlace2ExOrder(ABOrderFrom from) {
        //获取api
        CoinsApiVo longExApi = coinsApiService.queryById(from.getBuy().getAccountId());
        CoinsApiVo shortExApi = coinsApiService.queryById(from.getSell().getAccountId());
        if (Objects.isNull(longExApi) || Objects.isNull(shortExApi)) {

            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单失败！没有配置好API");
            throw new RuntimeException("没有配置好API");
        }
        //查找两个交易所

        ExchangeService longExService = exchangeApiManager.getExchangeService(from.getBuy().getExchange());
        ExchangeService shortExService = exchangeApiManager.getExchangeService(from.getSell().getExchange());
        Account accountLong = AccountUtils.coverToAccount(longExApi);
        Account accountShort = AccountUtils.coverToAccount(shortExApi);
        //设置杠杆
        try {
            longExService.setLeverage(accountLong, Math.toIntExact(from.getBuy().getLeverage()), from.getBuy().getSymbol(), CrossContractSide.LONG);
            shortExService.setLeverage(accountShort, Math.toIntExact(from.getSell().getLeverage()), from.getSell().getSymbol(), CrossContractSide.SHORT);
        } catch (Exception e) {
            e.printStackTrace();
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "设置杠杆失败！" + e.getMessage());
            throw new RuntimeException("设置杠杆失败");
        }


        OrderVo longOrderPre = new OrderVo();
        longOrderPre.setAccount(accountLong);
        if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            longOrderPre.setOrderType(OrderType.MARKET);
        } else {
            longOrderPre.setOrderType(OrderType.LIMIT);
        }
        longOrderPre.setPrice(null);
        longOrderPre.setSize(from.getBuy().getSize());
        longOrderPre.setSymbol(from.getBuy().getSymbol());
        longOrderPre.setLeverage(BigDecimal.valueOf(from.getBuy().getLeverage()));
        OrderVo longOrder = longExService.calcOrderSize(longOrderPre);

        OrderVo shortOrderPre = new OrderVo();
        shortOrderPre.setAccount(accountShort);
        if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            shortOrderPre.setOrderType(OrderType.MARKET);
        } else {
            shortOrderPre.setOrderType(OrderType.LIMIT);
        }
        shortOrderPre.setPrice(null);
        shortOrderPre.setSize(from.getSell().getSize());
        shortOrderPre.setSymbol(from.getSell().getSymbol());
        shortOrderPre.setLeverage(BigDecimal.valueOf(from.getSell().getLeverage()));
        OrderVo shortOrder = shortExService.calcOrderSize(shortOrderPre);
        try {
            longExService.preCheckOrder(longOrder);
            shortExService.preCheckOrder(shortOrder);
        } catch (Exception e) {
            e.printStackTrace();
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "订单检查异常:" + e.getMessage());
            throw e;
        }

        AtomicBoolean orderError = new AtomicBoolean(false);
        AtomicReference<String> orderErrorMsg = new AtomicReference<>();
        CompletableFuture<OrderOptStatus> longExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return longExService.buyContract(accountLong, longOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "多单下单异常:" + e.getMessage());
            AckOrderPlaceErrorEvent ackOrderPlaceErrorEvent = new AckOrderPlaceErrorEvent();
            ackOrderPlaceErrorEvent.setOrderTask(from.getAbOrderTask());
            SpringUtils.getApplicationContext().publishEvent(ackOrderPlaceErrorEvent);
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
            e.printStackTrace();
            return null;
        });
        ;
        CompletableFuture<OrderOptStatus> shortExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return shortExService.sellContract(accountShort, shortOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "空单下单异常:" + e.getMessage());
            AckOrderPlaceErrorEvent ackOrderPlaceErrorEvent = new AckOrderPlaceErrorEvent();
            ackOrderPlaceErrorEvent.setOrderTask(from.getAbOrderTask());
            SpringUtils.getApplicationContext().publishEvent(ackOrderPlaceErrorEvent);
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
            e.printStackTrace();
            return null;
        });

        OrderOptStatus longOrderStatus = null;
        OrderOptStatus shortOrderStatus = null;
        try {
            longOrderStatus = longExOrderFuture.join();

        } catch (Exception e) {
            e.printStackTrace();
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }
        try {
            shortOrderStatus = shortExOrderFuture.join();
        } catch (Exception e) {
            e.printStackTrace();
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }
        //保存订单
        if (Objects.nonNull(longOrderStatus) && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setAbTaskId(from.getAbTaskId());
            coinsOrderBo.setEx(from.getBuy().getExchange());
            coinsOrderBo.setOrderId(longOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(from.getBuy().getSymbol());
            coinsOrderBo.setSide(SideType.LONG);
            coinsOrderBo.setSize(longOrder.getSize().toPlainString());
            coinsOrderBo.setCreateBy(from.getUserId());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setAccountId(from.getBuy().getAccountId());

            if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            coinsOrderService.publishToWs(coinsOrderBo,from.getUserId());
            //检查是否是同步订单，有的交易所api 会同步返回订单状态，比如币安
            longOrderStatus.setAccount(accountLong);
            longOrderStatus.setUserId(from.getUserId());
            OrderOptStatus finalLongOrderStatus = longOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(from.getBuy().getSymbol(), finalLongOrderStatus,longExService);
            });
        }
        if (Objects.nonNull(shortOrderStatus) && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setAbTaskId(from.getAbTaskId());
            coinsOrderBo.setEx(from.getSell().getExchange());
            coinsOrderBo.setOrderId(shortOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(from.getSell().getSymbol());
            coinsOrderBo.setSide(SideType.SHORT);
            coinsOrderBo.setSize(shortOrder.getSize().toPlainString());
            coinsOrderBo.setCreateBy(from.getUserId());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setAccountId(from.getSell().getAccountId());

            if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            coinsOrderService.publishToWs(coinsOrderBo,from.getUserId());
            //检查是否是同步订单，有的交易所api 会同步返回订单状态，比如币安
            shortOrderStatus.setAccount(accountShort);
            shortOrderStatus.setUserId(from.getUserId());
            OrderOptStatus finalShortOrderStatus = shortOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(from.getSell().getSymbol(), finalShortOrderStatus,shortExService);
            });

        }

        if (longOrderStatus != null && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)
            && shortOrderStatus != null && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //已创建
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单成功");
            return;
        }
    }

    @Override
    public void oncePlace2CloseExOrder(ABCloseFrom from) {
        //获取api
        CoinsApiVo longExApi = coinsApiService.queryById(from.getBuy().getAccountId());
        CoinsApiVo shortExApi = coinsApiService.queryById(from.getSell().getAccountId());
        if (Objects.isNull(longExApi) || Objects.isNull(shortExApi)) {

            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单失败！没有配置好API");
            throw new RuntimeException("没有配置好API");
        }
        //查找两个交易所

        ExchangeService longExService = exchangeApiManager.getExchangeService(from.getBuy().getExchange());
        ExchangeService shortExService = exchangeApiManager.getExchangeService(from.getSell().getExchange());
        Account accountLong = AccountUtils.coverToAccount(longExApi);
        Account accountShort = AccountUtils.coverToAccount(shortExApi);
        //设置杠杆
        try {
            longExService.setLeverage(accountLong, Math.toIntExact(from.getBuy().getLeverage()), from.getBuy().getSymbol(), CrossContractSide.LONG);
            shortExService.setLeverage(accountShort, Math.toIntExact(from.getSell().getLeverage()), from.getSell().getSymbol(), CrossContractSide.SHORT);
        } catch (Exception e) {
            e.printStackTrace();
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "设置杠杆失败！" + e.getMessage());
            throw new RuntimeException("设置杠杆失败");
        }

        OrderVo longOrderPre = new OrderVo();
        longOrderPre.setAccount(accountLong);
        if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            longOrderPre.setOrderType(OrderType.MARKET);
        } else {
            longOrderPre.setOrderType(OrderType.LIMIT);
        }
        longOrderPre.setPrice(null);
        longOrderPre.setSize(from.getBuy().getSize());
        longOrderPre.setSymbol(from.getBuy().getSymbol());
        longOrderPre.setLeverage(BigDecimal.valueOf(from.getBuy().getLeverage()));
        longOrderPre.setReduceOnly(true);
        OrderVo longOrder = longExService.calcOrderSize(longOrderPre);

        OrderVo shortOrderPre = new OrderVo();
        shortOrderPre.setAccount(accountShort);
        if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            shortOrderPre.setOrderType(OrderType.MARKET);
        } else {
            shortOrderPre.setOrderType(OrderType.LIMIT);
        }
        shortOrderPre.setPrice(null);
        shortOrderPre.setSize(from.getSell().getSize());
        shortOrderPre.setSymbol(from.getSell().getSymbol());
        shortOrderPre.setLeverage(BigDecimal.valueOf(from.getSell().getLeverage()));
        shortOrderPre.setReduceOnly(true);
        OrderVo shortOrder = shortExService.calcOrderSize(shortOrderPre);
        try {
            longExService.preCheckOrder(longOrder);
            shortExService.preCheckOrder(shortOrder);
        } catch (Exception e) {
            e.printStackTrace();
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "订单检查异常:" + e.getMessage());
            throw e;
        }

        AtomicBoolean orderError = new AtomicBoolean(false);
        AtomicReference<String> orderErrorMsg = new AtomicReference<>();
        //平仓，做多的一边应该卖掉，做空的一边需要买回来
        CompletableFuture<OrderOptStatus> longExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return longExService.sellContract(accountLong, longOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
            return null;
        });
        CompletableFuture<OrderOptStatus> shortExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return shortExService.buyContract(accountShort, shortOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
            return null;
        });
        ;

        //监听订单状态，如果两方订单都成功，则成功，一方成功，另一方3秒后还没有成功，则撤销订单
        OrderOptStatus longOrderStatus = null;
        OrderOptStatus shortOrderStatus = null;

        try {
            longOrderStatus = longExOrderFuture.join();
        } catch (Exception e) {
            e.printStackTrace();
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }

        try {
            shortOrderStatus = shortExOrderFuture.join();
        } catch (Exception e) {
            e.printStackTrace();
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }
        //保存订单
        if (Objects.nonNull(longOrderStatus) && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setAbTaskId(from.getAbTaskId());
            coinsOrderBo.setEx(from.getBuy().getExchange().toLowerCase());
            coinsOrderBo.setOrderId(longOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(from.getBuy().getSymbol());
            coinsOrderBo.setSide(SideType.LONG);
            coinsOrderBo.setSize(longOrder.getSize().toPlainString());
            coinsOrderBo.setPrice(BigDecimalUtils.toPlainString(longOrder.getPrice()));
            coinsOrderBo.setCreateBy(from.getUserId());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setClosePositionOrder(CrossClosePositionFlag.CLOSE_POSITION_ORDER);
            coinsOrderBo.setAccountId(from.getBuy().getAccountId());

            if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            coinsOrderService.publishToWs(coinsOrderBo,from.getUserId());
            //检查是否是同步订单，有的交易所api 会同步返回订单状态，比如币安
            longOrderStatus.setAccount(accountShort);
            longOrderStatus.setUserId(from.getUserId());
            OrderOptStatus finalLongOrderStatus = longOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(from.getBuy().getSymbol(), finalLongOrderStatus,shortExService);
            });

        }
        if (Objects.nonNull(shortOrderStatus) && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setAbTaskId(from.getAbTaskId());
            coinsOrderBo.setEx(from.getSell().getExchange().toLowerCase());
            coinsOrderBo.setOrderId(shortOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(from.getSell().getSymbol());
            coinsOrderBo.setPrice(BigDecimalUtils.toPlainString(shortOrder.getPrice()));
            coinsOrderBo.setSide(SideType.SHORT);
            coinsOrderBo.setSize(shortOrder.getSize().toPlainString());
            coinsOrderBo.setCreateBy(from.getUserId());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setClosePositionOrder(CrossClosePositionFlag.CLOSE_POSITION_ORDER);
            coinsOrderBo.setAccountId(from.getSell().getAccountId());

            if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            coinsOrderService.publishToWs(coinsOrderBo,from.getUserId());
            shortOrderStatus.setAccount(accountShort);
            shortOrderStatus.setUserId(from.getUserId());
            OrderOptStatus finalShortOrderStatus = shortOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(from.getSell().getSymbol(), finalShortOrderStatus,shortExService);
            });
        }

        if (longOrderStatus != null && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)
            && shortOrderStatus != null && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //已创建
            abOrderLogService.sendAndSaveLog(from.getAbOrderTask(), "下单成功");
            return;
        }

    }

    private void checkSyncOrder(String symbol,OrderOptStatus orderStatus,ExchangeService exchangeService) {
        if (orderStatus.getSyncOrder()) {
            //同步返回订单，更新订单状态
            log.info("是同步返回订单，更新订单状态开始---");
            orderStatus.setSymbol(symbol);
            ContractOrder contractOrder = exchangeService.formateOrderBySyncOrderInfo(orderStatus, orderStatus.getAccount(), orderStatus.getSyncOrderDetail());
            if(Objects.nonNull(contractOrder)) {
                coinsOrderService.updateOrderByContractOrder(contractOrder);
                CoinsOrderVo coinsOrderVo = coinsOrderService.queryByOrderId(contractOrder.getOrderId());
                CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
                BeanUtils.copyProperties(coinsOrderVo, coinsOrderBo);
                coinsOrderService.publishToWs(coinsOrderBo,orderStatus.getUserId());
                log.info("是同步返回订单，更新订单状态完成---");
            }else{
                log.warn("是同步返回订单，更新订单状态失败，formate 订单为null");
            }
        }
    }

}
