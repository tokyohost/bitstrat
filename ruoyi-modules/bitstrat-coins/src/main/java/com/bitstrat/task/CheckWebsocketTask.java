package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.annotation.MapExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.alibaba.fastjson2.JSON;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.wsClients.WsClusterManager;
import com.bitstrat.service.*;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:15
 * @Content 检查活跃的持仓，并创建websocket 连接监听订单和仓位
 */
@Component
@Slf4j
@JobExecutor(name = "checkWebsocketTask")
public class CheckWebsocketTask{
    @Autowired
    ICoinsAiTaskService coinsAiTaskService;

    @Autowired
    ICommonService commonServce;

    @Autowired
    WsClusterManager clusterManager;
    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;

    @Autowired
    RedissonClient redissonClient;

    private final String LOCK_KEY = "checkWebsocketTask:";

    private final Integer BATCH_SIZE = 8;
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始检查Ws连接情况");
        long startTimeStamp = System.currentTimeMillis();
        this.run(null);
        SnailJobLog.LOCAL.info("结束检查Ws连接情况 耗时 {}ms", System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("检查Ws连接情况成功");
    }


    public void run(List<CoinsAiTaskVo> coinsAiTaskVos) {
        if(Objects.isNull(coinsAiTaskVos)){
            CoinsAiTaskBo coinsAiTaskBo = new CoinsAiTaskBo();
            coinsAiTaskBo.setStatus(2L);
            coinsAiTaskVos = coinsAiTaskService.queryList(coinsAiTaskBo);
        }

        //都是正在运行的任务，检查websocket 连接是否没有建立
        for (CoinsAiTaskVo coinsAiTaskVo : coinsAiTaskVos) {
            ExchangeType exchangeType = ExchangeType.getExchangeType(coinsAiTaskVo.getExchange());
            if (Objects.nonNull(exchangeType)) {
                RLock lock = redissonClient.getLock(LOCK_KEY + coinsAiTaskVo.getApiId());
                try{
                    lock.tryLock(60, TimeUnit.SECONDS);
                    try{
                        Channel channel = exchangeConnectionManager.getChannel(coinsAiTaskVo.getCreateUserId() + "", coinsAiTaskVo.getApiId(), exchangeType.getName(), WebSocketType.PRIVATE);
                        if(Objects.isNull(channel) || !channel.isActive()) {
                            log.info("检测到用户 {} 任务 {} 交易所 {} 私有频道ws连接未建立，重新创建连接", coinsAiTaskVo.getCreateUserId(), coinsAiTaskVo.getId(), exchangeType.getName());
                            SnailJobLog.LOCAL.info("检测到用户 {} 任务 {} 交易所 {} 私有频道ws连接未建立，重新创建连接", coinsAiTaskVo.getCreateUserId(), coinsAiTaskVo.getId(), exchangeType.getName());
                            commonServce.createWebsocketConnections(exchangeType.getName(),coinsAiTaskVo.getApiId(),coinsAiTaskVo.getCreateUserId());
                        }
                    }finally {
                        lock.unlock();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    SnailJobLog.LOCAL.info("检查Ws连接情况报错 节点ID:{}, error:{}", clusterManager.getNodeId(),e.getCause());
                }

            }
        }
    }


    @MapExecutor
    public ExecuteResult rootMapExecute(MapArgs mapArgs, MapHandler mapHandler) {
        SnailJobLog.LOCAL.info("检查Ws连接情况任务开始切片 节点ID:{}", clusterManager.getNodeId());
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
        SnailJobLog.LOCAL.info("检查Ws连接情况任务切片 SIZE={} 节点ID:{}",bsize, clusterManager.getNodeId());
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
        ExecuteResult secondMap = mapHandler.doMap(partition, "checkWebsocketTaskSecondMap");
        SnailJobLog.LOCAL.info("检查Ws连接情况任务切片结束 节点ID:{}", clusterManager.getNodeId());
        return secondMap;
    }

    // 由ROOT_MAP分片分发到SECOND_MAP任务
    @MapExecutor(taskName = "checkWebsocketTaskSecondMap")
    public ExecuteResult secondMapExecute(MapArgs mapArgs) {
        SnailJobLog.REMOTE.info("检查Ws连接情况任务执行切片 {} 节点ID {}", JSON.toJSONString(mapArgs.getMapResult()),clusterManager.getNodeId());
        // mapResult 即为上述分包中分发下来的具体参数
        // 各节点分别处理分片后的业务逻辑
        // 测试目的2时取消注释
        //List<String> strings = JSON.parseArray(JSON.toJSONString(mapArgs.getMapResult()), String.class);
        //if (strings.contains("5")){
        //    int i = 1/0;
        //}
        List<Long> ids = JSON.parseArray(JSON.toJSONString(mapArgs.getMapResult()), Long.class);
        List<CoinsAiTaskVo> coinsAiTaskVos = coinsAiTaskService.queryListByIds(ids);
        this.run(coinsAiTaskVos);
        SnailJobLog.LOCAL.info("执行结束 共完成{} 个节点ID:{}",ids.size(), clusterManager.getNodeId());
        return ExecuteResult.success("执行完毕");
    }



//    @Override
//    public void afterPropertiesSet() throws Exception {
//        this.run();
//    }
}
