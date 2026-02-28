package com.bitstrat.init;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.bo.CoinsTaskBo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.service.BybitService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.service.ICoinsTaskService;
import com.bitstrat.utils.BitStratThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 19:59
 * @Content
 */

@Component
@Slf4j
public class SyncTaskRunner implements ApplicationRunner {

    @Autowired
    ICoinsTaskService coinsTaskService;

    @Autowired
    CommonServce commonServce;

    @Autowired
    BybitService bybitService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2,BitStratThreadFactory.forName("task-scheduler"));
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //建立websocket 连接
//        commonServce.initWebsocket();



        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
        coinsTaskBo.setStatus(2L);
        List<CoinsTaskVo> coinsTaskVos = coinsTaskService.queryList(coinsTaskBo);

        for (CoinsTaskVo coinsTaskVo : coinsTaskVos) {
            startSyncTask(coinsTaskVo);
        }
    }

    public void startSyncTask(CoinsTaskVo coinsTaskVo) {
        ScheduledFuture<?> schedule = scheduler.scheduleWithFixedDelay(() -> {
            try{
                //同步任务状态
                String symbol = coinsTaskVo.getSymbol().endsWith("USDT") ? coinsTaskVo.getSymbol() : (coinsTaskVo.getSymbol()+ "USDT");
                JSONObject position = commonServce.queryPosition(symbol, commonServce.getBybitUserAccountByExchange(coinsTaskVo.getCreateBy()));
                String markPrice = position.getString("markPrice");
                CoinsTaskBo taskBo = new CoinsTaskBo();
                taskBo.setId(coinsTaskVo.getId());
                taskBo.setMarkPrice(markPrice);
                BigDecimal size = position.getBigDecimal("size");
                taskBo.setBalance(size == null ?0:size.doubleValue());
                taskBo.setUnrealisedPnl(position.getString("unrealisedPnl"));
                BigDecimal avgPrice = position.getBigDecimal("avgPrice");
                taskBo.setAvgPrice(avgPrice == null ?null:avgPrice.doubleValue());
                taskBo.setPositionValue(position.getString("positionValue"));
                coinsTaskService.updateByBo(taskBo);
                log.info("同步任务 {} 结果 {}", coinsTaskVo.getName(), JSONObject.toJSONString(taskBo));
            }catch (Exception e){
                log.info("同步任务 {} 失败 {}", coinsTaskVo.getName(), e.getMessage());
            }
        },0, 5, TimeUnit.SECONDS);
        ConcurrentHashMap<Long, ScheduledFuture<?>> taskSchedulerMap = commonServce.getSyncTaskSchedulerMap();
        ScheduledFuture<?> remove = taskSchedulerMap.remove(coinsTaskVo.getId());
        if(remove != null){
            remove.cancel(true);
        }
        taskSchedulerMap.put(coinsTaskVo.getId(), schedule);
        log.info("任务 {} 已加载", coinsTaskVo.getName());
    }
}
