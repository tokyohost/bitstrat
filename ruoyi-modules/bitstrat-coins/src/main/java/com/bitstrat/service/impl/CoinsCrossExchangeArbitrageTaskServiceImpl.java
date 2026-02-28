package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.cache.MarketPriceCache;
import com.bitstrat.config.DeviceConnectionManager;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bo.CoinsBatchBo;
import com.bitstrat.domain.bo.CoinsCrossExchangeArbitrageTaskBo;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.coinGlass.ExchangeItem;
import com.bitstrat.domain.vo.*;
import com.bitstrat.mapper.CoinsCrossExchangeArbitrageTaskMapper;
import com.bitstrat.service.*;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.store.orderCheck.OrderCheckerManager;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.task.TaskService;
import com.bitstrat.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.jetbrains.annotations.Nullable;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 跨交易所套利任务Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Slf4j
@Service
public class CoinsCrossExchangeArbitrageTaskServiceImpl implements ICoinsCrossExchangeArbitrageTaskService {

    @Autowired
    private CoinsCrossExchangeArbitrageTaskMapper baseMapper;

    @Autowired
    private ExchangeApiManager exchangeApiManager;
    @Autowired
    private ICoinsApiService coinsApiService;
    @Autowired
    private DeviceConnectionManager deviceConnectionManager;
    @Autowired
    private ICoinsCrossTaskLogService coinsCrossTaskLogService;
    @Autowired
    private ICoinsOrderService coinsOrderService;
    @Autowired
    private OrderCheckerManager orderCheckerManager;

    @Autowired
    private ExecuteService executeService;
    @Autowired
    private MarketPriceCache marketPriceCache;
    @Autowired
    private ICoinsBatchService coinsBatchService;
    @Autowired
    @Lazy
    private BatchOrderTaskService batchOrderTaskService;
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private TaskService taskService;

    /**
     * 查询跨交易所套利任务
     *
     * @param id 主键
     * @return 跨交易所套利任务
     */
    @Override
    public CoinsCrossExchangeArbitrageTaskVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询跨交易所套利任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 跨交易所套利任务分页列表
     */
    @Override
    public TableDataInfo<CoinsCrossExchangeArbitrageTaskVo> queryPageList(CoinsCrossExchangeArbitrageTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> lqw = buildQueryWrapper(bo);
        Page<CoinsCrossExchangeArbitrageTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 获取交易所套利任务列表，带告警阈值
     */
    @Override
    public List<CoinsCrossExchangeArbitrageTaskVo> queryListWithWarning(Map<String, Object> params) {
        params.put("arbitrageType", ArbitrageType.CROSS_EXCHANGE);
        return baseMapper.selectVoListWithWarning(params);
    }

    /**
     * 查询符合条件的跨交易所套利任务列表
     *
     * @param bo 查询条件
     * @return 跨交易所套利任务列表
     */
    @Override
    public List<CoinsCrossExchangeArbitrageTaskVo> queryList(CoinsCrossExchangeArbitrageTaskBo bo) {
        LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> lqw = buildQueryWrapper(bo);

        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> buildQueryWrapper(CoinsCrossExchangeArbitrageTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(Objects.nonNull(bo.getUserId()), CoinsCrossExchangeArbitrageTask::getUserId, bo.getUserId());
        lqw.eq(Objects.nonNull(bo.getStatus()), CoinsCrossExchangeArbitrageTask::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增跨交易所套利任务
     *
     * @param bo 跨交易所套利任务
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsCrossExchangeArbitrageTaskBo bo) {
        CoinsCrossExchangeArbitrageTask add = MapstructUtils.convert(bo, CoinsCrossExchangeArbitrageTask.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改跨交易所套利任务
     *
     * @param bo 跨交易所套利任务
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsCrossExchangeArbitrageTaskBo bo) {
        CoinsCrossExchangeArbitrageTask update = MapstructUtils.convert(bo, CoinsCrossExchangeArbitrageTask.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsCrossExchangeArbitrageTask entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除跨交易所套利任务信息
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

    @Override
    public R createTask(CreateArbitrageTaskVo createArbitrageTaskVo) {
        CoinFundingInfo argitrageData = createArbitrageTaskVo.getArgitrageData();
        AbTaskFrom from = createArbitrageTaskVo.getFrom();
        CoinsCrossExchangeArbitrageTaskBo taskBo = new CoinsCrossExchangeArbitrageTaskBo();
        //解析buy
        ExchangeItem buy = argitrageData.getBuy();
        ArbitrageFormData fromBuy = from.getBuy();

        taskBo.setLongLeverage(fromBuy.getLeverage());
        taskBo.setLongSize(fromBuy.getSize());
        taskBo.setLongSymbol(buy.getSymbol());
        taskBo.setLongEx(buy.getExchangeName());
        if (Objects.isNull(fromBuy.getAccountId())) {
            throw new RuntimeException("please select buy side account id");
        }
        taskBo.setLongAccountId(fromBuy.getAccountId());

        //解析sell
        ExchangeItem sell = argitrageData.getSell();
        ArbitrageFormData fromSell = from.getSell();

        taskBo.setShortLeverage(fromSell.getLeverage());
        taskBo.setShortSize(fromSell.getSize());
        taskBo.setShortSymbol(sell.getSymbol());
        taskBo.setShortEx(sell.getExchangeName());
        if (Objects.isNull(fromSell.getAccountId())) {
            throw new RuntimeException("please select sell side account id");
        }
        taskBo.setShortAccountId(fromSell.getAccountId());

        taskBo.setCreateTime(new Date());
        taskBo.setStatus((long) CrossTaskStatus.CREATED);

        taskBo.setSymbol(argitrageData.getSymbol());
//        taskBo.setBatchIncome(createArbitrageTaskVo.getBatchIncome());
//        taskBo.setBatchPrice(createArbitrageTaskVo.getBatchPrice());

        taskBo.setRole(JSONObject.toJSONString(createArbitrageTaskVo));
        taskBo.setUserId(LoginHelper.getUserId());
        Boolean b = this.insertByBo(taskBo);
        createArbitrageTaskVo.setTaskBo(taskBo);
        return b ? R.ok() : R.fail();
    }

    /**
     * 一次建仓
     * 弃用！
     */
    @Deprecated
    public void oncePlaceOrder(CoinsCrossExchangeArbitrageTaskVo crossTask) {

    }

    /**
     * 两腿下单
     *
     * @param from
     */
    public void oncePlace2ExOrder(AbTaskFrom from) {
        CoinsCrossExchangeArbitrageTaskVo crossTask = this.queryById(from.getTaskId());
        //获取api
        CoinsApiVo longExApi;
        CoinsApiVo shortExApi;
        if (Objects.nonNull(crossTask.getLongAccountId())) {
            longExApi = coinsApiService.queryById(crossTask.getLongAccountId());
        }else{
            longExApi = coinsApiService.queryApiByUserIdAndExchange(crossTask.getUserId(), crossTask.getLongEx().toLowerCase());
            LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateAccountId = new LambdaUpdateWrapper<>();
            updateAccountId.eq(CoinsCrossExchangeArbitrageTask::getId, crossTask.getId());
            updateAccountId.set(CoinsCrossExchangeArbitrageTask::getLongAccountId, longExApi.getId());
            this.getBaseMapper().update(updateAccountId);
        }
        if(Objects.nonNull(crossTask.getShortAccountId())) {
            shortExApi = coinsApiService.queryById(crossTask.getShortAccountId());
        }else{
            shortExApi = coinsApiService.queryApiByUserIdAndExchange(crossTask.getUserId(), crossTask.getShortEx().toLowerCase());
            LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateAccountId = new LambdaUpdateWrapper<>();
            updateAccountId.eq(CoinsCrossExchangeArbitrageTask::getId, crossTask.getId());
            updateAccountId.set(CoinsCrossExchangeArbitrageTask::getShortAccountId, shortExApi.getId());
            this.getBaseMapper().update(updateAccountId);
        }
        if (Objects.isNull(longExApi) || Objects.isNull(shortExApi)) {
            CoinsCrossExchangeArbitrageTaskBo update = new CoinsCrossExchangeArbitrageTaskBo();
            update.setStatus((long) CrossTaskStatus.STOPED);
            update.setId(crossTask.getId());
            this.updateByBo(update);
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "没有配置好API");
            throw new RuntimeException("没有配置好API");
        }
        //查找两个交易所的价格，并取中间值下单

        ExchangeService longExService = exchangeApiManager.getExchangeService(crossTask.getLongEx());
        ExchangeService shortExService = exchangeApiManager.getExchangeService(crossTask.getShortEx());
        Account accountLong = AccountUtils.coverToAccount(longExApi);
        Account accountShort = AccountUtils.coverToAccount(shortExApi);
        //设置杠杆
        try {
            longExService.setLeverage(accountLong, Math.toIntExact(crossTask.getLongLeverage()), crossTask.getSymbol(), CrossContractSide.LONG);
            shortExService.setLeverage(accountShort, Math.toIntExact(crossTask.getShortLeverage()), crossTask.getSymbol(), CrossContractSide.SHORT);
        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "设置杠杆失败！" + e.getMessage());
            throw new RuntimeException("设置杠杆失败");
        }

        //先初始化监听市场价
        CompletableFuture<BigDecimal> longPriceFuture = CompletableFuture.supplyAsync(() -> {
            return longExService.getNowPrice(null, crossTask.getSymbol());
        }, exchangeApiManager.getTaskExecutor());
        CompletableFuture<BigDecimal> shortPriceFuture = CompletableFuture.supplyAsync(() -> {
            return shortExService.getNowPrice(null, crossTask.getSymbol());
        }, exchangeApiManager.getTaskExecutor());

        BigDecimal longprice = longPriceFuture.join();
        BigDecimal shortprice = shortPriceFuture.join();
        if (Objects.isNull(longprice) || Objects.isNull(shortprice)) {
            CoinsCrossExchangeArbitrageTaskBo update = new CoinsCrossExchangeArbitrageTaskBo();
            update.setStatus((long) CrossTaskStatus.STOPED);
            update.setId(crossTask.getId());
            this.updateByBo(update);
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "获取实时价格出错,任务终止");
            throw new RuntimeException("获取实时价格出错,任务终止");
        }

        //根据两个价格计算最合适的买入价格
        BigDecimal midPrice = PriceCalculator.calculateMidPrice(longprice, shortprice);

        coinsCrossTaskLogService.saveLog(crossTask.getId(), crossTask.getSymbol() + "获取实时价格 " + crossTask.getLongEx() + "- " + longprice + " / " + crossTask.getShortEx() + "- " + shortprice
            + " 计算最优下单价格:" + midPrice);
        OrderVo longOrderPre = new OrderVo();
        longOrderPre.setAccount(accountLong);
        if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            longOrderPre.setOrderType(OrderType.MARKET);
        } else {
            longOrderPre.setOrderType(OrderType.LIMIT);
        }
        longOrderPre.setPrice(midPrice);
        longOrderPre.setSize(from.getBuy().getSize());
        longOrderPre.setSymbol(crossTask.getSymbol());
        longOrderPre.setLeverage(BigDecimal.valueOf(crossTask.getLongLeverage()));
        OrderVo longOrder = longExService.calcOrderSize(longOrderPre);

        OrderVo shortOrderPre = new OrderVo();
        shortOrderPre.setAccount(accountShort);
        if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            shortOrderPre.setOrderType(OrderType.MARKET);
        } else {
            shortOrderPre.setOrderType(OrderType.LIMIT);
        }
        shortOrderPre.setPrice(midPrice);
        shortOrderPre.setSize(from.getSell().getSize());
        shortOrderPre.setSymbol(crossTask.getSymbol());
        shortOrderPre.setLeverage(BigDecimal.valueOf(crossTask.getShortLeverage()));
        OrderVo shortOrder = shortExService.calcOrderSize(shortOrderPre);
        try {
            longExService.preCheckOrder(longOrder);
            shortExService.preCheckOrder(shortOrder);
        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "订单检查异常:" + e.getMessage());
            throw e;
        }
        //订单不知道是否成交，仓位任务状态修改为等待订单成交
        this.updateTaskStatus(crossTask.getId(), CrossTaskStatus.WAIT_ORDER_DEAL);
        AtomicBoolean orderError = new AtomicBoolean(false);
        AtomicReference<String> orderErrorMsg = new AtomicReference<>();
        CompletableFuture<OrderOptStatus> longExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return longExService.buyContract(accountLong, longOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "多单下单异常:" + e.getMessage());
//            throw e;
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
            e.printStackTrace();
            return null;
        });
        ;
        CompletableFuture<OrderOptStatus> shortExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return shortExService.sellContract(accountShort, shortOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "空单下单异常:" + e.getMessage());
