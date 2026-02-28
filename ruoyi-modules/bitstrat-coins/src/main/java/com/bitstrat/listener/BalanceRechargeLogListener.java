package com.bitstrat.listener;

import com.bitstrat.constant.CoinsBalanceStatus;
import com.bitstrat.constant.CoinsBalanceType;
import com.bitstrat.domain.bo.CoinsBalanceLogBo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsBalanceLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.system.domain.bo.SysUserAddBalanceBo;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 16:20
 * @Content
 */

@Slf4j
@Component
@AllArgsConstructor
public class BalanceRechargeLogListener {
    private final ICoinsBalanceLogService coinsBalanceLogService;

    @EventListener(SysUserAddBalanceBo.class)
    public void onMessage(SysUserAddBalanceBo balanceBo) {
        log.info("balanceBo {}", balanceBo);
        CoinsBalanceLogBo coinsBalanceLogBo = new CoinsBalanceLogBo();
        coinsBalanceLogBo.setRemark(balanceBo.getRemark());
        coinsBalanceLogBo.setType(balanceBo.getType());
        coinsBalanceLogBo.setStatus(CoinsBalanceStatus.SUCCESS.getStatus());
        coinsBalanceLogBo.setChangeAmount(balanceBo.getBalance());
        coinsBalanceLogBo.setUserId(balanceBo.getUserId());
        coinsBalanceLogService.insertByBo(coinsBalanceLogBo);
    }
}
