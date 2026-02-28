package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.constant.AbBotStatusConstant;
import com.bitstrat.constant.CoinsConstant;
import com.bitstrat.constant.CrossTaskStatus;
import com.bitstrat.constant.LockConstant;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bo.CoinsAbBotBo;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.vo.*;
import com.bitstrat.mapper.CoinsAbBotMapper;
import com.bitstrat.service.*;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.BigDecimalUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 套利机器人Service业务层处理
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Service
@Slf4j
public class CoinsAbBotServiceImpl implements ICoinsAbBotService {

    @Autowired
    private ExchangeApiManager exchangeApiManager;
    @Autowired
    private ICoinsApiService coinsApiService;
    @Autowired
    private CoinsAbBotMapper baseMapper;
    @Autowired
    private ICoinsBotAccountService coinsBotAccountService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeTaskService;

    @Autowired
    @Lazy
    private BatchOrderTaskService batchOrderTaskService;

    @Autowired
    private ICoinsNotifyService coinsNotifyService;

    @Autowired
    private ExecuteService executeService;

    /**
     * 查询套利机器人
     *
     * @param id 主键
     * @return 套利机器人
     */
    @Override
    public CoinsAbBotVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询套利机器人列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 套利机器人分页列表
     */
    @Override
    public TableDataInfo<CoinsAbBotVo> queryPageList(CoinsAbBotBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsAbBot> lqw = buildQueryWrapper(bo);
        Page<CoinsAbBotVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的套利机器人列表
     *
     * @param bo 查询条件
     * @return 套利机器人列表
     */
    @Override
    public List<CoinsAbBotVo> queryList(CoinsAbBotBo bo) {
        LambdaQueryWrapper<CoinsAbBot> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsAbBot> buildQueryWrapper(CoinsAbBotBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsAbBot> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsAbBot::getId);
        lqw.like(StringUtils.isNotBlank(bo.getBotName()), CoinsAbBot::getBotName, bo.getBotName());
        lqw.eq(bo.getAbPercentThreshold() != null, CoinsAbBot::getAbPercentThreshold, bo.getAbPercentThreshold());
        lqw.eq(bo.getTriggerMinutes() != null, CoinsAbBot::getTriggerMinutes, bo.getTriggerMinutes());
        lqw.eq(bo.getMinVolume() != null, CoinsAbBot::getMinVolume, bo.getMinVolume());
        lqw.eq(bo.getLeverage() != null, CoinsAbBot::getLeverage, bo.getLeverage());
        lqw.eq(bo.getMinSize() != null, CoinsAbBot::getMinSize, bo.getMinSize());
        lqw.eq(bo.getMaxSize() != null, CoinsAbBot::getMaxSize, bo.getMaxSize());
        lqw.eq(bo.getBatchSize() != null, CoinsAbBot::getBatchSize, bo.getBatchSize());
        lqw.eq(bo.getStatus() != null, CoinsAbBot::getStatus, bo.getStatus());
        lqw.eq(bo.getUserId() != null, CoinsAbBot::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增套利机器人
     *
     * @param bo 套利机器人
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsAbBotBo bo) {
        CoinsAbBot add = MapstructUtils.convert(bo, CoinsAbBot.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改套利机器人
     *
     * @param bo 套利机器人
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsAbBotBo bo) {
        CoinsAbBot update = MapstructUtils.convert(bo, CoinsAbBot.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsAbBot entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除套利机器人信息
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
    public List<CoinsAbBotVo> queryBotByStatus(List<Long> status) {
        LambdaQueryWrapper<CoinsAbBot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CoinsAbBot::getStatus, status);
        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public void startPosition(CoinsAbBotVo coinsAbBotVo, CoinFundingInfo coinFundingInfo) {
        RLock lock = redissonClient.getLock(LockConstant.AB_BOT_POSITION + ":" + coinsAbBotVo.getId());
        lock.lock();
        try {
            coinsAbBotVo = this.queryById(coinsAbBotVo.getId());
            if (!Objects.equals(coinsAbBotVo.getStatus(), AbBotStatusConstant.RUNNING)) {
                //不是运行中状态，不开仓
                log.info("任务：{} 非运行中状态，不进行开仓计算", coinsAbBotVo.getId());
                return;
            }

            List<CoinsApiVo> coinsApiVos = coinsBotAccountService.selectRelatedByBotId(coinsAbBotVo.getId());
            Map<String, List<CoinsApiVo>> exApis = coinsApiVos.stream().collect(Collectors.groupingBy(item -> item.getExchangeName().toLowerCase()));

            //先判断两个交易所下次结算时间差距大小
            String buyExchangeName = coinFundingInfo.getBuy().getExchangeName();
            String sellExchangeName = coinFundingInfo.getSell().getExchangeName();
            String symbol = coinFundingInfo.getSymbol();
            ExchangeService buyExchangeService = exchangeApiManager.getExchangeService(buyExchangeName.toLowerCase());
            ExchangeService sellExchangeService = exchangeApiManager.getExchangeService(sellExchangeName.toLowerCase());

            List<CoinsApiVo> buyApis = exApis.getOrDefault(buyExchangeName.toLowerCase(), new ArrayList<>());
            if (CollectionUtils.isEmpty(buyApis)) {
                throw new RuntimeException("指定交易所币对未配置API！");
            }
            CoinsApiVo coinsApiVo = buyApis.stream().findAny().get();
            Account buyAccount = AccountUtils.coverToAccount(coinsApiVo);


            List<CoinsApiVo> sellApis = exApis.getOrDefault(sellExchangeName.toLowerCase(), new ArrayList<>());
            if (CollectionUtils.isEmpty(sellApis)) {
                throw new RuntimeException("指定交易所币对未配置API！");
            }
            CoinsApiVo sellApiVo = sellApis.stream().findAny().get();
            Account sellAccount = AccountUtils.coverToAccount(sellApiVo);


            SymbolFundingRate buyFundingRate = buyExchangeService.getSymbolFundingRate(symbol);
            SymbolFundingRate sellFundingRate = buyExchangeService.getSymbolFundingRate(symbol);

            //判断两个交易所的资金费结算时间误差是否相差30分钟,并且判断两个时间戳是否都是毫秒，如不是，自动按毫秒补全并对比
            if (BigDecimalUtils.isFundingTimeGapWithin30Minutes(buyFundingRate.getNextFundingTime(), sellFundingRate.getNextFundingTime())) {
                //在30分钟内，触发开仓
                //计算两个API账户可用余额，找到最低的余额进行计算
                AccountBalance buyBalance = buyExchangeService.getBalance(buyAccount, CoinsConstant.USDT);
                AccountBalance sellBalance = sellExchangeService.getBalance(sellAccount, CoinsConstant.USDT);

                BigDecimal min = buyBalance.getFreeBalance().min(sellBalance.getFreeBalance());
                if (min.compareTo(coinsAbBotVo.getMinSize()) <= 0) {
                    //不满足最低开仓余额
                    log.error("任务ID：{} 账户最低余额：{} 不满足预设最低开仓余额:{}", coinsAbBotVo.getId()
                        , min, coinsAbBotVo.getMinSize());
                    return;
                }
                // 第一步：计算最大可开 USDT（减去 10U）
                BigDecimal maxCanOpen = min.min(coinsAbBotVo.getMaxSize()).subtract(BigDecimal.TEN);

                // 第二步：获取单批开仓金额
                BigDecimal batchSizeUsdt = coinsAbBotVo.getBatchSize();

                // 第三步：计算总批次数（向下取整）
                BigDecimal totalBatchCount = maxCanOpen.divide(batchSizeUsdt, 0, RoundingMode.DOWN);

                // 第四步：计算每批占比百分比 = 100 / totalBatchCount（保留两位小数）
                BigDecimal percentPerBatch = BigDecimal.ZERO;
                if (totalBatchCount.compareTo(BigDecimal.ZERO) > 0) {
                    percentPerBatch = new BigDecimal("100")
                        .divide(totalBatchCount, 2, RoundingMode.HALF_UP);
                }

                if (percentPerBatch.compareTo(BigDecimal.ZERO) == 0) {
                    log.error("计算每单百分比错误！");
                    Long userId = coinsAbBotVo.getUserId();
                    executeService.getNotifyExecute().submit(() -> {
                            coinsNotifyService.sendNotification(userId, "计算开仓百分比失败！！开仓失败");
                        }
                    );
                    return;
                }
                //创建批量开仓对象
                CreateArbitrageTaskVo createArbitrageTaskVo = new CreateArbitrageTaskVo();
                AbTaskFrom abTaskFrom = new AbTaskFrom();
                abTaskFrom.setSide(1);
                //查询两边合约详情
                CoinContractInfomation buyContractInfo = buyExchangeService.getContractCoinInfo(buyAccount, symbol);
                CoinContractInfomation sellContractInfo = sellExchangeService.getContractCoinInfo(sellAccount, symbol);
                //找到两边最少允许计算的数量小数位数
                int sizeCalcPlaces = Math.min(buyContractInfo.getCalcPlaces(), sellContractInfo.getCalcPlaces());
                BigDecimal buyPrice = buyExchangeService.getNowPrice(buyAccount, symbol);
                BigDecimal sellPrice = sellExchangeService.getNowPrice(buyAccount, symbol);
                // 计算平均值：(buyPrice + sellPrice) / 2
                BigDecimal avg = buyPrice.add(sellPrice).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

                // 获取两个价格中小数位最少的精度
                int buyScale = buyPrice.stripTrailingZeros().scale();
                int sellScale = sellPrice.stripTrailingZeros().scale();
                int targetScale = Math.min(Math.max(buyScale, 0), Math.max(sellScale, 0));

                // 按照最小小数位数保留,平均价格
                BigDecimal finalAvg = avg.setScale(targetScale, RoundingMode.HALF_UP);

                //拿到平均价格，根据市价和允许开仓的USDT数量计算size
                BigDecimal maxSize = maxCanOpen.divide(finalAvg, sizeCalcPlaces, RoundingMode.HALF_UP);
                //开多一方
                ArbitrageFormData longForm = new ArbitrageFormData();
                longForm.setAccountId(buyAccount.getId());
                longForm.setLeverage(coinsAbBotVo.getLeverage());
                longForm.setSize(maxSize);
                longForm.setOrderType(coinsAbBotVo.getOrderType().toLowerCase());


                //开空一方
                ArbitrageFormData shortFrom = new ArbitrageFormData();
                shortFrom.setAccountId(sellAccount.getId());
                shortFrom.setLeverage(coinsAbBotVo.getLeverage());
                shortFrom.setSize(maxSize);
                shortFrom.setOrderType(coinsAbBotVo.getOrderType().toLowerCase());
                AbTaskFrom from = new AbTaskFrom();
                from.setBuy(longForm);
                from.setSell(shortFrom);
                from.setSide(1);


                //先建立任务
                CreateArbitrageTaskVo taskVo = new CreateArbitrageTaskVo();
                taskVo.setFrom(from);
                taskVo.setBatchIncome(1);
                taskVo.setArgitrageData(coinFundingInfo);
                R task = coinsCrossExchangeTaskService.createTask(taskVo);
                if (!task.isSuccess()) {
                    //创建失败
                    throw new RuntimeException("创建任务失败");
                }
                from.setTaskId(taskVo.getTaskBo().getId());
                createArbitrageTaskVo.setFrom(from);
                createArbitrageTaskVo.setBatchIncome(1);
                createArbitrageTaskVo.setBatchSize(percentPerBatch.doubleValue());
                coinsCrossExchangeTaskService.createBatchOrder(createArbitrageTaskVo);
                batchOrderTaskService.asyncCheckAndRunBatchOrderTask();
                LambdaUpdateWrapper<CoinsAbBot> botWrapper = new LambdaUpdateWrapper<>();
                botWrapper.eq(CoinsAbBot::getId, buyAccount.getId());
                botWrapper.set(CoinsAbBot::getStatus, AbBotStatusConstant.CREATE_POSITION)
                    .set(CoinsAbBot::getAvaliableTaskId, taskVo.getTaskBo().getId());
                this.baseMapper.update(botWrapper);
                sendCreatePositionMsg(coinsAbBotVo, createArbitrageTaskVo, coinFundingInfo, buyBalance, sellBalance,
                    buyPrice, sellPrice);
            } else {
                log.error("两个交易所{} {} 币对：{} 下次资金费结算时间差距过大！(超过30分钟)", coinFundingInfo.getBuy().getExchangeName()
                    , coinFundingInfo.getSell().getExchangeName()
                    , coinFundingInfo.getSymbol());
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 发送建仓消息
     *
     * @param coinsAbBot
     * @param createArbitrageTaskVo
     * @param coinFundingInfo
     */
    private void sendCreatePositionMsg(CoinsAbBotVo coinsAbBot, CreateArbitrageTaskVo createArbitrageTaskVo, CoinFundingInfo coinFundingInfo
        , AccountBalance buyBalance, AccountBalance sellBalance, BigDecimal buyPrice, BigDecimal sellPrice) {
        String template = """
            ❎自动建仓提示
            币对：%s
            做多交易所：%s (现价:%s)
            做多余额：%s
            做多数量：%s

            做空交易所：%s (现价:%s)
            做空余额：%s
            做空数量：%s

            年化收益率：%s %%
            """;
        String formatted = template.formatted(
            coinFundingInfo.getSymbol(),

            coinFundingInfo.getBuy().getExchangeName(),
            buyPrice,
            buyBalance.getEquity(),
            createArbitrageTaskVo.getFrom().getBuy().getSize(),

            coinFundingInfo.getSell().getExchangeName(),
            sellPrice,
            sellBalance.getEquity(),
            createArbitrageTaskVo.getFrom().getSell().getSize()
        );
        executeService.getNotifyExecute().submit(() -> {
            coinsNotifyService.sendNotification(coinsAbBot.getUserId(), formatted);
        });


    }

    /**
     * 发送平仓消息
     *
     * @param coinsAbBot
     * @param createArbitrageTaskVo
     */
    private void sendClosePositionMsg(CoinsAbBotVo coinsAbBot, CreateArbitrageTaskVo createArbitrageTaskVo,CoinsCrossExchangeArbitrageTaskVo task,  BigDecimal buyPrice, BigDecimal sellPrice) {
        String template = """
            ⚠ 自动平仓提示
            币对：%s
            做多交易所：%s (现价:%s)
            做多数量：%s

            做空交易所：%s (现价:%s)
            做空数量：%s

            年化收益率：%s %%
            """;
        String formatted = template.formatted(
            task.getSymbol(),

            task.getLongEx(),
            buyPrice,
            createArbitrageTaskVo.getFrom().getBuy().getSize(),
            task.getShortEx(),
            sellPrice,
            createArbitrageTaskVo.getFrom().getSell().getSize()
        );
        executeService.getNotifyExecute().submit(() -> {
            coinsNotifyService.sendNotification(coinsAbBot.getUserId(), formatted);
        });

    }

    /**
     * 分批平仓
     */
    @Override
    public void closePosition(CoinsAbBotVo coinsAbBotVo, CoinsCrossExchangeArbitrageTaskVo vo) {
        RLock lock = redissonClient.getLock(LockConstant.AB_BOT_POSITION + ":" + coinsAbBotVo.getId());
        lock.lock();
        try {
            coinsAbBotVo = this.queryById(coinsAbBotVo.getId());
            if (Objects.equals(coinsAbBotVo.getStatus(), AbBotStatusConstant.HOLD)) {
                Long avaliableTaskId = coinsAbBotVo.getAvaliableTaskId();
                CoinsCrossExchangeArbitrageTaskVo taskVo = coinsCrossExchangeTaskService.queryById(avaliableTaskId);

                String symbol = taskVo.getSymbol();
                ExchangeService buyExchangeService = exchangeApiManager.getExchangeService(taskVo.getLongEx().toLowerCase());
                ExchangeService sellExchangeService = exchangeApiManager.getExchangeService(taskVo.getShortEx().toLowerCase());
                CoinsApiVo longAccountVo = coinsApiService.queryById(taskVo.getLongAccountId());
                CoinsApiVo shortAccountVo = coinsApiService.queryById(taskVo.getShortAccountId());
                Account buyAccount = AccountUtils.coverToAccount(longAccountVo);
                Account sellAccount = AccountUtils.coverToAccount(shortAccountVo);

                OrderPosition orderPositionLong = buyExchangeService.queryContractPosition(buyAccount, taskVo.getSymbol(), new PositionParams());
                OrderPosition orderPositionShort = sellExchangeService.queryContractPosition(sellAccount, taskVo.getSymbol(), new PositionParams());

                if (orderPositionLong.getSize().compareTo(orderPositionShort.getSize()) != 0) {
                    //两边持仓数量不一致，报警，手动平仓
                    log.error("数量不一致，无法触发双腿平仓");
                    Long userId = coinsAbBotVo.getUserId();
                    executeService.getNotifyExecute().submit(() -> {
                        coinsNotifyService.sendNotification(userId, "持仓数量不一致！做多持仓:" + orderPositionLong.getSize() + " 做空持仓:" + orderPositionShort.getSize()
                            + "\n请手动平仓！");
                    });
                    return;
                }


                //已持仓，进行平仓
                //创建批量开仓对象
                CreateArbitrageTaskVo createArbitrageTaskVo = new CreateArbitrageTaskVo();
                AbTaskFrom abTaskFrom = new AbTaskFrom();
                abTaskFrom.setSide(1);
                //查询两边合约详情
                CoinContractInfomation buyContractInfo = buyExchangeService.getContractCoinInfo(buyAccount, symbol);
                CoinContractInfomation sellContractInfo = sellExchangeService.getContractCoinInfo(sellAccount, symbol);
                //找到两边最少允许计算的数量小数位数
                int sizeCalcPlaces = Math.min(buyContractInfo.getCalcPlaces(), sellContractInfo.getCalcPlaces());
                BigDecimal buyPrice = buyExchangeService.getNowPrice(buyAccount, symbol);
                BigDecimal sellPrice = sellExchangeService.getNowPrice(buyAccount, symbol);
                // 计算平均值：(buyPrice + sellPrice) / 2
                BigDecimal avg = buyPrice.add(sellPrice).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

                //这是每单最小数量
                BigDecimal orderSize = coinsAbBotVo.getMinSize().divide(avg, sizeCalcPlaces, RoundingMode.HALF_UP);
                //总计需要平的数量
                BigDecimal minSize = orderPositionLong.getSize().min(orderPositionShort.getSize());

                //根据每单最小数量，和总计需要平的数量，计算出每单最小数量在总计平仓数量的占比百分多少
                BigDecimal percentPerBatch = BigDecimal.ZERO;
                if (minSize.compareTo(BigDecimal.ZERO) > 0) {
                    percentPerBatch = orderSize
                        .divide(minSize, 6, RoundingMode.HALF_UP) // 先多保留位数避免精度误差
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP); // 最终保留两位小数
                }
                if (percentPerBatch.compareTo(BigDecimal.ZERO) == 0) {
                    log.error("计算每单百分比错误！");
                    Long userId = coinsAbBotVo.getUserId();
                    executeService.getNotifyExecute().submit(() -> {
                            coinsNotifyService.sendNotification(userId, "计算开仓百分比失败！！做多持仓:" + orderPositionLong.getSize() + " 做空持仓:" + orderPositionShort.getSize()
                                + "\n请手动平仓！");
                        }
                    );
                    return;
                }


                // 获取两个价格中小数位最少的精度
                int buyScale = buyPrice.stripTrailingZeros().scale();
                int sellScale = sellPrice.stripTrailingZeros().scale();
                int targetScale = Math.min(Math.max(buyScale, 0), Math.max(sellScale, 0));

                //开多一方
                ArbitrageFormData longForm = new ArbitrageFormData();
                longForm.setAccountId(buyAccount.getId());
                longForm.setLeverage(coinsAbBotVo.getLeverage());
                longForm.setSize(minSize);
                longForm.setOrderType(coinsAbBotVo.getOrderType().toLowerCase());


                //开空一方
                ArbitrageFormData shortFrom = new ArbitrageFormData();
                shortFrom.setAccountId(sellAccount.getId());
                shortFrom.setLeverage(coinsAbBotVo.getLeverage());
                shortFrom.setSize(minSize);
                shortFrom.setOrderType(coinsAbBotVo.getOrderType().toLowerCase());
                AbTaskFrom from = new AbTaskFrom();
                from.setBuy(longForm);
                from.setSell(shortFrom);
                from.setSide(2);

                from.setTaskId(taskVo.getId());
                createArbitrageTaskVo.setFrom(from);
                createArbitrageTaskVo.setBatchIncome(1);
                createArbitrageTaskVo.setBatchSize(percentPerBatch.doubleValue());
                coinsCrossExchangeTaskService.createBatchOrder(createArbitrageTaskVo);
                batchOrderTaskService.asyncCheckAndRunBatchOrderTask();
                LambdaUpdateWrapper<CoinsAbBot> botWrapper = new LambdaUpdateWrapper<>();
                botWrapper.eq(CoinsAbBot::getId, buyAccount.getId());
                botWrapper.set(CoinsAbBot::getStatus, AbBotStatusConstant.CLOSE_POSITION)
                    .set(CoinsAbBot::getAvaliableTaskId, taskVo.getId());
                this.baseMapper.update(botWrapper);
                sendClosePositionMsg(coinsAbBotVo,createArbitrageTaskVo,taskVo,
                    buyPrice,sellPrice);

            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void updateStatusById(Long botId, Long status) {
        LambdaUpdateWrapper<CoinsAbBot> updateWrapper = new LambdaUpdateWrapper<CoinsAbBot>().eq(CoinsAbBot::getId, botId)
            .set(CoinsAbBot::getStatus, status);
        this.baseMapper.update(updateWrapper);

    }

    @Override
    public void checkCloseStatus(CoinsAbBotVo coinsAbBotVo) {
        Long avaliableTaskId = coinsAbBotVo.getAvaliableTaskId();
        CoinsCrossExchangeArbitrageTaskVo coinsCrossExchangeArbitrageTaskVo = coinsCrossExchangeTaskService.queryById(avaliableTaskId);
        if(Objects.nonNull(coinsCrossExchangeArbitrageTaskVo)){
            Long status = coinsCrossExchangeArbitrageTaskVo.getStatus();
            if (status == CrossTaskStatus.CLOSED) {
                //已平仓，需要修改为running
                this.updateStatusById(avaliableTaskId, AbBotStatusConstant.RUNNING);
            }
        }
    }

}
