package com.bitstrat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.distuptor.MarketABPriceEventHandler;
import com.bitstrat.ai.domain.StartCompareContext;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.ai.domain.serverWatch.ServerWatchContext;
import com.bitstrat.ai.domain.serverWatch.StopWatchContext;
import com.bitstrat.domain.CoinsAbTask;
import com.bitstrat.domain.bo.CoinsAbTaskBo;
import com.bitstrat.domain.vo.CoinsAbTaskVo;
import com.bitstrat.service.ICoinsAbOrderService;
import com.bitstrat.service.ICoinsAbTaskService;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/4 17:10
 * @Content
 */

@Service
public class CoinsAbOrderServiceImpl implements ICoinsAbOrderService {

    @Autowired
    ICoinsAbTaskService iCoinsAbTaskService;
    @Autowired
    MarketABPriceEventHandler marketABPriceEventHandler;

    @Override
    public ABOrderTask updateOrCreateABOrderTask(ABOrderTask abOrder,Long userId) {
        CoinsAbTaskVo coinsAbTask = iCoinsAbTaskService.queryByTaskId(abOrder.getTaskId());
        if(Objects.isNull(coinsAbTask)){
            CoinsAbTaskBo coinsAbTaskBo = new CoinsAbTaskBo();
            coinsAbTaskBo.setTaskId(abOrder.getTaskId());
            coinsAbTaskBo.setBody(JSONObject.toJSONString(abOrder));
            coinsAbTaskBo.setUserId(userId);
            coinsAbTaskBo.setCreateTime(new Date());
            coinsAbTaskBo.setUpdateTime(new Date());
            iCoinsAbTaskService.insertByBo(coinsAbTaskBo);
        }else{
            CoinsAbTaskBo coinsAbTaskBo = new CoinsAbTaskBo();
            coinsAbTaskBo.setId(coinsAbTask.getId());
            coinsAbTaskBo.setTaskId(abOrder.getTaskId());
            coinsAbTaskBo.setBody(JSONObject.toJSONString(abOrder));
            coinsAbTaskBo.setUserId(userId);
            coinsAbTaskBo.setUpdateTime(new Date());
            iCoinsAbTaskService.updateByBo(coinsAbTaskBo);
        }

        ServerWatchContext startCompareContext = new ServerWatchContext();
        abOrder.setUserId(userId);
        if (StringUtils.isEmpty(abOrder.getTaskId())) {
            abOrder.setTaskId(IdUtil.fastUUID());
        }
        abOrder.setMarketABPriceEventHandler(marketABPriceEventHandler);
        startCompareContext.setMarketABPriceEventHandler(marketABPriceEventHandler);
        startCompareContext.setAbOrderTask(abOrder);
        startCompareContext.setUserId(userId);
        abOrder.setLastUpdateTime(new Date());
        SpringUtils.getApplicationContext().publishEvent(startCompareContext);
        return abOrder;
    }

    public ABOrderTask stopABOrderTask(ABOrderTask abOrder,Long userId) {
        CoinsAbTaskVo coinsAbTask = iCoinsAbTaskService.queryByTaskId(abOrder.getTaskId());
        if(Objects.isNull(coinsAbTask)){
            CoinsAbTaskBo coinsAbTaskBo = new CoinsAbTaskBo();
            coinsAbTaskBo.setTaskId(abOrder.getTaskId());
            coinsAbTaskBo.setBody(JSONObject.toJSONString(abOrder));
            coinsAbTaskBo.setUserId(userId);
            coinsAbTaskBo.setCreateTime(new Date());
            coinsAbTaskBo.setUpdateTime(new Date());
            iCoinsAbTaskService.insertByBo(coinsAbTaskBo);
        }else{
            CoinsAbTaskBo coinsAbTaskBo = new CoinsAbTaskBo();
            coinsAbTaskBo.setId(coinsAbTask.getId());
            coinsAbTaskBo.setTaskId(abOrder.getTaskId());
            coinsAbTaskBo.setBody(JSONObject.toJSONString(abOrder));
            coinsAbTaskBo.setUserId(userId);
            coinsAbTaskBo.setUpdateTime(new Date());
            iCoinsAbTaskService.updateByBo(coinsAbTaskBo);
        }

        StopWatchContext startCompareContext = new StopWatchContext();
        abOrder.setUserId(userId);
        if (StringUtils.isEmpty(abOrder.getTaskId())) {
            abOrder.setTaskId(IdUtil.fastUUID());
        }
        abOrder.setMarketABPriceEventHandler(marketABPriceEventHandler);
        startCompareContext.setAbOrderTask(abOrder);
        startCompareContext.setUserId(userId);
        abOrder.setLastUpdateTime(new Date());
        SpringUtils.getApplicationContext().publishEvent(startCompareContext);
        return abOrder;
    }
}
