package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.bitstrat.service.BatchOrderTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@JobExecutor(name = "crossBatchTask")
public class CrossBatchTask {

    @Autowired
    BatchOrderTaskService batchOrderTaskService;
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始检查批量订单任务");
        long startTimeStamp = System.currentTimeMillis();
        batchOrderTaskService.checkAndRunBatchOrderTask();
        SnailJobLog.LOCAL.info("结束检查批量订单任务 耗时 {} ms",System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("检查批量订单任务成功");
    }
}
