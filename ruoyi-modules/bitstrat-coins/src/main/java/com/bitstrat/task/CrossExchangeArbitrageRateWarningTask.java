package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.bitstrat.domain.FundFeeTask;
import com.bitstrat.domain.TaskBalance;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import com.bitstrat.service.ICoinsNotifyService;
import com.bitstrat.service.impl.FundingFeeService;
import com.bitstrat.strategy.ExchangeApiManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 跨交易所资金费套利任务预警
 */
@Component
@Slf4j
@JobExecutor(name = "crossExchangeArbitrageRateWarningTask")
public class CrossExchangeArbitrageRateWarningTask {
    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;
    @Autowired
    ICoinsNotifyService coinsNotifyService;

    @Autowired
    ExchangeApiManager exchangeApiManager;
    @Autowired
    FundingFeeService fundingFeeService;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    //    @Scheduled(cron = "*/10 * * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", 1);
        List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = coinsCrossExchangeArbitrageTaskService.queryListWithWarning(params);
        for (CoinsCrossExchangeArbitrageTaskVo vo : coinsCrossExchangeArbitrageTaskVos) {
            fundingFeeInversionWarning(vo);
        }
    }
    private void fundingFeeInversionWarning(CoinsCrossExchangeArbitrageTaskVo vo) {
        // 判断是否资金费倒挂
        boolean isFundingFeeInversion = fundingFeeService.checkCrossExchangFundingFeeInversion(vo);
        if (isFundingFeeInversion) {
            FundFeeTask fundFeeTask = fundingFeeService.queryFundingFeeByTask(vo);
            TaskBalance taskBalance = fundingFeeService.queryBalanceByTask(vo);
            String template = """
                ⚠ 资金费监控报警
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
                """;

            BigDecimal longFee = formatPercent(fundFeeTask.getLongFundingFee());

            BigDecimal shortFee = formatPercent(fundFeeTask.getShortFundingFee());

            BigDecimal profit = fundFeeTask.getFundingFeeCanMake()
                .multiply(BigDecimal.valueOf(100))
                .setScale(5, RoundingMode.HALF_UP);

            BigDecimal apy = fundFeeTask.getApy()
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
        } else {
            FundFeeTask fundFeeTask = fundingFeeService.queryFundingFeeByTask(vo);
            TaskBalance taskBalance = fundingFeeService.queryBalanceByTask(vo);
            String template = """
                📊 资金费监控提醒
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
                """;

            BigDecimal longFee = formatPercent(fundFeeTask.getLongFundingFee());

            BigDecimal shortFee = formatPercent(fundFeeTask.getShortFundingFee());

            BigDecimal profit = fundFeeTask.getFundingFeeCanMake()
                .multiply(BigDecimal.valueOf(100))
                .setScale(5, RoundingMode.HALF_UP);

            BigDecimal apy = fundFeeTask.getApy()
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
        }
    }

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始跨交易所资金费套利任务预警");
        long startTimeStamp = System.currentTimeMillis();
        this.run();
        SnailJobLog.LOCAL.info("跨交易所资金费套利任务预警 耗时 {} ms", System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("跨交易所资金费套利任务预警成功");
    }

    private static BigDecimal formatPercent(BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(100)).setScale(5, RoundingMode.HALF_UP);
    }

}
