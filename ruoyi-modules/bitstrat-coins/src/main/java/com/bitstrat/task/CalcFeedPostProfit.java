package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.FeedStatus;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.domain.bo.CoinsFeedBo;
import com.bitstrat.domain.vo.CoinAITaskBalanceVo;
import com.bitstrat.domain.vo.CoinsFeedVo;
import com.bitstrat.service.AiService;
import com.bitstrat.service.ICoinAITaskBalanceService;
import com.bitstrat.service.ICoinsAiTaskService;
import com.bitstrat.service.ICoinsFeedService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/12 22:37
 * @Content
 */
@Component
@Slf4j
@AllArgsConstructor
@JobExecutor(name = "CalcFeedPostProfit")
public class CalcFeedPostProfit {
    private final ICoinsFeedService coinsFeedService;
    private final ICoinAITaskBalanceService coinTestAiService;
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始计算收益率定时任务");
        long startTimeStamp = System.currentTimeMillis();
        this.calc();
        SnailJobLog.LOCAL.info("结束计算收益率定时任务 耗时 {} ms",System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("计算收益率定时任务执行成功");
    }

    private void calc() {
        CoinsFeedBo feedBo = new CoinsFeedBo();
        feedBo.setStatus(FeedStatus.PUBLISH.getStatus());

        List<CoinsFeedVo> coinsFeedVos = coinsFeedService.queryList(feedBo);
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startDate = now.minusMonths(3);//最近三个月
        for (CoinsFeedVo coinsFeedVo : coinsFeedVos) {
            calcPercent(coinsFeedVo, startDate, now);
        }
    }
    public void calc(CoinsFeedVo coinsFeedVo) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startDate = now.minusMonths(3);//最近三个月
        calcPercent(coinsFeedVo, startDate, now);
    }

    private void calcPercent(CoinsFeedVo coinsFeedVo, ZonedDateTime startDate, ZonedDateTime now) {
        // 计算收益率
        CoinAITaskBalanceBo queryParams = new CoinAITaskBalanceBo();
        queryParams.setTaskId(coinsFeedVo.getStrategyId());
        queryParams.setStartDate(startDate);
        queryParams.setEndDate(now);
        // 查询原始数据
        List<CoinAITaskBalanceVo> voList = coinTestAiService.queryList(queryParams);

        // 按时间升序
        List<CoinAITaskBalanceVo> sortedList = voList.stream()
            .sorted(Comparator.comparing(CoinAITaskBalanceVo::getTime))
            .collect(Collectors.toList());

        // 计算最早的金额和现在的金额 的收益率
        // 最早金额
        BigDecimal firstEquity = sortedList.get(0).getEquity();
        // 最新金额
        BigDecimal lastEquity  = sortedList.get(sortedList.size() - 1).getEquity();

        if (firstEquity == null || lastEquity == null || firstEquity.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // (最新 - 最初) / 最初
        BigDecimal profitPercent = lastEquity
            .subtract(firstEquity)
            .multiply(BigDecimal.valueOf(100))
            .divide(firstEquity, 2, RoundingMode.HALF_UP);
        CoinsFeedBo feedBoUpdate = new CoinsFeedBo();
        feedBoUpdate.setId(coinsFeedVo.getId());
        feedBoUpdate.setProfit3m(profitPercent);
        coinsFeedService.updateByBo(feedBoUpdate);
    }
}
