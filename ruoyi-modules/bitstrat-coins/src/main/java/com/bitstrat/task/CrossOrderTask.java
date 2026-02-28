package com.bitstrat.task;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/22 11:30
 * @Content
 */

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.service.ICoinsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 跨交易所套利订单定时同步
 */
@Component
@Slf4j
@JobExecutor(name = "crossOrderTask")
public class CrossOrderTask {
    @Autowired
    ICoinsOrderService coinsOrderService;

//    @Scheduled(cron = "*/10 * * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        List<CoinsOrderVo> coinsOrderVos = coinsOrderService.queryUnEndOrderList();
        Map<Long, List<CoinsOrderVo>> userIdMap = coinsOrderVos.stream().collect(Collectors.groupingBy(CoinsOrderVo::getCreateBy));
        for (Long userId : userIdMap.keySet()) {
            List<CoinsOrderVo> userOrders = userIdMap.getOrDefault(userId, new ArrayList<>());
            coinsOrderService.syncOrder(userOrders,userId,false);
        }
    }
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始同步订单情况");
        long startTimeStamp = System.currentTimeMillis();
        this.run();
        SnailJobLog.LOCAL.info("结束同步跨交易所订单情况 耗时 {} ms",System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("同步订单情况成功");
    }

}
