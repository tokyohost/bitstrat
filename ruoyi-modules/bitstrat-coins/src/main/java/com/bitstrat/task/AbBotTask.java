package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.bitstrat.constant.AbBotStatusConstant;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.FundFeeTask;
import com.bitstrat.domain.TaskBalance;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.vo.CoinsAbBotVo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.service.*;
import com.bitstrat.service.impl.FundingFeeService;
import com.bitstrat.strategy.fundingFee.FundingFeeStrategy;
import com.bitstrat.utils.BigDecimalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/26 17:18
 * @Content 合约套利机器人定时任务
 */
@Component
@Slf4j
@JobExecutor(name = "AbBotTask")
public class AbBotTask {
    @Autowired
    ICoinsAbBotService coinsAbBotService;
    @Autowired
    ICoinGlassService coinGlassService;
    @Autowired
    ICoinsBotAccountService coinsBotAccountService;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    FundingFeeService fundingFeeService;

    @Autowired
    ICoinsNotifyService coinsNotifyService;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始套利机器人定时任务");
        long startTimeStamp = System.currentTimeMillis();
        this.checkAbBot();
        SnailJobLog.LOCAL.info("结束套利机器人定时任务 耗时 {} ms",System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("套利机器人定时执行成功");
    }

    private void checkAbBot() {


        //检查没有持仓的正在运行中的机器人是否存在套利机会
        List<CoinsAbBotVo> coinsAbBotVos = coinsAbBotService.queryBotByStatus(List.of(AbBotStatusConstant.RUNNING));
        for (CoinsAbBotVo coinsAbBotVo : coinsAbBotVos) {
            checkBotAbSymbol(coinsAbBotVo);
        }


        //先检查已经持仓的是否还有套利空间
        List<CoinsAbBotVo> holdCoinsAbBotVos = coinsAbBotService.queryBotByStatus(List.of(AbBotStatusConstant.HOLD));
        for (CoinsAbBotVo coinsAbBotVo : holdCoinsAbBotVos) {
            checkBotAbHold(coinsAbBotVo);
        }


        //检查在平仓状态、hold状态任务对应的持仓是否平仓，平仓需要修改为running
        List<CoinsAbBotVo> needCheckClose = coinsAbBotService.queryBotByStatus(List.of(AbBotStatusConstant.CLOSE_POSITION, AbBotStatusConstant.HOLD));
        for (CoinsAbBotVo coinsAbBotVo : needCheckClose) {
            checkCloseStatus(coinsAbBotVo);
        }




    }


    private void checkCloseStatus(CoinsAbBotVo coinsAbBotVo) {
        coinsAbBotService.checkCloseStatus(coinsAbBotVo);
    }

