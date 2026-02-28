package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.Event.AckPositionSyncEvent;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.service.ICoinsAbOrderService;
import com.bitstrat.service.ICoinsAbTaskService;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/4 17:04
 * @Content
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/abOrder")
public class CoinsABOrderController {

    @Autowired
    ICoinsAbOrderService coinsAbOrderService;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    ICoinsAbTaskService iCoinsAbTaskService;

    @PostMapping("/sync")
    @SaCheckLogin
    public R<ABOrderTask> sync(@Validated @RequestBody ABOrderTask abOrder) {
        ABOrderTask abOrderTask = coinsAbOrderService.updateOrCreateABOrderTask(abOrder,LoginHelper.getUserId());

        return R.ok(abOrderTask);
    }

    @PostMapping("/stop")
    @SaCheckLogin
    public R<ABOrderTask> stop(@Validated @RequestBody ABOrderTask abOrder) {
        ABOrderTask abOrderTask = coinsAbOrderService.stopABOrderTask(abOrder,LoginHelper.getUserId());

        return R.ok(abOrderTask);
    }

    @GetMapping("/syncPosition")
    @SaCheckLogin
    public synchronized R<ABOrderTask> syncPosition() {

        List<CoinsApiVo> coinsApiVos = coinsApiService.queryApiByUserId(LoginHelper.getUserId());
        for (CoinsApiVo coinsApiVo : coinsApiVos) {
            Account account = AccountUtils.coverToAccount(coinsApiVo);
            AckPositionSyncEvent ackPositionSyncEvent = new AckPositionSyncEvent();
            ackPositionSyncEvent.setAccount(account);
            ackPositionSyncEvent.setExchangeName(coinsApiVo.getExchangeName());
            SpringUtils.getApplicationContext().publishEvent(ackPositionSyncEvent);
        }
        return R.ok();
    }

}
