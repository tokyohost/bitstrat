package com.bitstrat.init;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.domain.bo.CoinsAbTaskBo;
import com.bitstrat.domain.vo.CoinsAbTaskVo;
import com.bitstrat.service.ICoinsAbOrderService;
import com.bitstrat.service.ICoinsAbTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class initAbTask  implements ApplicationRunner {

    @Autowired
    ICoinsAbOrderService coinsAbOrderService;
    @Autowired
    ICoinsAbTaskService iCoinsAbTaskService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<CoinsAbTaskVo> coinsAbTaskVos = iCoinsAbTaskService.queryList(new CoinsAbTaskBo());
        for (CoinsAbTaskVo coinsAbTaskVo : coinsAbTaskVos) {
            String body = coinsAbTaskVo.getBody();
            ABOrderTask abOrderTask = JSONObject.parseObject(body).to(ABOrderTask.class);
            abOrderTask.setTaskId(coinsAbTaskVo.getTaskId());
            coinsAbOrderService.updateOrCreateABOrderTask(abOrderTask,coinsAbTaskVo.getUserId());
        }

    }
}
