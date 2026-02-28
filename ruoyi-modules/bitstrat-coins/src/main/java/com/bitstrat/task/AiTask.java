package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.annotation.MapExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.service.AiService;
import com.bitstrat.service.ICoinsAiTaskService;
import com.bitstrat.service.impl.AiServiceImpl;
import com.bitstrat.wsClients.WsClusterManager;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/25 17:56
 * @Content
 */

@Component
@Slf4j
@AllArgsConstructor
@JobExecutor(name = "AiTask")
public class AiTask {

    private final AiService aiService;

    private WsClusterManager clusterManager;
    private final Integer BATCH_SIZE = 8;


    private final ICoinsAiTaskService coinsAiTaskService;
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始AI任务定时任务");
        long startTimeStamp = System.currentTimeMillis();
        this.checkAiTask(null);
        SnailJobLog.LOCAL.info("结束AI任务定时任务 耗时 {} ms",System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("AI任务定时任务执行成功");
    }

    private void checkAiTask(List<CoinsAiTaskVo> coinsAiTaskVos) {
        if(Objects.isNull(coinsAiTaskVos)){
            CoinsAiTaskBo coinsAiTaskBo = new CoinsAiTaskBo();
            coinsAiTaskBo.setStatus(2L);
            coinsAiTaskVos = coinsAiTaskService.queryList(coinsAiTaskBo);
        }
        for (CoinsAiTaskVo coinsAiTaskVo : coinsAiTaskVos) {
            if (coinsAiTaskVo.getLastRunTime() == null) {
                //直接执行
                CoinsAiTask convert = MapstructUtils.convert(coinsAiTaskVo, CoinsAiTask.class);
                convert.setLastRunTime(new Date());
                coinsAiTaskService.updateByBo(MapstructUtils.convert(convert, CoinsAiTaskBo.class));
                aiService.invokeAiTask(convert);

            }else{
                //判断是否上次执行的时间跟现在已经超过了
                Date lastRunTime = coinsAiTaskVo.getLastRunTime();
                String interval = coinsAiTaskVo.getInterval(); // 如 "5m"

                if (isIntervalExceeded(lastRunTime, interval)) {
                    // 已超时，可以执行
                    CoinsAiTask convert = MapstructUtils.convert(coinsAiTaskVo, CoinsAiTask.class);
                    convert.setLastRunTime(new Date());
                    coinsAiTaskService.updateByBo(MapstructUtils.convert(convert, CoinsAiTaskBo.class));
                    aiService.invokeAiTask(convert);
                } else {
                    // 未超过间隔，不执行
                    log.info("任务未达间隔时间，不执行: interval={}, lastRun={}", interval, lastRunTime);
                }
            }
        }
    }


    @MapExecutor
    public ExecuteResult rootMapExecute(MapArgs mapArgs, MapHandler mapHandler) {
        SnailJobLog.LOCAL.info("AI任务定时任务开始切片 节点ID:{}", clusterManager.getNodeId());
        // 此处可以进行自定义的分片逻辑

        CoinsAiTaskBo coinsAiTaskBo = new CoinsAiTaskBo();
        coinsAiTaskBo.setStatus(2L);
        List<CoinsAiTaskVo> coinsAiTaskVos = coinsAiTaskService.queryList(coinsAiTaskBo);
        Object jobParams = mapArgs.getJobParams();
        String paramsSize = String.valueOf(jobParams);
        Integer bsize = BATCH_SIZE;
        if(NumberUtils.isParsable(paramsSize)){
            bsize = Integer.valueOf(paramsSize);
        }
        SnailJobLog.LOCAL.info("AI任务定时任务切片 SIZE={} 节点ID:{}",bsize, clusterManager.getNodeId());
        List<Long> collect = coinsAiTaskVos.stream().map(CoinsAiTaskVo::getId).collect(Collectors.toList());
        List<List<Long>> partition = Lists.partition(collect, bsize);
        if(partition.size() == 0){
            return ExecuteResult.success("执行结束,暂无待执行任务");
        }
        // 服务端会获取到客户端的节点数量来处理map的各个分片
        // 根据在页面配置的路由策略，把对应的分片分发到不同客户端节点
        // 例如：入参总量为10条，BATCH_SIZE设置为3则每3条一个分组，分组结果（List<T> taskList）为[{0,3}, {4, 6}, {7,9}, {10}]，此时就会分为四组
        // 集合中的各个对象都会作为MapArgs的mapResult参数传递到后续的各个Map任务中
        // 此处的nextTaskName即为后续分发的Map任务名称
        ExecuteResult secondMap = mapHandler.doMap(partition, "AiTaskSecondMap");
        SnailJobLog.LOCAL.info("AI任务定时任务切片结束 节点ID:{}", clusterManager.getNodeId());
        return secondMap;
    }

    // 由ROOT_MAP分片分发到SECOND_MAP任务
    @MapExecutor(taskName = "AiTaskSecondMap")
    public ExecuteResult secondMapExecute(MapArgs mapArgs) {
        SnailJobLog.REMOTE.info("AI任务定时任务执行切片 {}",JSON.toJSONString(mapArgs.getMapResult()));
        // mapResult 即为上述分包中分发下来的具体参数
        // 各节点分别处理分片后的业务逻辑
        // 测试目的2时取消注释
        //List<String> strings = JSON.parseArray(JSON.toJSONString(mapArgs.getMapResult()), String.class);
        //if (strings.contains("5")){
        //    int i = 1/0;
        //}
        List<Long> ids = JSON.parseArray(JSON.toJSONString(mapArgs.getMapResult()), Long.class);
        List<CoinsAiTaskVo> coinsAiTaskVos = coinsAiTaskService.queryListByIds(ids);
        this.checkAiTask(coinsAiTaskVos);
        return ExecuteResult.success("执行完毕");
    }


    public static boolean isIntervalExceeded(Date lastRunTime, String interval) {
        if (lastRunTime == null || interval == null || !interval.endsWith("m")) {
            return true;
        }

        // 1. Date -> LocalDateTime
        LocalDateTime last = LocalDateTime.ofInstant(lastRunTime.toInstant(), ZoneId.systemDefault());

        // 2. 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 3. 解析分钟数
        int minutes = Integer.parseInt(interval.replace("m", ""));

        // 4. 判断是否超过
        Duration diff = Duration.between(last, now);
        return diff.toMinutes() >= minutes;
    }
}
