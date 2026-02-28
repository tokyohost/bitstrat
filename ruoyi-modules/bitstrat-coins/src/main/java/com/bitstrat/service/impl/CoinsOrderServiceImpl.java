package com.bitstrat.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.mapper.CoinsOrderMapper;
import com.bitstrat.service.BatchOrderTaskService;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.BigDecimalUtils;
import com.bitstrat.utils.UpdateWrapperBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bitstrat.constant.LockConstant.UPDATE_CROSS_AB_ORDER_TASK_FEE_LOCK;

/**
 * 订单列表Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CoinsOrderServiceImpl implements ICoinsOrderService {
    @Autowired
    private ExecuteService executeService;


    private final CoinsOrderMapper baseMapper;
    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    @Lazy
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 查询订单列表
     *
     * @param id 主键
     * @return 订单列表
     */
    @Override
    public CoinsOrderVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询订单列表列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单列表分页列表
     */
    @Override
    public TableDataInfo<CoinsOrderVo> queryPageList(CoinsOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsOrder> lqw = buildQueryWrapper(bo);
        Page<CoinsOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的订单列表列表
     *
     * @param bo 查询条件
     * @return 订单列表列表
     */
    @Override
    public List<CoinsOrderVo> queryList(CoinsOrderBo bo) {
        LambdaQueryWrapper<CoinsOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsOrder> buildQueryWrapper(CoinsOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsOrder::getId);
        lqw.eq(bo.getTaskId() != null, CoinsOrder::getTaskId, bo.getTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderId()), CoinsOrder::getOrderId, bo.getOrderId());
        lqw.eq(StringUtils.isNotBlank(bo.getEx()), CoinsOrder::getEx, bo.getEx());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), CoinsOrder::getSymbol, bo.getSymbol());
        lqw.eq(StringUtils.isNotBlank(bo.getSize()), CoinsOrder::getSize, bo.getSize());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), CoinsOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getFee()), CoinsOrder::getFee, bo.getFee());
        lqw.eq(StringUtils.isNotBlank(bo.getAvgPrice()), CoinsOrder::getAvgPrice, bo.getAvgPrice());
        lqw.eq(StringUtils.isNotBlank(bo.getPrice()), CoinsOrder::getPrice, bo.getPrice());
        lqw.eq(bo.getCreateBy() != null, CoinsOrder::getCreateBy, bo.getCreateBy());
        return lqw;
    }

    /**
     * 新增订单列表
     *
     * @param bo 订单列表
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsOrderBo bo) {
        CoinsOrder add = MapstructUtils.convert(bo, CoinsOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改订单列表
     *
     * @param bo 订单列表
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsOrderBo bo) {
        CoinsOrder update = MapstructUtils.convert(bo, CoinsOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsOrder entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除订单列表信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 同步订单状态
     *
     * @param
     * @return
     */

    @SneakyThrows
    @Override
    public List<CoinsOrderVo> syncOrder(List<CoinsOrderVo> orders, Long userId, boolean async) {

        Map<String, List<CoinsOrderVo>> exchangeOrders = orders.stream().filter(item -> OrderEndStatus.NOT_END == item.getOrderEnd()).collect(Collectors.groupingBy(CoinsOrderVo::getEx));

        Set<Long> userIds = orders.stream().map(CoinsOrderVo::getCreateBy).collect(Collectors.toSet());
        HashMap<Long, Account> accountMap = new HashMap<>();
        //获取api
        for (String ex : exchangeOrders.keySet()) {
            Set<Long> allaccountId = exchangeOrders.get(ex).stream().map(CoinsOrderVo::getAccountId).collect(Collectors.toSet());
            List<CoinsApiVo> coinsApiVos = coinsApiService.queryByIds(new ArrayList<>(allaccountId));
            for (CoinsApiVo coinsApiVo : coinsApiVos) {
                Account account = AccountUtils.coverToAccount(coinsApiVo);
                accountMap.put(account.getId(), account);
            }
        }

        List<CompletableFuture<List<ContractOrder>>> futures = new ArrayList<>();
        for (String ex : exchangeOrders.keySet()) {
            List<CoinsOrderVo> orderVos = exchangeOrders.get(ex);
            Map<String, List<CoinsOrderVo>> symbolOrderMap = orderVos.stream().collect(Collectors.groupingBy(CoinsOrderVo::getSymbol));
            for (String symbol : symbolOrderMap.keySet()) {
                List<CoinsOrderVo> coinsOrderVos = symbolOrderMap.get(symbol);
                Map<Long, List<CoinsOrderVo>> accountOrders = coinsOrderVos.stream().collect(Collectors.groupingBy(CoinsOrderVo::getAccountId));
                for (Long accountId : accountOrders.keySet()) {
                    Account account = accountMap.get(accountId);
                    List<CoinsOrderVo> accountOrder = accountOrders.get(accountId);
                    //交易所中未到终结态的订单
                    Set<String> orderIds = accountOrder.stream().map(CoinsOrderVo::getOrderId).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(orderIds)) {
                        continue;
                    }
                    ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
                    if (Objects.nonNull(exchangeService)) {
                        CompletableFuture<List<ContractOrder>> orderFuture = CompletableFuture.supplyAsync(() -> {
                            return exchangeService.queryContractOrdersByIds(account, new ArrayList<>(orderIds), symbol);
                        }, exchangeApiManager.getOrderExecutor());
                        futures.add(orderFuture);
                    } else {
                        log.error("未找到交易所 处理实现 {}", ex);
                    }

                }
            }
        }


        // 等待所有完成
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 等待完成后统一收集结果
        List<ContractOrder> allOrders = allDoneFuture.thenApply(v ->
            futures.stream()
                .flatMap(f -> {
                    try {
                        return f.join().stream();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 可以记录日志
                        return Stream.<ContractOrder>empty();
                    }
                })  // join 每个 future，flatMap 合并成一个 List
                .collect(Collectors.toList())
        ).join();
        Map<String, ContractOrder> orderInfoMap = allOrders.stream().collect(Collectors.toMap(ContractOrder::getOrderId, item -> item));
        ArrayList<CoinsOrderVo> needUpdate = new ArrayList<>();
        HashMap<Long, CoinsCrossExchangeArbitrageTaskVo> taskPositionMap = new HashMap<>();
        HashSet<CoinsCrossExchangeArbitrageTaskVo> taskUpdate = new HashSet<>();
        AtomicBoolean hasBatchOrder = new AtomicBoolean(false);
        for (CoinsOrderVo order : orders) {
            if (orderInfoMap.containsKey(order.getOrderId())) {
                //查到了订单信息
                ContractOrder contractOrder = orderInfoMap.get(order.getOrderId());
                log.info("查询订单api 返回{}", JSONObject.toJSONString(contractOrder));
                order.setOrderEnd(contractOrder.getOrderEnd() ? OrderEndStatus.END : OrderEndStatus.NOT_END);
                order.setFee(BigDecimalUtils.toPlainString(contractOrder.getFee()));
                order.setAvgPrice(BigDecimalUtils.toPlainString(contractOrder.getAvgPrice()));
                order.setPrice(BigDecimalUtils.toPlainString(contractOrder.getPrice()));
//                order.setSide(contractOrder.getSide());
                order.setStatus(contractOrder.getStatus());
                order.setSize(BigDecimalUtils.toPlainString(contractOrder.getSize()));
                order.setCumExecQty(contractOrder.getCumExecQty());
                order.setCumExecValue(contractOrder.getCumExecValue());
                order.setLeavesQty(contractOrder.getLeavesQty());
                order.setLeavesValue(contractOrder.getLeavesValue());
                if (Objects.nonNull(contractOrder.getPnl())) {
                    order.setPnl(contractOrder.getPnl());
                }
                if (order.getTaskId() == null) {
                    continue;
                }

                //如果订单对应的仓位状态是等待成交，那么根据成交情况，更新仓位状态为运行中，方便定时任务去同步仓位
                CoinsCrossExchangeArbitrageTaskVo taskVo = taskPositionMap.computeIfAbsent(order.getTaskId(), (id) -> {
                    return coinsCrossExchangeArbitrageTaskService.queryById(id);
                });
                if (contractOrder.getOrderEnd() && taskVo.getStatus() == CrossTaskStatus.WAIT_ORDER_DEAL) {
                    //订单已经是最终状态了，不能根据这个判断是否要平仓！
                    CoinsCrossExchangeArbitrageTaskVo updateTask = new CoinsCrossExchangeArbitrageTaskVo();
                    updateTask.setId(order.getTaskId());
                    updateTask.setStatus(CrossTaskStatus.RUNNING);
                    taskUpdate.add(updateTask);
                }
                if (contractOrder.getOrderEnd() && order.getBatchId() != null) {
                    //批次订单
                    hasBatchOrder.set(true);
                }
                needUpdate.add(order);
            }
        }
        log.info("更新订单状态");
        SpringUtils.getBean(this.getClass()).updateVos(needUpdate);
        log.info("更新订单状态结束");
        if (async) {
            //异步更新仓位
            exchangeApiManager.getPositionExecutor().submit(() -> {
//                RLock lock = redissonClient.getLock(UPDATE_CROSS_AB_ORDER_TASK_FEE_LOCK);
//                if (lock.tryLock(120, TimeUnit.SECONDS)) {
//                    try {
                        log.info("开始同步更新仓位状态");
                        SpringUtils.getBean(ICoinsCrossExchangeArbitrageTaskService.class).updateVos(new ArrayList<>(taskUpdate));
                        log.info("更新仓位状态结束");
                        log.info("开始同步更新手续费状态");
                        SpringUtils.getBean(ICoinsCrossExchangeArbitrageTaskService.class).updateFee();
                        log.info("更新仓位手续费状态结束");
                        if (hasBatchOrder.get()) {
                            SpringUtils.getBean(BatchOrderTaskService.class).checkAndRunBatchOrderTask();
                        }
//                    } finally {
//                        lock.unlock();
//                    }
//                } else {   // perform alternative actions
//                    log.error("异步更新仓位获取锁失败...");
//                }

            });
        }else{
//            RLock lock = redissonClient.getLock(UPDATE_CROSS_AB_ORDER_TASK_FEE_LOCK);
//            if (lock.tryLock(120, TimeUnit.SECONDS)) {
//                try {     // manipulate protected state

                    log.info("开始同步更新仓位状态");
                    SpringUtils.getBean(ICoinsCrossExchangeArbitrageTaskService.class).updateVos(new ArrayList<>(taskUpdate));
                    log.info("更新仓位状态结束");
                    log.info("开始同步更新手续费状态");
                    SpringUtils.getBean(ICoinsCrossExchangeArbitrageTaskService.class).updateFee();
                    log.info("更新仓位手续费状态结束");
                    if (hasBatchOrder.get()) {
                        SpringUtils.getBean(BatchOrderTaskService.class).checkAndRunBatchOrderTask();
                    }
//                } finally {
//                    lock.unlock();
//                }
//            } else {   // perform alternative actions
//                log.error("异步更新仓位获取锁失败...");
//            }
        }



        return orders;
    }

    @Override
    public List<CoinsOrderVo> syncOrderTask(List<CoinsOrderVo> orders) {
        List<CoinsOrderVo> result = new ArrayList<>();
        //按用户先分
        Map<Long, List<CoinsOrderVo>> userOrders = orders.stream().collect(Collectors.groupingBy(CoinsOrderVo::getCreateBy));
        for (Long userid : userOrders.keySet()) {
            List<CoinsOrderVo> userTasks = userOrders.getOrDefault(userid, new ArrayList<>());
            List<CoinsOrderVo> syncedOrder = SpringUtils.getBean(this.getClass()).syncOrder(userTasks, userid,false);
            result.addAll(syncedOrder);
        }


        return result;
    }

    @Override
    public List<CoinsOrderVo> queryUnEndOrderList() {
        LambdaQueryWrapper<CoinsOrder> coinsOrderVoLambdaQueryWrapper = new LambdaQueryWrapper<CoinsOrder>()
            .eq(CoinsOrder::getOrderEnd, OrderEndStatus.NOT_END);
        return baseMapper.selectVoList(coinsOrderVoLambdaQueryWrapper);
    }

    @Override
    public List<CoinsOrderVo> queryAllOrderByTaskIds(List<Long> taskids) {
        LambdaQueryWrapper<CoinsOrder> coinsOrderVoLambdaQueryWrapper = new LambdaQueryWrapper<CoinsOrder>()
            .eq(CoinsOrder::getOrderEnd, OrderEndStatus.END);

        return baseMapper.selectVoList(coinsOrderVoLambdaQueryWrapper);
    }

    @Override
    public List<CoinsOrderVo> queryMarketPrice(List<CoinsOrderVo> rows) {
        //找出共有的交易所和币对
        HashMap<String, HashSet<String>> exSymbol = new HashMap<>();
        for (CoinsOrderVo orderVo : rows) {
            String symbol = orderVo.getSymbol();
            String ex = orderVo.getEx();
            exSymbol.computeIfAbsent(ex, k -> new HashSet<>()).add(symbol);
        }
        HashMap<String, BigDecimal> marketPriceMap = new HashMap<>();
        for (String ex : exSymbol.keySet()) {
            HashSet<String> symbols = exSymbol.get(ex);
            ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
            if(Objects.nonNull(exchangeService)) {
                for (String symbol : symbols) {
                    BigDecimal marketprice = exchangeService.getNowPrice(null, symbol);
                    marketPriceMap.put(ex+":"+symbol, marketprice);
                }
            }
        }
        for (CoinsOrderVo row : rows) {
            String exSymbolKey = row.getEx()+":"+row.getSymbol();

            BigDecimal marketPrice = marketPriceMap.get(exSymbolKey);
            row.setMarketPrice(marketPrice);
        }

        return rows;
    }

    @Override
    public R updatePrice(CoinsOrderBo bo) {
        CoinsOrderVo dbOrder = this.queryById(bo.getId());
        //修改价格
        Long id = bo.getId();
        CoinsOrderVo coinsOrderVo = this.queryById(id);
        String ex = coinsOrderVo.getEx();
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
        Long userid = coinsOrderVo.getCreateBy();
        CoinsApiVo apiVo = coinsApiService.queryByUserAndId(userid, dbOrder.getAccountId());
        Account account = AccountUtils.coverToAccount(apiVo);
        if(Objects.nonNull(exchangeService)) {
            OrderVo orderVo = new OrderVo();
            orderVo.setOrderId(coinsOrderVo.getOrderId());
            orderVo.setSymbol(coinsOrderVo.getSymbol());
            orderVo.setOrderType(coinsOrderVo.getOrderType());
            orderVo.setSide(coinsOrderVo.getSide());
            if (NumberUtils.isParsable(bo.getSize())) {
                orderVo.setSize(new BigDecimal(bo.getSize()));
                orderVo = exchangeService.calcOrderSize(orderVo);
            }
            if(NumberUtils.isParsable(bo.getPrice())) {
                orderVo.setPrice(new BigDecimal(bo.getPrice()));
            }
            orderVo.setOrderSize(new BigDecimal(coinsOrderVo.getSize()));

            if (Objects.nonNull(bo.getMarketPriceAmend()) && bo.getMarketPriceAmend() == OrderAmendStatus.MARKET_PRICE_UPDATE) {
                //市价修改
                BigDecimal marketPrice = exchangeService.getNowPrice(null, coinsOrderVo.getSymbol());
                orderVo.setPrice(marketPrice);
            }
            orderVo.setAccount(account);
            exchangeService.updateContractOrder(account,orderVo);
            return R.ok("修改成功");
        }else{
            return R.fail("unsupport exchange "+ex);
        }
    }

    @Override
    public List<CoinsOrderVo> formatSize(List<CoinsOrderVo> orderVoList, Long userId) {
        for (CoinsOrderVo row : orderVoList) {
            ExchangeService exchangeService = exchangeApiManager.getExchangeService(row.getEx());
            if(NumberUtils.isParsable(row.getSize())) {
                row.setSize(exchangeService.calcShowSize(row.getSymbol(), new BigDecimal(row.getSize())).toPlainString());
            }else{
                row.setSize("0");
            }
            row.setCumExecQty(exchangeService.calcShowSize(row.getSymbol(),row.getCumExecQty()));
        }

        return orderVoList;
    }

    @Override
    public List<CoinsOrderVo> queryAllOrderByTaskIdAndClosePositionFlag(Long id, long closePositionOrder) {
        LambdaQueryWrapper<CoinsOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoinsOrder::getTaskId, id);
        queryWrapper.eq(CoinsOrder::getClosePositionOrder, closePositionOrder);
        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public List<CoinsOrderVo> queryBatchUnEndOrderCount(Long batchId,Integer doneBatch) {
        LambdaQueryWrapper<CoinsOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoinsOrder::getBatchId, batchId);
        queryWrapper.eq(CoinsOrder::getBatchCount, doneBatch);

        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public void updateDbPrice(Long id, BigDecimal nowPrice) {
        LambdaUpdateWrapper<CoinsOrder> updateWrapper = new LambdaUpdateWrapper<CoinsOrder>()
            .eq(CoinsOrder::getId, id)
            .set(CoinsOrder::getPrice, nowPrice);
        baseMapper.update(updateWrapper);
    }

    @Override
    public CoinsOrderVo queryByOrderId(String ordId) {
        LambdaQueryWrapper<CoinsOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoinsOrder::getOrderId, ordId);
        return baseMapper.selectVoOne(queryWrapper);
    }

    @Override
    public CoinsOrderMapper getBaseMapper() {
        return baseMapper;
    }

    @Override
    public void updateDbPriceAndOrderId(Long id, BigDecimal nowPrice, String orderId) {
        LambdaUpdateWrapper<CoinsOrder> updateWrapper = new LambdaUpdateWrapper<CoinsOrder>()
            .eq(CoinsOrder::getId, id)
            .set(CoinsOrder::getPrice, nowPrice)
                .set(CoinsOrder::getOrderId, orderId);
        baseMapper.update(updateWrapper);
    }

    @Override
    public Integer queryOrderCountByBatchId(Long id, Integer doneStatus) {
        LambdaQueryWrapper<CoinsOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoinsOrder::getBatchId, id);
        queryWrapper.eq(CoinsOrder::getOrderEnd, doneStatus);

        return Math.toIntExact(baseMapper.selectCount(queryWrapper));
    }

    @Override
    public void updateOrderByContractOrder(ContractOrder contractOrder) {
        //查到了订单信息
        CoinsOrderVo order = this.queryByOrderId(contractOrder.getOrderId());
        log.info("订单  updateOrderByContractOrder {}", JSONObject.toJSONString(contractOrder));
        order.setOrderEnd(contractOrder.getOrderEnd() ? OrderEndStatus.END : OrderEndStatus.NOT_END);
        order.setFee(BigDecimalUtils.toPlainString(contractOrder.getFee()));
        order.setAvgPrice(BigDecimalUtils.toPlainString(contractOrder.getAvgPrice()));
        order.setPrice(BigDecimalUtils.toPlainString(contractOrder.getPrice()));
//                order.setSide(contractOrder.getSide());
        order.setStatus(contractOrder.getStatus());
        order.setSize(BigDecimalUtils.toPlainString(contractOrder.getSize()));
        order.setCumExecQty(contractOrder.getCumExecQty());
        order.setCumExecValue(contractOrder.getCumExecValue());
        order.setLeavesQty(contractOrder.getLeavesQty());
        order.setLeavesValue(contractOrder.getLeavesValue());
        if (Objects.nonNull(contractOrder.getPnl())) {
            order.setPnl(contractOrder.getPnl());
        }

        //如果订单对应的仓位状态是等待成交，那么根据成交情况，更新仓位状态为运行中，方便定时任务去同步仓位
        CoinsCrossExchangeArbitrageTaskVo taskVo = coinsCrossExchangeArbitrageTaskService.queryById(order.getTaskId());
        if(Objects.nonNull(taskVo)){
            if (contractOrder.getOrderEnd() && taskVo.getStatus() == CrossTaskStatus.WAIT_ORDER_DEAL) {
                //订单已经是最终状态了，不能根据这个判断是否要平仓！
                CoinsCrossExchangeArbitrageTaskVo updateTask = new CoinsCrossExchangeArbitrageTaskVo();
                updateTask.setId(order.getTaskId());
                updateTask.setStatus(CrossTaskStatus.RUNNING);
                coinsCrossExchangeArbitrageTaskService.updateVos(List.of(updateTask));
            }
        }
        SpringUtils.getBean(this.getClass()).updateVos(List.of(order));
        if (contractOrder.getOrderEnd() && order.getBatchId() != null) {
            //批次订单，触发批次
            SpringUtils.getBean(BatchOrderTaskService.class).checkAndRunBatchOrderTask();
        }


    }

    @Override
    public void publishToWs(CoinsOrderBo coinsOrderBo,Long userId) {
        executeService.getWebsocketNotifyExecute().submit(() -> {
            WebsocketMsgData<CoinsOrderVo> websocketMsgData = new WebsocketMsgData<>();
            websocketMsgData.setType(WebsocketMsgType.ORDER);
            CoinsOrderVo coinsOrderVo = new CoinsOrderVo();
            BeanUtils.copyProperties(coinsOrderBo, coinsOrderVo);
            websocketMsgData.setData(coinsOrderVo);
            WebSocketUtils.sendMessage(userId, websocketMsgData.toJSONString());
        });

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateVos(List<CoinsOrderVo> orders) {
        for (CoinsOrderVo coinsOrderVo : orders) {
            //判断ID和orderId 是否与数据库一致，一致的情况下更新，不一致则放弃，防止bitget 修改订单时会重新创建一个订单的情况
            //todo 后期需要优化锁粒度
            RLock lock = redissonClient.getLock(LockConstant.ORDER_LOCK + ":" + coinsOrderVo.getCreateBy() + ":"+coinsOrderVo.getId());
            lock.lock(30,TimeUnit.SECONDS);
            try{
                CoinsOrderVo dbOrder = this.queryById(coinsOrderVo.getId());
                if(dbOrder.getOrderId().equalsIgnoreCase(coinsOrderVo.getOrderId())){
                    CoinsOrder coinsOrder = new CoinsOrder();
                    BeanUtils.copyProperties(coinsOrderVo, coinsOrder);
                    LambdaUpdateWrapper<CoinsOrder> updateWrapper = UpdateWrapperBuilder.buildNonNullUpdateWrapper(coinsOrder, CoinsOrder::getId);
                    baseMapper.update(coinsOrder,updateWrapper);
                }else{
                    log.error("订单id：{} orderId:{} dbOrderId:{} 交易所:{} 出现id 不一致，可能被更新，放弃更新订单状态：{}",coinsOrderVo.getId()
                    ,coinsOrderVo.getOrderId(),dbOrder.getOrderId(),coinsOrderVo.getEx(),dbOrder.getStatus());
                }

            }finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
            String plainString = BigDecimalUtils.toPlainString(new BigDecimal(8));
            System.out.println(plainString);
        }

    }
}
