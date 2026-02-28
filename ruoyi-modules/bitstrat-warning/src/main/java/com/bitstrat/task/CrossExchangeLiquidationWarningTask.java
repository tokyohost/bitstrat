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
import com.bitstrat.service.ICommonService;
import com.bitstrat.service.impl.FundingFeeService;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.strategy.ExchangeApiManager;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
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
 * 爆仓预警任务
 */
@Component
@Slf4j
@JobExecutor(name = "crossExchangeLiquidationWarningTask")
public class CrossExchangeLiquidationWarningTask {
    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;
    @Autowired
    ICoinsNotifyService coinsNotifyService;
    @Autowired
    ICommonService commonService;
    @Autowired
    ExecuteService executeService;


    @Transactional(rollbackFor = Exception.class)
    public void run() {
        Map<String, Object> params = new HashMap<>();
        params.put("liquidationConfigStatus", 1);
        // todo 先这么写，后期考虑实时性等方面，再改造
        List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = coinsCrossExchangeArbitrageTaskService.queryListWithWarning(params);
        for (CoinsCrossExchangeArbitrageTaskVo vo : coinsCrossExchangeArbitrageTaskVos) {
            executeService.getLiquidationExecute().submit(() -> liquidationWarning(vo));
        }
    }

    private void liquidationWarning(CoinsCrossExchangeArbitrageTaskVo vo) {
        // 做多爆仓预警阈值
        BigDecimal longLiquidationThreshold = vo.getLongLiquidationThreshold();
        // 做多预估强平价
        BigDecimal longLiqPx = vo.getLongLiqPx();
        // 做多当前价格
        BigDecimal longNowPrice = (BigDecimal) commonService.querySymbolMarketPrice(vo.getLongEx(), vo.getSymbol()).getData();

        // SHORT
        BigDecimal shortLiquidationThreshold = vo.getShortLiquidationThreshold();
        BigDecimal shortLiqPx = vo.getShortLiqPx();
        BigDecimal shortNowPrice = (BigDecimal) commonService.querySymbolMarketPrice(vo.getShortEx(), vo.getSymbol()).getData();

        // 做多方向预警检查
        if (longNowPrice != null && longLiqPx != null && longLiquidationThreshold != null) {
            BigDecimal longDiffPercent = longNowPrice.subtract(longLiqPx)
                .divide(longNowPrice, 5, RoundingMode.HALF_UP)
                .abs();

            if (longDiffPercent.compareTo(longLiquidationThreshold.abs()) <= 0) {
                sendLiquidationWarning(vo, "LONG", longNowPrice, longLiqPx, formatPercent(longLiquidationThreshold));
            }
        }

        // 做空方向预警检查
        if (shortNowPrice != null && shortLiqPx != null && shortLiquidationThreshold != null) {
            BigDecimal shortDiffPercent = shortLiqPx.subtract(shortNowPrice)
                .divide(shortNowPrice, 5, RoundingMode.HALF_UP)
                .abs();

            if (shortDiffPercent.compareTo(shortLiquidationThreshold.abs()) <= 0) {
                sendLiquidationWarning(vo, "SHORT", shortNowPrice, shortLiqPx, formatPercent(shortLiquidationThreshold));
            }
        }
    }

    //todo  爆仓
    private void sendLiquidationWarning(CoinsCrossExchangeArbitrageTaskVo vo, String direction,
                                        BigDecimal currentPrice, BigDecimal liqPrice, BigDecimal threshold) {
        String template = """
            ⚠️ 爆仓风险预警
            币对：%s
            方向：%s
            交易所：%s
            当前价格：%s
            强平价格：%s
            预警阈值：%s%%

            ⚠️ 请注意风险，及时调整仓位或补充保证金！
            """;

        String message = String.format(
            template,
            vo.getSymbol(),
            direction.equals("LONG") ? "做多" : "做空",
            direction.equals("LONG") ? vo.getLongEx() : vo.getShortEx(),
            currentPrice.toPlainString(),
            liqPrice.toPlainString(),
            threshold.toPlainString()
        );

        coinsNotifyService.sendNotification(vo.getUserId(), message);
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
