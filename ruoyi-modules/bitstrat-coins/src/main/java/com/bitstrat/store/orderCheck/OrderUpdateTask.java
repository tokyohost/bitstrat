package com.bitstrat.store.orderCheck;

import com.bitstrat.constant.CrossOrderStatus;
import com.bitstrat.constant.LockConstant;
import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.service.ICoinsCrossTaskLogService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.service.ICoinsTaskLogService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.ThreadLocalLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/25 19:02
 * @Content
 */

@Slf4j
public class OrderUpdateTask implements Runnable {

    private final int maxRetry;
    private ExchangeApiManager exchangeApiManager;
    private CoinsOrder order;
    private Account account;
    private final long retryIntervalMillis;
    private int retryCount = 0;
    private ICoinsCrossTaskLogService coinsCrossTaskLogService;
    private ICoinsOrderService coinsOrderService;
    private RedissonClient redissonClient;

    public OrderUpdateTask(CoinsOrder order, Account account, int maxRetry, long retryIntervalMillis) {
        this.order = order;
        this.account = account;
        this.maxRetry = maxRetry;
        this.retryIntervalMillis = retryIntervalMillis;
        this.coinsCrossTaskLogService = SpringUtils.getBean(ICoinsCrossTaskLogService.class);
        this.exchangeApiManager = SpringUtils.getBean(ExchangeApiManager.class);
        this.coinsOrderService = SpringUtils.getBean(ICoinsOrderService.class);
        this.redissonClient = SpringUtils.getBean(RedissonClient.class);
    }

    @Override
    public void run() {
        try {
            ThreadLocalLogUtil.clear();
            if (!order.getOrderType().equalsIgnoreCase(OrderType.LIMIT)) {
                log.info("非限价单，不监控成交");
                return;
            }

            ExchangeService exchangeService = exchangeApiManager.getExchangeService(order.getEx());
            if (Objects.nonNull(exchangeService)) {
                OrderOptStatus orderOptStatus = new OrderOptStatus();
                orderOptStatus.setOrderId(order.getOrderId());
                orderOptStatus.setSymbol(order.getSymbol());
                OrderOptStatus contractOrders = exchangeService.queryContractOrderStatus(account, orderOptStatus);
//                List<ContractOrder> contractOrders = exchangeService.queryContractOrdersByIds(account, Arrays.asList(order.getOrderId()), order.getSymbol());
//                if(!contractOrders.isEmpty()){
                //查到订单了
//                    for (ContractOrder contractOrder : contractOrders) {
                if (contractOrders.getOrderId().equalsIgnoreCase(order.getOrderId())) {
                    //判断是否终结状态，如果不是，就修改订单
                    if (!contractOrders.getStatus().equalsIgnoreCase(CrossOrderStatus.END)) {
                        //改价
                        BigDecimal nowPrice = exchangeService.getNowPrice(account, order.getSymbol());
                        if (Objects.nonNull(nowPrice)) {
                            OrderVo orderVo = new OrderVo();
                            orderVo.setOrderId(order.getOrderId());
                            orderVo.setSymbol(order.getSymbol());
                            orderVo.setOrderType(order.getOrderType());
                            orderVo.setSide(order.getSide());
                            orderVo.setPrice(nowPrice);
                            orderVo.setOrderSize(new BigDecimal(order.getSize()));
                            orderVo.setAccount(account);
                            RLock lock = redissonClient.getLock(LockConstant.ORDER_LOCK + ":" + order.getCreateBy() + ":"+order.getId());
                            boolean b = lock.tryLock(15, TimeUnit.SECONDS);
                            try {
//                                if (b) {
                                    OrderVo updated = exchangeService.updateContractOrder(account, orderVo);
                                    if (updated.getOrderId().equalsIgnoreCase(order.getOrderId())) {
                                        coinsOrderService.updateDbPrice(order.getId(), nowPrice);
                                    } else {
                                        order.setOrderId(updated.getOrderId());
                                        coinsOrderService.updateDbPriceAndOrderId(order.getId(), nowPrice, updated.getOrderId());
                                    }

                                    coinsCrossTaskLogService.saveLog(order.getTaskId(), "检测到限价单未成交，改价为市价 " + nowPrice + "." + " orderid=" + order.getOrderId() + ".");

//                                }
                            } finally {
                                lock.unlock();
                            }
                        } else {
                            coinsCrossTaskLogService.saveLog(order.getTaskId(), "检测到限价单未成交，查询市价失败！" + order.getEx() + "." + " orderid=" + order.getOrderId() + ".");
                        }

                    } else {
                        coinsCrossTaskLogService.saveLog(order.getTaskId(), "检测到限价单订单已结束,停止监听" + " orderid=" + order.getOrderId() + ".");
                        return;
                    }
                }
//                    }

//                }else{
//                    log.error("未找到订单,检查订单状态失败，可能未创建成功，继续重试"+ " orderid="+order.getOrderId()+".");
//                    return;
//                }
            } else {
                log.error("未找到对应交易所实现,检查订单状态失败" + " orderid=" + order.getOrderId() + ".");
            }

            if (retryCount < maxRetry) {
                retryCount++;
                // 继续重试
                log.error("继续重试");
                coinsCrossTaskLogService.saveLog(order.getTaskId(), "继续监听订单 retry=" + retryCount + "." + " orderid=" + order.getOrderId() + ".");
                OrderCheckerManager.scheduleLater(this, retryIntervalMillis);
            }

        } catch (Exception e) {
            log.error("订单成交检查异常: " + e.getMessage(), e);
            coinsCrossTaskLogService.saveLog(order.getTaskId(), "订单成交检查异常: " + e.getMessage() + " orderid=" + order.getOrderId() + ".");
        } finally {
            ThreadLocalLogUtil.clear();
        }
    }
}
