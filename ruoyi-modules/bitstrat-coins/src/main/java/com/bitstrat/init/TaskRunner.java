package com.bitstrat.init;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.bo.CoinsTaskBo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.service.BybitService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.service.ICoinsTaskService;
import com.bitstrat.strategy.NormalStrategy;
import com.bitstrat.strategy.StrategyManager;
import com.bitstrat.utils.BitStratThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.enums.BusinessStatus;
import org.dromara.common.log.event.OperLogEvent;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 19:59
 * @Content
 */

@Component
@Slf4j
public class TaskRunner implements ApplicationRunner {
    ListAppender<ILoggingEvent> defaultListAppender = new ListAppender<>();
    @Autowired
    ICoinsTaskService coinsTaskService;

    @Autowired
    CommonServce commonServce;

    @Autowired
    BybitService bybitService;

    @Autowired
    StrategyManager strategyManager;

    @Autowired
    RedissonClient redissonClient;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2,BitStratThreadFactory.forName("task-scheduler"));
    @Override
    public void run(ApplicationArguments args) throws Exception {
        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
        coinsTaskBo.setStatus(2L);
        List<CoinsTaskVo> coinsTaskVos = coinsTaskService.queryList(coinsTaskBo);

        for (CoinsTaskVo coinsTaskVo : coinsTaskVos) {
            startTask(coinsTaskVo);
        }
    }

    public void startTask(CoinsTaskVo coinsTaskVo) {
        ScheduledFuture<?> schedule = scheduler.scheduleWithFixedDelay(() -> {
            OperLogEvent operLog = new OperLogEvent();
            operLog.setTenantId(coinsTaskVo.getTenantId());
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            operLog.setOperParam(JSONObject.toJSONString(coinsTaskVo));
            long start = System.currentTimeMillis();
            ListAppender<ILoggingEvent> listAppender = null;
            try{

                // 请求的地址
                operLog.setOperIp("127.0.0.1");
                operLog.setOperUrl("TaskRunner");
                operLog.setOperName("SYSTEM");
                operLog.setDeptName("SYSTEM");


                // 设置方法名称
                String className =this.getClass().getName();
                String methodName = "startTask";
                operLog.setMethod(className + "." + methodName + "()");
                // 设置请求方式
                operLog.setRequestMethod("Inner");
                operLog.setCostTime(System.currentTimeMillis() - start);

                if("AI".equalsIgnoreCase(coinsTaskVo.getTaskType())){
                    throw new RuntimeException("暂不支持AI任务");
                }else{
                    NormalStrategy strategy = strategyManager.getStrategy(Math.toIntExact(coinsTaskVo.getRoleId()));
//                    listAppender = strategy.getListAppender();
//                    Logger logger = strategy.getLogger();
//                    // 添加 ListAppender 临时收集日志
//
//                    listAppender.start();
//                    logger.addAppender(listAppender);
                    if (Objects.nonNull(strategy)) {
                        strategy.run(coinsTaskVo,operLog);
                    }else{
                        throw new RuntimeException("未知的策略ID "+coinsTaskVo.getRoleId());
                    }
                }
                log.info("执行任务 {} 成功", coinsTaskVo.getName());
            }catch (Exception e){
                e.printStackTrace();
                log.info("执行任务 {} 失败 {}", coinsTaskVo.getName(), e.getMessage());
                if (e != null) {
                    operLog.setStatus(BusinessStatus.FAIL.ordinal());
                    operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 3800));
                }
            }

            //获取执行日志
            // 获取日志内容
//            if(listAppender != null){
//                String logs = listAppender.list.stream()
//                    .map(ILoggingEvent::getFormattedMessage)
//                    .collect(Collectors.joining("\n"));
//                JSONObject openlogs = new JSONObject();
//                openlogs.put("params", operLog.getJsonResult());
////                openlogs.put("logs", logs);
//                operLog.setJsonResult(JSONObject.toJSONString(openlogs));
//            }


            // 发布事件保存数据库
            SpringUtils.context().publishEvent(operLog);
        },0, 10, TimeUnit.SECONDS);
        ConcurrentHashMap<Long, ScheduledFuture<?>> taskSchedulerMap = commonServce.getTaskSchedulerMap();
        ScheduledFuture<?> remove = taskSchedulerMap.remove(coinsTaskVo.getId());
        if(remove != null){
            remove.cancel(true);
        }
        taskSchedulerMap.put(coinsTaskVo.getId(), schedule);
        log.info("任务 {} 已加载", coinsTaskVo.getName());
    }
}