//            throw e;
            orderError.set(true);

            orderErrorMsg.set(e.getMessage());
            e.printStackTrace();
            return null;
        });

        //监听订单状态，如果两方订单都成功，则成功，一方成功，另一方3秒后还没有成功，则撤销订单
        OrderOptStatus longOrderStatus = null;
        OrderOptStatus shortOrderStatus = null;


        try {
            longOrderStatus = longExOrderFuture.join();

        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "多单下单异常:" + e.getMessage());
//            throw e;
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }
        try {
            shortOrderStatus = shortExOrderFuture.join();
        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "空单下单异常:" + e.getMessage());
//            throw e;
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }
        //保存订单
        if (Objects.nonNull(longOrderStatus) && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setTaskId(crossTask.getId());
            coinsOrderBo.setEx(crossTask.getLongEx());
            coinsOrderBo.setOrderId(longOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(crossTask.getSymbol());
            coinsOrderBo.setSide(SideType.LONG);
            coinsOrderBo.setSize(longOrder.getSize().toPlainString());
            coinsOrderBo.setCreateBy(crossTask.getUserId());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setAccountId(crossTask.getLongAccountId());
            if (from.getBatchId() != null) {
                //是批次任务
                coinsOrderBo.setBatchId(from.getBatchId());
                coinsOrderBo.setBatchCount(from.getBatchCount());
            }
            if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            //检查是否是同步订单，有的交易所api 会同步返回订单状态，比如币安
            longOrderStatus.setAccount(accountLong);
            OrderOptStatus finalLongOrderStatus = longOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(crossTask.getSymbol(), finalLongOrderStatus,longExService);
            });

            //这里是去监控订单成交状态，未成交的限价单会及时修改为最新价格
            orderCheckerManager.submit(coinsOrderBo, accountLong, Integer.MAX_VALUE, 2000);
        }
        if (Objects.nonNull(shortOrderStatus) && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setTaskId(crossTask.getId());
            coinsOrderBo.setEx(crossTask.getShortEx());
            coinsOrderBo.setOrderId(shortOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(crossTask.getSymbol());
            coinsOrderBo.setSide(SideType.SHORT);
            coinsOrderBo.setSize(shortOrder.getSize().toPlainString());
            coinsOrderBo.setCreateBy(crossTask.getUserId());
            coinsOrderBo.setAccountId(crossTask.getShortAccountId());
            if (from.getBatchId() != null) {
                //是批次任务
                coinsOrderBo.setBatchId(from.getBatchId());
                coinsOrderBo.setBatchCount(from.getBatchCount());
            }
            if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            //检查是否是同步订单，有的交易所api 会同步返回订单状态，比如币安
            shortOrderStatus.setAccount(accountShort);
            OrderOptStatus finalShortOrderStatus = shortOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(crossTask.getSymbol(), finalShortOrderStatus,shortExService);
            });

            //这里是去监控订单成交状态，未成交的限价单会及时修改为最新价格
            orderCheckerManager.submit(coinsOrderBo, accountShort, Integer.MAX_VALUE, 2000);
        }

        if (longOrderStatus != null && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)
            && shortOrderStatus != null && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //已创建
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单成功");
            return;
        }

        if (longOrderStatus != null && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //做多订单创建成功，需要取消订单
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "做多订单创建成功正在取消...");
            String cancelled = longExService.cancelContractOrder(accountLong, longOrderStatus);
            if (cancelled.equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
                //取消成功
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做多订单取消成功");
                return;
            } else {
                //todo 取消失败
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做多订单取消失败，请检查 " + longExService.getExchangeName() + "持仓情况手动平仓！");
            }
        }
        if (shortOrderStatus != null && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //做空订单创建成功，需要取消订单，如已成交则平仓
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "做空订单创建成功正在取消...");
            String cancelled = longExService.cancelContractOrder(accountLong, shortOrderStatus);
            if (cancelled.equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
                //取消成功
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做空订单取消成功");
                return;
            } else {
                //todo 取消失败
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做空订单取消失败，请检查" + shortExService.getExchangeName() + "持仓情况手动平仓！");
            }
        }
        coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单结束...");
        //更新任务状态
        //如果任务状态已经是等待成交或者运行中了就不修改
        if (crossTask.getStatus() == CrossTaskStatus.RUNNING
            || crossTask.getStatus() == CrossTaskStatus.WAIT_ORDER_DEAL) {
        } else {
            //订单不知道是否成交，仓位任务状态修改为等待订单成交
//            this.updateTaskStatus(crossTask.getId(), CrossTaskStatus.WAIT_ORDER_DEAL);
        }

        if (from.isBatchFlag() && orderError.get()) {
            //是批次任务,下单出错了往外抛异常
            throw new RuntimeException(orderErrorMsg.get());
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
                log.info("是同步返回订单，更新订单状态完成---");
            }else{
                log.warn("是同步返回订单，更新订单状态失败，formate 订单为null");
            }
        }
    }

    public synchronized void updateTaskStatus(Long id, Long status) {
        LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> update = new LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask>();
        update.eq(CoinsCrossExchangeArbitrageTask::getId, id).set(CoinsCrossExchangeArbitrageTask::getStatus, status);
        this.baseMapper.update(update);
    }

    /**
     * 计算年华
     *
     * @param rows
     * @param userId
     * @return
     */
    @Override
    public List<CoinsCrossExchangeArbitrageTaskVo> formateARY(List<CoinsCrossExchangeArbitrageTaskVo> rows, Long userId) {
        for (CoinsCrossExchangeArbitrageTaskVo row : rows) {
            if (CrossTaskStatus.WAIT_ORDER_DEAL == row.getStatus() ||
                CrossTaskStatus.RUNNING == row.getStatus()) {
                Date createTime = row.getCreateTime();
                Date updateTime = row.getUpdateTime();
                long daysDifference = InvestmentUtils.calculateDaysDifference(createTime, updateTime);
                String longAvgPrice = row.getLongAvgPrice();
                String shortAvgPrice = row.getShortAvgPrice();
                BigDecimal longSymbolSize = row.getLongSymbolSize() == null ? BigDecimal.ZERO : row.getLongSymbolSize();
                BigDecimal shortSymbolSize = row.getShortSymbolSize() == null ? BigDecimal.ZERO : row.getShortSymbolSize();
                //这里不用转换持仓数量，因为上一步已经转换了
                BigDecimal longAvg = NumberUtils.isParsable(longAvgPrice) ? new BigDecimal(longAvgPrice).setScale(8, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
                BigDecimal shortAvg = NumberUtils.isParsable(shortAvgPrice) ? new BigDecimal(shortAvgPrice).setScale(8, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
                BigDecimal longTotal = longAvg.multiply(longSymbolSize);
                BigDecimal shortTotal = shortAvg.multiply(shortSymbolSize);
                BigDecimal longAvgTotal = longTotal.add(shortTotal);

                //计算年华
                BigDecimal apy = InvestmentUtils.calculateAnnualizedReturn(row.getTotalProfit(), longAvgTotal, (int) daysDifference);
                row.setApy(apy);
            }
        }

        return rows;
    }

    @Override
    public CoinsCrossExchangeArbitrageTaskVo queryActiveTaskByUserIdAndSymbol(String symbol, Long userId) {
        LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> queryWrapper = new LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask>();
        queryWrapper.eq(CoinsCrossExchangeArbitrageTask::getSymbol, symbol);
        queryWrapper.eq(CoinsCrossExchangeArbitrageTask::getUserId, userId);
        queryWrapper.in(CoinsCrossExchangeArbitrageTask::getStatus, List.of(CrossTaskStatus.RUNNING, CrossTaskStatus.WAIT_ORDER_DEAL,CrossTaskStatus.CLOSED
        ,CrossTaskStatus.STOPED));
        queryWrapper.orderByDesc(CoinsCrossExchangeArbitrageTask::getCreateTime);
        queryWrapper.last("limit 1");
        return this.baseMapper.selectVoOne(queryWrapper);
    }

    @Override
    public CoinsCrossExchangeArbitrageTaskVo queryActiveTaskByUserIdSymbolAndAccountId(String symbol, Long userId, Long account) {
        LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> queryWrapper = new LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask>();
        queryWrapper.eq(CoinsCrossExchangeArbitrageTask::getSymbol, symbol);
        queryWrapper.eq(CoinsCrossExchangeArbitrageTask::getUserId, userId);

        queryWrapper.and(wrapper ->
            wrapper.eq(CoinsCrossExchangeArbitrageTask::getLongAccountId, account)
                .or()
                .eq(CoinsCrossExchangeArbitrageTask::getShortAccountId, account)
        );
        queryWrapper.in(CoinsCrossExchangeArbitrageTask::getStatus, List.of(CrossTaskStatus.RUNNING, CrossTaskStatus.WAIT_ORDER_DEAL,CrossTaskStatus.CLOSED
            ,CrossTaskStatus.STOPED));
        queryWrapper.orderByDesc(CoinsCrossExchangeArbitrageTask::getCreateTime);
        queryWrapper.last("limit 1");
        return this.baseMapper.selectVoOne(queryWrapper);
    }

    @Override
    public List<CoinsCrossExchangeArbitrageTaskVo> queryActiveTaskByUserIdAndAccountId(Long userId, Long accountId) {
        LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask> queryWrapper = new LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask>();
        queryWrapper.eq(CoinsCrossExchangeArbitrageTask::getUserId, userId);

        queryWrapper.and(wrapper ->
            wrapper.eq(CoinsCrossExchangeArbitrageTask::getLongAccountId, accountId)
                .or()
                .eq(CoinsCrossExchangeArbitrageTask::getShortAccountId, accountId)
        );
        queryWrapper.in(CoinsCrossExchangeArbitrageTask::getStatus, List.of(CrossTaskStatus.RUNNING, CrossTaskStatus.WAIT_ORDER_DEAL,CrossTaskStatus.CLOSED
            ,CrossTaskStatus.STOPED));
        queryWrapper.orderByDesc(CoinsCrossExchangeArbitrageTask::getCreateTime);
        return this.baseMapper.selectVoList(queryWrapper);
    }

    /**
     * 两腿平仓
     *
     * @param from
     */
    public void oncePlace2ExOrderClosePosition(AbTaskFrom from) {
        CoinsCrossExchangeArbitrageTaskVo crossTask = this.queryById(from.getTaskId());
        //获取api
        CoinsApiVo longExApi;
        if (Objects.nonNull(crossTask.getLongAccountId())) {
            longExApi = coinsApiService.queryById(crossTask.getLongAccountId());
        }else {
            longExApi = coinsApiService.queryApiByUserIdAndExchange(crossTask.getUserId(), crossTask.getLongEx().toLowerCase());

            //保存accountId
            LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateAccountId = new LambdaUpdateWrapper<>();
            updateAccountId.eq(CoinsCrossExchangeArbitrageTask::getId, crossTask.getId());
            updateAccountId.set(CoinsCrossExchangeArbitrageTask::getLongAccountId, longExApi.getId());
            this.getBaseMapper().update(updateAccountId);
        }
        CoinsApiVo shortExApi;
        if(Objects.nonNull(crossTask.getShortAccountId())) {
            shortExApi = coinsApiService.queryById(crossTask.getShortAccountId());
        }else{
            shortExApi = coinsApiService.queryApiByUserIdAndExchange(crossTask.getUserId(), crossTask.getShortEx().toLowerCase());
            //保存accountId
            LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateAccountId = new LambdaUpdateWrapper<>();
            updateAccountId.eq(CoinsCrossExchangeArbitrageTask::getId, crossTask.getId());
            updateAccountId.set(CoinsCrossExchangeArbitrageTask::getShortAccountId, shortExApi.getId());
            this.getBaseMapper().update(updateAccountId);
        }

        if (Objects.isNull(longExApi) || Objects.isNull(shortExApi)) {
            this.updateTaskStatus(crossTask.getId(), CrossTaskStatus.STOPED);
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "没有配置好API");
            throw new RuntimeException("没有配置好API");
        }
        //查找两个交易所的价格，并取中间值下单

        ExchangeService longExService = exchangeApiManager.getExchangeService(crossTask.getLongEx());
        ExchangeService shortExService = exchangeApiManager.getExchangeService(crossTask.getShortEx());
        Account accountLong = AccountUtils.coverToAccount(longExApi);
        Account accountShort = AccountUtils.coverToAccount(shortExApi);
        //设置杠杆
        try {
            longExService.setLeverage(accountLong, Math.toIntExact(crossTask.getLongLeverage()), crossTask.getSymbol(), CrossContractSide.LONG);
            shortExService.setLeverage(accountShort, Math.toIntExact(crossTask.getShortLeverage()), crossTask.getSymbol(), CrossContractSide.SHORT);
        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "设置杠杆失败！" + e.getMessage());
            throw new RuntimeException("设置杠杆失败");
        }

        //先初始化监听市场价
        CompletableFuture<BigDecimal> longPriceFuture = CompletableFuture.supplyAsync(() -> {
            return longExService.getNowPrice(null, crossTask.getSymbol());
        }, exchangeApiManager.getTaskExecutor());
        CompletableFuture<BigDecimal> shortPriceFuture = CompletableFuture.supplyAsync(() -> {
            return shortExService.getNowPrice(null, crossTask.getSymbol());
        }, exchangeApiManager.getTaskExecutor());

        BigDecimal longprice = longPriceFuture.join();
        BigDecimal shortprice = shortPriceFuture.join();
        if (Objects.isNull(longprice) || Objects.isNull(shortprice)) {

            this.updateTaskStatus(crossTask.getId(), CrossTaskStatus.STOPED);
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "获取实时价格出错,任务终止");
            throw new RuntimeException("获取实时价格出错,任务终止");
        }

        //根据两个价格计算最合适的买入价格
        BigDecimal midPrice = PriceCalculator.calculateMidPrice(longprice, shortprice);

        coinsCrossTaskLogService.saveLog(crossTask.getId(), crossTask.getSymbol() + "获取实时价格 " + crossTask.getLongEx() + "- " + longprice + " / " + crossTask.getShortEx() + "- " + shortprice
            + " 计算最优下单价格:" + midPrice);


        OrderVo longOrderPre = new OrderVo();
        if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            longOrderPre.setOrderType(OrderType.MARKET);
        } else {
            longOrderPre.setOrderType(OrderType.LIMIT);
        }
        longOrderPre.setPrice(midPrice);
        longOrderPre.setReduceOnly(true);
        longOrderPre.setSize(from.getBuy().getSize());
        longOrderPre.setSymbol(crossTask.getSymbol());
        longOrderPre.setAccount(accountLong);
        OrderVo longOrder = longExService.calcOrderSize(longOrderPre);

        OrderVo shortOrderPre = new OrderVo();
        if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            shortOrderPre.setOrderType(OrderType.MARKET);
        } else {
            shortOrderPre.setOrderType(OrderType.LIMIT);
        }

        shortOrderPre.setPrice(midPrice);
        shortOrderPre.setSize(from.getSell().getSize());
        shortOrderPre.setSymbol(crossTask.getSymbol());
        shortOrderPre.setReduceOnly(true);
        shortOrderPre.setAccount(accountShort);
        OrderVo shortOrder = shortExService.calcOrderSize(shortOrderPre);


        try {
            longExService.preCheckOrder(longOrder);
            shortExService.preCheckOrder(shortOrder);
        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "订单检查异常:" + e.getMessage());
            throw e;
        }


        AtomicBoolean orderError = new AtomicBoolean(false);
        AtomicReference<String> orderErrorMsg = new AtomicReference<>();
        //平仓，做多的一边应该卖掉，做空的一边需要买回来
        CompletableFuture<OrderOptStatus> longExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return longExService.sellContract(accountLong, longOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
            return null;
        });
        CompletableFuture<OrderOptStatus> shortExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return shortExService.buyContract(accountShort, shortOrder);
        }, exchangeApiManager.getTaskExecutor()).exceptionally((e) -> {
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单异常:" + e.getMessage());
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
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }

        try {
            shortOrderStatus = shortExOrderFuture.join();
        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单异常:" + e.getMessage());
            orderError.set(true);
            orderErrorMsg.set(e.getMessage());
        }
        //保存订单
        if (Objects.nonNull(longOrderStatus) && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setTaskId(crossTask.getId());
            coinsOrderBo.setEx(crossTask.getLongEx().toLowerCase());
            coinsOrderBo.setOrderId(longOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(crossTask.getSymbol());
            coinsOrderBo.setSide(SideType.LONG);
            coinsOrderBo.setSize(longOrder.getSize().toPlainString());
            coinsOrderBo.setPrice(BigDecimalUtils.toPlainString(longOrder.getPrice()));
            coinsOrderBo.setCreateBy(crossTask.getUserId());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setClosePositionOrder(CrossClosePositionFlag.CLOSE_POSITION_ORDER);
            coinsOrderBo.setAccountId(crossTask.getLongAccountId());
            if (from.getBatchId() != null) {
                //是批次任务
                coinsOrderBo.setBatchId(from.getBatchId());
                coinsOrderBo.setBatchCount(from.getBatchCount());
            }

            if (StringUtils.isNotEmpty(from.getBuy().getOrderType()) && from.getBuy().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            //检查是否是同步订单，有的交易所api 会同步返回订单状态，比如币安
            longOrderStatus.setAccount(accountShort);
            OrderOptStatus finalLongOrderStatus = longOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(crossTask.getSymbol(), finalLongOrderStatus,shortExService);
            });

            //这里是去监控订单成交状态，未成交的限价单会及时修改为最新价格
            orderCheckerManager.submit(coinsOrderBo, accountLong, Integer.MAX_VALUE, 2000);
        }
        if (Objects.nonNull(shortOrderStatus) && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setTaskId(crossTask.getId());
            coinsOrderBo.setEx(crossTask.getShortEx().toLowerCase());
            coinsOrderBo.setOrderId(shortOrderStatus.getOrderId());
            coinsOrderBo.setSymbol(crossTask.getSymbol());
            coinsOrderBo.setPrice(BigDecimalUtils.toPlainString(shortOrder.getPrice()));
            coinsOrderBo.setSide(SideType.SHORT);
            coinsOrderBo.setSize(shortOrder.getSize().toPlainString());
            coinsOrderBo.setCreateBy(crossTask.getUserId());
            coinsOrderBo.setClosePositionOrder(CrossClosePositionFlag.CLOSE_POSITION_ORDER);
            coinsOrderBo.setAccountId(crossTask.getShortAccountId());
            if (from.getBatchId() != null) {
                //是批次任务
                coinsOrderBo.setBatchId(from.getBatchId());
                coinsOrderBo.setBatchCount(from.getBatchCount());
            }

            if (StringUtils.isNotEmpty(from.getSell().getOrderType()) && from.getSell().getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            shortOrderStatus.setAccount(accountShort);
            OrderOptStatus finalShortOrderStatus = shortOrderStatus;
            executeService.getSyncOrderCheck().submit(()->{
                checkSyncOrder(crossTask.getSymbol(), finalShortOrderStatus,shortExService);
            });

            //这里是去监控订单成交状态，未成交的限价单会及时修改为最新价格
            orderCheckerManager.submit(coinsOrderBo, accountShort, Integer.MAX_VALUE, 2000);
        }

        if (longOrderStatus != null && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)
            && shortOrderStatus != null && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //已创建
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单成功");
            return;
        }

        if (longOrderStatus != null && longOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //做多订单创建成功，需要取消订单，如果已成交则平仓
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "做多订单创建成功正在取消...");
            String cancelled = longExService.cancelContractOrder(accountLong, longOrderStatus);
            if (cancelled.equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
                //取消成功
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做多订单取消成功");
                return;
            } else {
                //todo 取消失败
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做多订单取消失败，请检查持仓情况手动平仓！");
            }
        }
        if (shortOrderStatus != null && shortOrderStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            //做空订单创建成功，需要取消订单，如已成交则平仓
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "做空订单创建成功正在取消...");
            String cancelled = longExService.cancelContractOrder(accountLong, shortOrderStatus);
            if (cancelled.equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
                //取消成功
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做空订单取消成功");
                return;
            } else {
                //todo 取消失败
                coinsCrossTaskLogService.saveLog(crossTask.getId(), "做空订单取消失败，请检查持仓情况手动平仓！");
            }
        }
        coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单结束...");

        if (from.isBatchFlag() && orderError.get()) {
            //是批次任务,下单出错了往外抛异常
            throw new RuntimeException(orderErrorMsg.get());
        }
    }

    /**
     * 允许一腿平仓
     */
    public void onceSideOrderClosePosition(ArbitrageFormData formData, Long taskId, SingleClosePosition singleClosePosition) {
        CoinsCrossExchangeArbitrageTaskVo crossTask = this.queryById(taskId);

        coinsCrossTaskLogService.saveLog(crossTask.getId(), "单腿平仓 平仓交易所 " + singleClosePosition.getEx());
        //获取api
        CoinsApiVo exApi;
        if(Objects.nonNull(singleClosePosition.getAccountId())){
            exApi = coinsApiService.queryById(singleClosePosition.getAccountId());
        }else{
            exApi = coinsApiService.queryApiByUserIdAndExchange(crossTask.getUserId(), singleClosePosition.getEx().toLowerCase());
        }

        ExchangeService exchangeService = exchangeApiManager.getExchangeService(singleClosePosition.getEx());

        if (Objects.isNull(exApi)) {

            this.updateTaskStatus(crossTask.getId(), CrossTaskStatus.STOPED);
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "没有配置好API");
            throw new RuntimeException("没有配置好API");
        }

        Account account = AccountUtils.coverToAccount(exApi);

        try {
            if(singleClosePosition.getSide().equals(SideType.SHORT)){
                exchangeService.setLeverage(account, Math.toIntExact(crossTask.getShortLeverage()), crossTask.getSymbol(), CrossContractSide.SHORT);
            }else if(singleClosePosition.getSide().equals(SideType.LONG)){
                exchangeService.setLeverage(account, Math.toIntExact(crossTask.getLongLeverage()), crossTask.getSymbol(), CrossContractSide.LONG);
            }

        } catch (Exception e) {
            e.printStackTrace();
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "设置杠杆失败！" + e.getMessage());
            throw new RuntimeException("设置杠杆失败");
        }
        //先初始化监听市场价
        BigDecimal nowPrice = exchangeService.getNowPrice(null, crossTask.getSymbol());
        if (Objects.isNull(nowPrice)) {

            this.updateTaskStatus(crossTask.getId(), CrossTaskStatus.STOPED);
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "获取实时价格出错,任务终止");
            throw new RuntimeException("获取实时价格出错,任务终止");
        }


        //根据两个价格计算最合适的买入价格
        BigDecimal midPrice = nowPrice;

        coinsCrossTaskLogService.saveLog(crossTask.getId(), crossTask.getSymbol() + "获取实时价格 " + singleClosePosition.getEx() + "- " + nowPrice
            + " 计算最优下单价格:" + midPrice);


        OrderVo orderPre = new OrderVo();
        if (StringUtils.isNotEmpty(formData.getOrderType()) && formData.getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            orderPre.setOrderType(OrderType.MARKET);
        } else {
            orderPre.setOrderType(OrderType.LIMIT);
        }
        orderPre.setPrice(midPrice);
        orderPre.setReduceOnly(true);
        orderPre.setSize(formData.getSize());
        orderPre.setSymbol(crossTask.getSymbol());
        orderPre.setAccount(account);
        OrderVo order = exchangeService.calcOrderSize(orderPre);
        OrderOptStatus orderOptStatus;
        if (singleClosePosition.getSide().equalsIgnoreCase(SideType.LONG)) {
            orderOptStatus = exchangeService.sellContract(account, order);
        } else if (singleClosePosition.getSide().equalsIgnoreCase(SideType.SHORT)) {
            orderOptStatus = exchangeService.buyContract(account, order);
        } else {
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单异常: 未知的买卖方向");
            return;
        }


        //保存订单
        if (Objects.nonNull(orderOptStatus) && orderOptStatus.getStatus().equalsIgnoreCase(CrossOrderStatus.SUCCESS)) {
            CoinsOrderBo coinsOrderBo = new CoinsOrderBo();
            coinsOrderBo.setTaskId(crossTask.getId());
            coinsOrderBo.setEx(crossTask.getLongEx());
            coinsOrderBo.setOrderId(orderOptStatus.getOrderId());
            coinsOrderBo.setSymbol(crossTask.getSymbol());
            coinsOrderBo.setSide(SideType.LONG);
            coinsOrderBo.setPrice(BigDecimalUtils.toPlainString(order.getPrice()));
            coinsOrderBo.setCreateBy(crossTask.getUserId());
            coinsOrderBo.setSize(order.getSize().toPlainString());
            coinsOrderBo.setStatus(ContractOrderStatus.NEW.toLowerCase());
            coinsOrderBo.setClosePositionOrder(CrossClosePositionFlag.CLOSE_POSITION_ORDER);
            coinsOrderBo.setAccountId(singleClosePosition.getAccountId());

            if (StringUtils.isNotEmpty(formData.getOrderType()) && formData.getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
                coinsOrderBo.setOrderType(OrderType.MARKET);
            } else {
                coinsOrderBo.setOrderType(OrderType.LIMIT);
            }
            coinsOrderService.insertByBo(coinsOrderBo);
            orderOptStatus.setAccount(account);
            checkSyncOrder(crossTask.getSymbol(),orderOptStatus,exchangeService);
            //这里是去监控订单成交状态，未成交的限价单会及时修改为最新价格
            orderCheckerManager.submit(coinsOrderBo, account, Integer.MAX_VALUE, 2000);
        }

        coinsCrossTaskLogService.saveLog(crossTask.getId(), "下单结束...");

    }


    @Override
    @Deprecated
    public R startTask(StartTaskVo task) {
        CoinsCrossExchangeArbitrageTaskVo crossTask = baseMapper.selectVoById(task.getTaskId());
        if (Objects.isNull(crossTask)) {
            return R.fail("执行失败，没有找到此任务");
        }


        if (crossTask.getBatchIncome() != null && crossTask.getBatchIncome() == 1) {
            //分批入场
            coinsCrossTaskLogService.saveLog(crossTask.getId(), "已提交分批入场策略");
            return R.ok("后台已执行分批入场策略");
        }


        crossTask.setUserId(LoginHelper.getUserId());
        exchangeApiManager.getTaskExecutor().execute(() -> {
            this.oncePlaceOrder(crossTask);
        });


        return R.ok("启动成功");
    }

    //创建双腿下单
    @Override
    public R createCrossArbitrage(CreateArbitrageTaskVo createArbitrageTaskVo) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            ThreadLocalLogUtil.clear();
            if (createArbitrageTaskVo.getBatchIncome() == 1) {
                //分批建仓
                this.createBatchOrder(createArbitrageTaskVo);
                batchOrderTaskService.asyncCheckAndRunBatchOrderTask();
            } else {
                this.oncePlace2ExOrder(createArbitrageTaskVo.getFrom());
            }

            //获取日志
            List<String> logs = ThreadLocalLogUtil.getLogs();
            result.put("logs", logs);
            result.put("success", true);
            //更新任务状态为等待成交
            CoinsCrossExchangeArbitrageTaskBo update = new CoinsCrossExchangeArbitrageTaskBo();
            update.setId(createArbitrageTaskVo.getFrom().getTaskId());
            update.setStatus(CrossTaskStatus.WAIT_ORDER_DEAL);
            this.updateByBo(update);
            return R.ok("下单成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            List<String> logs = ThreadLocalLogUtil.getLogs();
            result.put("logs", logs);
            result.put("success", false);
            return R.ok(e.getMessage(), result);
        } finally {
            ThreadLocalLogUtil.clear();
        }
    }

    /**
     * 分批建仓
     *
     * @param createArbitrageTaskVo
     */
    public void createBatchOrder(CreateArbitrageTaskVo createArbitrageTaskVo) {

        //计算总操作金额
        AbTaskFrom from = createArbitrageTaskVo.getFrom();
        CoinsCrossExchangeArbitrageTaskVo crossTask = this.queryById(from.getTaskId());
        BigDecimal buySize = from.getBuy().getSize();
        BigDecimal sellSize = from.getSell().getSize();
        CoinsBatchBo coinsBatchBo = new CoinsBatchBo();
        coinsBatchBo.setTaskId(crossTask.getId());
        List<Double> operations = RatioOperationBuilder.calculateOperations(createArbitrageTaskVo.getBatchSize().doubleValue());
        coinsBatchBo.setBatchTotal((long) operations.size());
        coinsBatchBo.setBuyEx(crossTask.getLongEx());
        coinsBatchBo.setBatchSize(new BigDecimal(createArbitrageTaskVo.getBatchSize()));
        coinsBatchBo.setSellEx(crossTask.getShortEx());
        coinsBatchBo.setBuyTotal(buySize);
        coinsBatchBo.setSellTotal(sellSize);
        coinsBatchBo.setTotalSize(sellSize.add(buySize));
        coinsBatchBo.setDoneBatch(0L);
        coinsBatchBo.setStatus(BatchOrderTaskStatus.RUNNING);
        coinsBatchBo.setStartTime(new Date());
        coinsBatchBo.setUserId(LoginHelper.getUserId());

        coinsBatchBo.setSymbol(crossTask.getSymbol());
        coinsBatchBo.setBuyLeverage(Math.toIntExact(crossTask.getLongLeverage()));
        coinsBatchBo.setSellLeverage(Math.toIntExact(crossTask.getShortLeverage()));
        coinsBatchBo.setDoneBuySize(BigDecimal.ZERO);
        coinsBatchBo.setDoneSellSize(BigDecimal.ZERO);
        coinsBatchBo.setBuyOrderType(from.getBuy().getOrderType());
        coinsBatchBo.setSellOrderType(from.getSell().getOrderType());
        coinsBatchBo.setSide(from.getSide());
        coinsBatchBo.setCreateTime(new Date());
        coinsBatchBo.setBotId(createArbitrageTaskVo.getBotId());

        coinsBatchService.insertByBo(coinsBatchBo);
        coinsCrossTaskLogService.saveLog(crossTask.getId(), String.format("分批下单任务已创建 总计买入数量%s 总计卖出数量%s 总计两腿下单次数%s次",
            coinsBatchBo.getBuyTotal(), coinsBatchBo.getSellTotal(), coinsBatchBo.getBatchTotal()));


    }

    @Override
    public synchronized List<CoinsCrossExchangeArbitrageTaskVo> syncPosition(List<CoinsCrossExchangeArbitrageTaskVo> rows, Long userId) {
        List<CoinsCrossExchangeArbitrageTaskVo> needSync = rows.stream().filter(item -> item.getStatus() < CrossTaskStatus.STOPED).collect(Collectors.toList());
        if(org.apache.commons.collections4.CollectionUtils.isEmpty(needSync)) {
            return List.of();
        }
        //做多交易所和币对
        Map<String, List<CoinsCrossExchangeArbitrageTaskVo>> longexSymbol = needSync.stream().collect(Collectors.groupingBy(CoinsCrossExchangeArbitrageTaskVo::getLongEx));
        //做空交易所和币对
        Map<String, List<CoinsCrossExchangeArbitrageTaskVo>> shortexSymbol = needSync.stream().collect(Collectors.groupingBy(CoinsCrossExchangeArbitrageTaskVo::getShortEx));

        //先获取所有的account
        Set<Long> accountIds = needSync.stream().map(CoinsCrossExchangeArbitrageTaskVo::getLongAccountId).collect(Collectors.toSet());
        accountIds.addAll(needSync.stream().map(CoinsCrossExchangeArbitrageTaskVo::getShortAccountId).collect(Collectors.toSet()));

        Set<String> exSet = longexSymbol.keySet();
        Set<String> exshort = shortexSymbol.keySet();
        Set<String> allExSet = new HashSet<>(exSet);
        allExSet.addAll(exshort);
        HashMap<String, Set<Account>> exAccount = new HashMap<>();
        HashMap<Long, Account> allAccountMap = new HashMap<>();
        //获取api
        List<CoinsApiVo> allAccounts = coinsApiService.queryByIds(new ArrayList<>(accountIds));
        Map<Long, List<CoinsApiVo>> userAccounts = allAccounts.stream().collect(Collectors.groupingBy(CoinsApiVo::getUserId));
        if (userId != null) {
            List<CoinsApiVo> userAccountList = userAccounts.get(userId);
            Map<String, List<CoinsApiVo>> exAccountMap = userAccountList.stream().collect(Collectors.groupingBy((item)->item.getExchangeName().toLowerCase()));
            for (String ex : allExSet) {

                List<CoinsApiVo> coinsApiVos = exAccountMap.get(ex.toLowerCase());
                for (CoinsApiVo coinsApiVo : coinsApiVos) {
                    Account account = AccountUtils.coverToAccount(coinsApiVo);
                    exAccount.computeIfAbsent(ex, k -> new HashSet<>()).add(account);
                    allAccountMap.put(account.getId(), account);
                }

            }
        } else {
            throw new RuntimeException("unknow user ! please check your login status");
        }
        List<CompletableFuture<OrderPosition>> futures = new ArrayList<>();

        //每条套利任务都只会套利一个币对，一个任务会有两个交易所，以及同一个币对
        //只需要去不同的交易所查指定的币对就行了
        HashMap<String, Set<String>> exAndSymbol = new HashMap<>();
        for (CoinsCrossExchangeArbitrageTaskVo task : needSync) {
            String longEx = task.getLongEx();
            String shortEx = task.getShortEx();

            String symbol = task.getSymbol();
            // 添加 longEx 对应的 symbol
            exAndSymbol.computeIfAbsent(longEx, k -> new HashSet<>()).add(symbol);
            // 添加 shortEx 对应的 symbol
            exAndSymbol.computeIfAbsent(shortEx, k -> new HashSet<>()).add(symbol);
        }


        //查询持仓信息
        for (String ex : allExSet) {
            //循环交易所
            ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
            //循环每个api
            Set<Account> accounts = exAccount.get(ex);
            for (Account account : accounts) {
                if (Objects.nonNull(exchangeService)) {
                    Set<String> symbols = exAndSymbol.get(ex);
                    for (String symbol : symbols) {
                        CompletableFuture<OrderPosition> positionFuture = CompletableFuture.supplyAsync(() -> {
                            PositionParams positionParams = new PositionParams();
                            OrderPosition orderPosition = exchangeService.queryContractPosition(account, symbol, positionParams);
                            return orderPosition;
                        }, exchangeApiManager.getPositionExecutor());
                        futures.add(positionFuture);
                    }
                } else {
                    log.error("不支持的交易所 {}", ex);
                }
            }

        }
        // 包装每个 future，处理异常，避免 join 时抛出异常
        List<CompletableFuture<Optional<OrderPosition>>> safeFutures = futures.stream()
            .map(future -> future.handle((result, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                    // 可以记录日志或打印异常
                    log.error("异常：{}", ex.getMessage());
                    return Optional.<OrderPosition>empty();
                }
                return Optional.ofNullable(result);
            }))
            .collect(Collectors.toList());
        // 等待所有完成
        CompletableFuture.allOf(safeFutures.toArray(new CompletableFuture[0]));

        // 等待完成后统一收集结果
        List<OrderPosition> resultList = safeFutures.stream()
            .map(CompletableFuture::join)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

        Map<String, OrderPosition> exSymbolMap = resultList.stream().collect(Collectors.toMap(item -> item.getEx().toLowerCase() + ":" + item.getSymbol().toLowerCase()+":"+item.getAccountId(), item -> item));

        ArrayList<CoinsCrossExchangeArbitrageTaskVo> needUpdate = new ArrayList<>();
        for (CoinsCrossExchangeArbitrageTaskVo task : needSync) {
            //查询做多持仓
            OrderPosition orderPosition = exSymbolMap.get(task.getLongEx().toLowerCase() + ":" + task.getSymbol().toLowerCase()+":"+task.getLongAccountId());
            if (Objects.nonNull(orderPosition)) {
                if (Objects.nonNull(orderPosition.getSize()) &&
                    orderPosition.getSize().doubleValue() != 0) {
                    //size 等于0 可能是平仓了
                    task.setLongAvgPrice(BigDecimalUtils.toPlainString(orderPosition.getAvgPrice()));
                    task.setLongFundingFee(orderPosition.getFundingFee());
                }
                task.setLongInFee(orderPosition.getFee() == null ? task.getLongInFee() : orderPosition.getFee());
                task.setLongProfit(orderPosition.getProfit() == null ? task.getLongProfit() : orderPosition.getProfit());

                task.setLongSymbolSize(orderPosition.getSize() != null ? new BigDecimal(orderPosition.getSize().toPlainString()) : BigDecimal.ZERO);

                task.setLongMgnRatio(orderPosition.getMgnRatio());
                task.setLongLiqPx(orderPosition.getLiqPx());
                task.setLongPosId(orderPosition.getPositionId());
//                task.setLongLeverage(BigDecimalUtils.toLong(orderPosition.getLever()));
//                task.setLongSymbolSize(BigDecimal.ZERO);
                BigDecimal closePositionProfit = checkClosePositionProfit(userId, task, task.getLongEx(), task.getLongPosId(), orderPosition, allAccountMap.get(task.getLongAccountId()));
                if (Objects.nonNull(closePositionProfit) && closePositionProfit.doubleValue() != 0) {
                    task.setLongProfit(closePositionProfit);
                }


            } else {
                //做多持仓查询不到
                BigDecimal closePositionProfit = checkClosePositionProfit(userId, task, task.getLongEx(), task.getLongPosId(), null, allAccountMap.get(task.getLongAccountId()));
                if (Objects.nonNull(closePositionProfit) && closePositionProfit.doubleValue() != 0) {
                    task.setLongProfit(closePositionProfit);
                }
                task.setLongSymbolSize(BigDecimal.ZERO);
                log.error("多仓查询失败 {} {}", task.getLongEx(), task.getSymbol());
            }
            //查询空仓持仓
            OrderPosition orderPositionshort = exSymbolMap.get(task.getShortEx().toLowerCase() + ":" + task.getSymbol().toLowerCase()+":"+task.getShortAccountId());
            if (Objects.nonNull(orderPositionshort)) {
                if (Objects.nonNull(orderPositionshort.getSize()) &&
                    orderPositionshort.getSize().doubleValue() != 0) {
                    //size 等于0 可能是平仓了
                    task.setShortAvgPrice(BigDecimalUtils.toPlainString(orderPositionshort.getAvgPrice()));
                    task.setShortFundingFee(orderPositionshort.getFundingFee());
                }

                task.setShortInFee(orderPositionshort.getFee() == null ? task.getShortInFee() : orderPositionshort.getFee());
                task.setShortProfit(orderPositionshort.getProfit() == null ? task.getShortProfit() : orderPositionshort.getProfit());
                task.setShortSymbolSize(orderPositionshort.getSize() != null ? new BigDecimal(orderPositionshort.getSize().toPlainString()) : BigDecimal.ZERO);

                task.setShortMgnRatio(orderPositionshort.getMgnRatio());
                task.setShortLiqPx(orderPositionshort.getLiqPx());
                task.setShortPosId(orderPositionshort.getPositionId());
//                task.setShortLeverage(BigDecimalUtils.toLong(orderPositionshort.getLever()));
                BigDecimal closePositionProfit = checkClosePositionProfit(userId, task, task.getShortEx(), task.getShortPosId(), orderPositionshort, allAccountMap.get(task.getShortAccountId()));
                if (Objects.nonNull(closePositionProfit) && closePositionProfit.doubleValue() != 0) {
                    task.setShortProfit(closePositionProfit);
                }
            } else {
                //做空持仓查询不到
                task.setShortSymbolSize(BigDecimal.ZERO);
                BigDecimal closePositionProfit = checkClosePositionProfit(userId, task, task.getShortEx(), task.getLongPosId(), null, allAccountMap.get(task.getShortAccountId()));
                if (Objects.nonNull(closePositionProfit) && closePositionProfit.doubleValue() != 0) {
                    task.setShortProfit(closePositionProfit);
                }
                task.setShortSymbolSize(BigDecimal.ZERO);
                log.error("空仓查询失败 {} {}", task.getShortEx(), task.getSymbol());
            }
            BigDecimal longProfit = task.getLongProfit();
            longProfit = longProfit == null ? BigDecimal.ZERO : longProfit;
            BigDecimal shortProfit = task.getShortProfit();
            shortProfit = shortProfit == null ? BigDecimal.ZERO : shortProfit;
            task.setTotalProfit(longProfit.add(shortProfit));
            needUpdate.add(task);
        }
        if (CollectionUtils.isNotEmpty(needUpdate)) {
            SpringUtils.getBean(this.getClass()).updateVos(needUpdate);
        }
        Map<Long, CoinsCrossExchangeArbitrageTaskVo> syncedMap = needUpdate.stream().collect(Collectors.toMap(CoinsCrossExchangeArbitrageTaskVo::getId, item -> item));
        ArrayList<CoinsCrossExchangeArbitrageTaskVo> result = new ArrayList<>();
        for (CoinsCrossExchangeArbitrageTaskVo row : rows) {
            result.add(syncedMap.getOrDefault(row.getId(), row));
        }
        return result;
    }

    /**
     * 检查持仓是否平仓，平仓就获取平仓盈亏
     *
     * @param userId
     * @param task
     * @param orderPosition
     * @param account
     * @return
     */
    @Nullable
    private BigDecimal checkClosePositionProfit(Long userId, CoinsCrossExchangeArbitrageTaskVo task, String ex, String posId, OrderPosition orderPosition, Account account) {
        if (orderPosition == null || orderPosition.getSize() == null || orderPosition.getSize().compareTo(BigDecimal.ZERO) == 0) {
            //没有仓位了
            if (task.getStatus() == CrossTaskStatus.RUNNING ||
                task.getStatus() == CrossTaskStatus.STOPED) {
                //查询持仓
                List<CoinsOrderVo> closeOrders = coinsOrderService.queryAllOrderByTaskIdAndClosePositionFlag(task.getId(), CrossClosePositionFlag.CLOSE_POSITION_ORDER);
                if (CollectionUtils.isNotEmpty(closeOrders)) {
                    PositionParams positionParams = new PositionParams();
                    Optional<Date> min = closeOrders.stream()
                        .map(CoinsOrderVo::getCreateTime)
                        .min(Date::compareTo);
                    if (min.isPresent()) {
                        positionParams.setStartTime(CommonUtils.getPreviousMinuteTimestamp(15, min.get()));
                        positionParams.setCloseOrderIds(closeOrders.stream().map(CoinsOrderVo::getOrderId).collect(Collectors.toList()));
                        positionParams.setEx(ex);
                        positionParams.setPosId(posId);
                        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
                        if (Objects.nonNull(exchangeService)) {
                            BigDecimal profit = exchangeService.queryClosePositionProfit(account, task.getSymbol()
                                , positionParams);
                            if (Objects.nonNull(profit)) {
                                return profit;
                            }
                        }
                    }

                }
            }
        }
        return null;
    }


    @Override
    public R closePosition(CreateArbitrageTaskVo createArbitrageTaskVo) {
        //平仓，必须两腿下单
        AbTaskFrom from = createArbitrageTaskVo.getFrom();
        CoinFundingInfo argitrageData = createArbitrageTaskVo.getArgitrageData();
        HashMap<String, Object> result = new HashMap<>();
        try {
            ThreadLocalLogUtil.clear();
            if (createArbitrageTaskVo.getBatchIncome() == 1) {
                //分批平仓
                this.createBatchOrder(createArbitrageTaskVo);
                batchOrderTaskService.asyncCheckAndRunBatchOrderTask();
            } else {
                if (Objects.nonNull(createArbitrageTaskVo.getSingleOrder()) && createArbitrageTaskVo.getSingleOrder() == 1) {
                    SingleClosePosition singleClosePosition = new SingleClosePosition();
                    CoinsCrossExchangeArbitrageTaskVo crossTask = this.queryById(createArbitrageTaskVo.getFrom().getTaskId());
                    //单腿平仓
                    if ("buy".equalsIgnoreCase(createArbitrageTaskVo.getSingleOrderSide())) {
                        singleClosePosition.setEx(crossTask.getLongEx());
                        singleClosePosition.setSide(SideType.LONG);
                        singleClosePosition.setSymbol(crossTask.getSymbol());
                        singleClosePosition.setAccountId(crossTask.getLongAccountId());
                        this.onceSideOrderClosePosition(createArbitrageTaskVo.getFrom().getBuy(), crossTask.getId(), singleClosePosition);
                    } else if ("sell".equalsIgnoreCase(createArbitrageTaskVo.getSingleOrderSide())) {
                        singleClosePosition.setEx(crossTask.getShortEx());
                        singleClosePosition.setSide(SideType.SHORT);
                        singleClosePosition.setSymbol(crossTask.getSymbol());
                        singleClosePosition.setAccountId(crossTask.getShortAccountId());
                        this.onceSideOrderClosePosition(createArbitrageTaskVo.getFrom().getSell(), crossTask.getId(), singleClosePosition);
                    }


                } else {
                    this.oncePlace2ExOrderClosePosition(from);
                }
            }
//            this.oncePlace2ExOrderClosePosition(from);
            //获取日志
            List<String> logs = ThreadLocalLogUtil.getLogs();
            result.put("logs", logs);
            result.put("success", true);
            return R.ok("下单成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            List<String> logs = ThreadLocalLogUtil.getLogs();
            result.put("logs", logs);
            result.put("success", false);
            return R.ok(e.getMessage(), result);
        } finally {
            ThreadLocalLogUtil.clear();
        }
    }

    @Override
    public List<CoinsCrossExchangeArbitrageTaskVo> queryHandleList() {
        List<Long> handleStatus = List.of(CrossTaskStatus.RUNNING, CrossTaskStatus.WAIT_ORDER_DEAL, CrossTaskStatus.CREATED);
        Wrapper<CoinsCrossExchangeArbitrageTask> queryWrapper = new LambdaQueryWrapper<CoinsCrossExchangeArbitrageTask>()
            .in(CoinsCrossExchangeArbitrageTask::getStatus, handleStatus);

        return baseMapper.selectVoList(queryWrapper);
    }


    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateVos(List<CoinsCrossExchangeArbitrageTaskVo> tasks) {
        for (CoinsCrossExchangeArbitrageTaskVo task : tasks) {
            CoinsCrossExchangeArbitrageTask exchangeArbitrageTask = new CoinsCrossExchangeArbitrageTask();
            BeanUtils.copyProperties(task, exchangeArbitrageTask);
            LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateWrapper = UpdateWrapperBuilder.buildNonNullUpdateWrapper(exchangeArbitrageTask, CoinsCrossExchangeArbitrageTask::getId);
            baseMapper.update(exchangeArbitrageTask, updateWrapper);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateFee(List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos) {
        log.info("手续费计算任务开始");
        long startTimeStramp = System.currentTimeMillis();

        Set<Long> taskIds = coinsCrossExchangeArbitrageTaskVos.stream().map(CoinsCrossExchangeArbitrageTaskVo::getId).collect(Collectors.toSet());
        List<CoinsOrderVo> endOrders = coinsOrderService.queryAllOrderByTaskIds(new ArrayList<>(taskIds));
        Map<Long, List<CoinsOrderVo>> taskOrders = endOrders.stream().filter(item->item.getTaskId() != null).collect(Collectors.groupingBy(CoinsOrderVo::getTaskId));
        for (CoinsCrossExchangeArbitrageTaskVo taskVo : coinsCrossExchangeArbitrageTaskVos) {
            List<CoinsOrderVo> taskEndOrders = taskOrders.getOrDefault(taskVo.getId(), new ArrayList<>());
            BigDecimal totalLongInFee = BigDecimal.ZERO;
            BigDecimal totalLongOutFee = BigDecimal.ZERO;
            BigDecimal totalShortInFee = BigDecimal.ZERO;
            BigDecimal totalShortOutFee = BigDecimal.ZERO;

            Optional<BigDecimal> longfeeAll = taskEndOrders.stream().filter(item -> item.getSide().equalsIgnoreCase(SideType.LONG)
                    && CrossClosePositionFlag.NORMAL_ORDER == item.getClosePositionOrder()).map(CoinsOrderVo::getFee).filter(NumberUtils::isParsable)
                .map(BigDecimal::new).reduce(BigDecimal::add);
            if (longfeeAll.isPresent()) {
                totalLongInFee = longfeeAll.get();
            }

            Optional<BigDecimal> shortfeeAll = taskEndOrders.stream().filter(item -> item.getSide().equalsIgnoreCase(SideType.SHORT)
                    && CrossClosePositionFlag.NORMAL_ORDER == item.getClosePositionOrder()).map(CoinsOrderVo::getFee).filter(NumberUtils::isParsable)
                .map(BigDecimal::new).reduce(BigDecimal::add);
            if (shortfeeAll.isPresent()) {
                totalShortInFee = shortfeeAll.get();
            }
            Optional<BigDecimal> longOutfeeAll = taskEndOrders.stream().filter(item -> item.getSide().equalsIgnoreCase(SideType.LONG)
                    && CrossClosePositionFlag.CLOSE_POSITION_ORDER == item.getClosePositionOrder()).map(CoinsOrderVo::getFee).filter(NumberUtils::isParsable)
                .map(BigDecimal::new).reduce(BigDecimal::add);
            if (longOutfeeAll.isPresent()) {
                totalLongOutFee = longOutfeeAll.get();
            }

            Optional<BigDecimal> shortOutfeeAll = taskEndOrders.stream().filter(item -> item.getSide().equalsIgnoreCase(SideType.SHORT)
                    && CrossClosePositionFlag.CLOSE_POSITION_ORDER == item.getClosePositionOrder()).map(CoinsOrderVo::getFee).filter(NumberUtils::isParsable)
                .map(BigDecimal::new).reduce(BigDecimal::add);
            if (shortOutfeeAll.isPresent()) {
                totalShortOutFee = shortOutfeeAll.get();
            }


            LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> update = new LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask>()
                .eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId())
                .set(CoinsCrossExchangeArbitrageTask::getLongInFee, totalLongInFee)
                .set(CoinsCrossExchangeArbitrageTask::getShortInFee, totalShortInFee)
                .set(CoinsCrossExchangeArbitrageTask::getLongOutFee, totalLongOutFee)
                .set(CoinsCrossExchangeArbitrageTask::getShortOutFee, totalShortOutFee);
            baseMapper.update(update);
            log.info("更新用户 {} 任务 {} 做多开仓手续费 {} 做空开仓手续费 {} 做多平仓手续费 {}  做空平仓手续费 {}", taskVo.getUserId(),
                taskVo.getSymbol(), totalLongInFee, totalShortInFee, totalLongOutFee, totalShortOutFee);
        }

        log.info("手续费计算任务解锁，耗时 {}ms", System.currentTimeMillis() - startTimeStramp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateFee() {
        log.info("手续费计算任务开始");
        long startTimeStramp = System.currentTimeMillis();
        //先查运行中的仓位task
        List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = this.queryHandleList();
        this.updateFee(coinsCrossExchangeArbitrageTaskVos);

        log.info("手续费计算任务解锁，耗时 {}ms", System.currentTimeMillis() - startTimeStramp);

    }

    @Override
    public R syncTask(Long taskId) {
//        RLock lock = redissonClient.getLock(LockConstant.POSITION_SYNC_LOCK);
//        lock.lock(60, TimeUnit.SECONDS);
//        try {
//            CoinsCrossExchangeArbitrageTaskVo coinsCrossExchangeArbitrageTaskVo = this.queryById(taskId);
//
//            taskService.startWsSocket(List.of(coinsCrossExchangeArbitrageTaskVo));
//
//            List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = this.syncPosition(List.of(coinsCrossExchangeArbitrageTaskVo), coinsCrossExchangeArbitrageTaskVo.getUserId());
//
//            updateFee(coinsCrossExchangeArbitrageTaskVos);
//        } finally {
//            lock.unlock();
//        }


        return R.ok("同步成功");
    }

    @Override
    public List<CoinsCrossExchangeArbitrageTaskVo> formateSize(List<CoinsCrossExchangeArbitrageTaskVo> rows, Long userId) {
        for (CoinsCrossExchangeArbitrageTaskVo row : rows) {
            ExchangeService longExService = exchangeApiManager.getExchangeService(row.getLongEx());
            ExchangeService shortExService = exchangeApiManager.getExchangeService(row.getShortEx());
            row.setLongSymbolSize(longExService.calcShowSize(row.getSymbol(), row.getLongSymbolSize()));
            row.setShortSymbolSize(shortExService.calcShowSize(row.getSymbol(), row.getShortSymbolSize()));
        }
        return rows;
    }

    @Override
    public CoinsCrossExchangeArbitrageTaskVo formateSize(CoinsCrossExchangeArbitrageTaskVo data, Long userId) {
        ExchangeService longExService = exchangeApiManager.getExchangeService(data.getLongEx());
        ExchangeService shortExService = exchangeApiManager.getExchangeService(data.getShortEx());
        data.setLongSymbolSize(longExService.calcShowSize(data.getSymbol(), data.getLongSymbolSize()));
        data.setShortSymbolSize(shortExService.calcShowSize(data.getSymbol(), data.getShortSymbolSize()));
        return data;
    }

    @Override
    public CoinsCrossExchangeArbitrageTaskMapper getBaseMapper() {
        return baseMapper;
    }

    public static void main(String[] args) {
        CompletableFuture<Integer> shortExOrderFuture = CompletableFuture.supplyAsync(() -> {

            return 1 / 0;
        });
        Integer join = shortExOrderFuture.join();
        System.out.println(join);
    }
}