    private void checkBotAbHold(CoinsAbBotVo coinsAbBotVo) {
        if (Objects.isNull(coinsAbBotVo.getAvaliableTaskId())) {
            //没有具体仓位
            log.error("任务：{} 状态为已持仓，但没有具体持仓Task ID！",coinsAbBotVo.getId());
            return;
        }
        CoinsCrossExchangeArbitrageTaskVo vo = coinsCrossExchangeArbitrageTaskService.queryById(coinsAbBotVo.getAvaliableTaskId());
        // 判断是否资金费倒挂
        boolean isFundingFeeInversion = fundingFeeService.checkCrossExchangFundingFeeInversion(vo);
        if (isFundingFeeInversion ) {
            //资金费倒挂，自动平仓
            FundFeeTask fundFeeTask = fundingFeeService.queryFundingFeeByTask(vo);
            TaskBalance taskBalance = fundingFeeService.queryBalanceByTask(vo);
            BigDecimal apy = fundFeeTask.getApy()
                .multiply(BigDecimal.valueOf(100))
                .setScale(5, RoundingMode.HALF_UP);
            if (coinsAbBotVo.getMinAllowPercent().compareTo(apy) >= 0) {
                //暂时不满足自动平仓预警
                log.info("任务Id:{} 暂时不满足自动平仓预警，预设最低收益率{} 当前收益率 {}",coinsAbBotVo.getId(),coinsAbBotVo.getMinAllowPercent(),apy);
                return;
            }

            String template = """
                    🚫自动平仓预警🚫
                    币对：%s
                    做多交易所：%s (现价:%s)
                    做多余额：%s
                    做多资金费率：%s %%
                    做多资金费下次结算时间：%s

                    做空交易所：%s (现价:%s)
                    做空余额：%s
                    做空资金费率：%s %%
                    做空资金费下次结算时间：%s

                    资金费盈利空间：%s %%
                    年化收益率：%s %%
                    超出最低允许收益率 %s %%
                    """;

            BigDecimal longFee = BigDecimalUtils.formatPercent(fundFeeTask.getLongFundingFee());

            BigDecimal shortFee = BigDecimalUtils.formatPercent(fundFeeTask.getShortFundingFee());

            BigDecimal profit = fundFeeTask.getFundingFeeCanMake()
                .multiply(BigDecimal.valueOf(100))
                .setScale(5, RoundingMode.HALF_UP);



            String longNextTime = dtf.format(fundFeeTask.getLongNextFundingTime());
            String shortNextTime = dtf.format(fundFeeTask.getShortNextFundingTime());

            String message = String.format(
                template,
                vo.getSymbol(),
                vo.getLongEx(),
                fundFeeTask.getLongMarketPrice(),
                taskBalance.getLongBalance(),
                longFee,
                longNextTime,
                vo.getShortEx(),
                fundFeeTask.getShortMarketPrice(),
                taskBalance.getShortBalance(),
                shortFee,
                shortNextTime,
                profit,
                apy
            );


            coinsNotifyService.sendNotification(vo.getUserId(), message);

            log.info("任务ID:{} 币对：{} 年华：{} 准备平仓",coinsAbBotVo.getId(),vo.getSymbol(),apy);
            coinsAbBotService.closePosition(coinsAbBotVo,vo);

        }


    }

    /**
     * 检查配置的api中是否有套利机会
     * @param coinsAbBotVo
     */
    private void checkBotAbSymbol(CoinsAbBotVo coinsAbBotVo) {
        List<CoinsApiVo> coinsApiVos = coinsBotAccountService.selectRelatedByBotId(coinsAbBotVo.getId());
        Set<String> exs = coinsApiVos.stream().map(CoinsApiVo::getExchangeName).collect(Collectors.toSet());
        HashSet<String> avaliableExs = new HashSet<>();
        for (String ex : exs) {
            for (ExchangeType exType : ExchangeType.values()) {
                if (exType.name().equalsIgnoreCase(ex)) {
                    avaliableExs.add(exType.getCoinsGlassQuery());
                }
            }
        }

        String queryEx = avaliableExs.stream().collect(Collectors.joining(","));
        List<CoinFundingInfo> coinFundingInfoList = coinGlassService.queryFundingRateInterestArbitrage(queryEx);
        //只处理前5个套利机会
        List<CoinFundingInfo> maxFundingInfoList = coinFundingInfoList.subList(0, 5);

        //判断收益率是否超过了预设百分比
        for (CoinFundingInfo coinFundingInfo : maxFundingInfoList) {
            if (coinFundingInfo.getApr() >= coinsAbBotVo.getAbPercentThreshold().doubleValue()) {
                //开仓，增加日志
                log.info("任务ID:{} 币对：{} 年华：{} 准备开仓",coinsAbBotVo.getId(),coinFundingInfo.getSymbol(),coinFundingInfo.getApr());
                coinsAbBotService.startPosition(coinsAbBotVo, coinFundingInfo);
                return;
            }
        }

        log.info("任务ID:{}  无满足套利币对",coinsAbBotVo.getId());

    }

}
