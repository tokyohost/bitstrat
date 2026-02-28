package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.CoinsBalanceStatus;
import com.bitstrat.constant.CoinsBalanceType;
import com.bitstrat.domain.CoinsBalanceLog;
import com.bitstrat.domain.bo.CoinsBalanceLogBo;
import com.bitstrat.service.CommonService;
import com.bitstrat.service.ICoinsBalanceLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.event.UserRegisterEvent;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.service.ISysUserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/4 11:19
 * @Content
 */
@Service
@Slf4j
@AllArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final ICoinsBalanceLogService coinsBalanceLogService;
    private final ISysUserService sysUserService;
    private final RestTemplate restTemplate;

    @Override
    @EventListener(UserRegisterEvent.class)
    @Transactional(rollbackFor = Exception.class)
    public void userRegister(UserRegisterEvent userRegisterEvent) {
        log.info("用户ID:{} 注册成功，赠送10元额度",userRegisterEvent.getUserId());
        //注册成功，默认添加1元余额
        CoinsBalanceLogBo coinsBalanceLog = new CoinsBalanceLogBo();
        coinsBalanceLog.setUserId(userRegisterEvent.getUserId());
        coinsBalanceLog.setAfterBalance(BigDecimal.ONE);
        coinsBalanceLog.setBeforeBalance(BigDecimal.ZERO);
        coinsBalanceLog.setStatus(CoinsBalanceStatus.SUCCESS.getStatus());
        coinsBalanceLog.setType(CoinsBalanceType.GIFT.getStatus());
        coinsBalanceLog.setChangeAmount(BigDecimal.TEN);
        coinsBalanceLog.setRemark("赠送额度");
        coinsBalanceLogService.insertByBo(coinsBalanceLog);
        coinsBalanceLogService.addBalanceByUserId(BigDecimal.TEN, userRegisterEvent.getUserId());
    }

    /**
     * 查实时汇率 CNY 到 USD
     * @return
     */
    @Override
    @Cacheable(value = "rmb2usdRate#1d#1d#10", key = "'rmb2usdRate'")
    public BigDecimal getRMB2USDRate() {
        String url = "https://free.ratesdb.com/v1/rates?from=CNY&to=USD";
        String object = restTemplate.getForObject(url, String.class);
        try{
            if (StringUtils.isNotBlank(object) && JSON.isValid(object)) {
                JSONObject parsed = JSONObject.parseObject(object);
                JSONObject data = parsed.getJSONObject("data");
                BigDecimal bigDecimal = data.getJSONObject("rates").getBigDecimal("USD");
                if (Objects.isNull(bigDecimal)) {
                    return BigDecimal.valueOf(0.1428);
                }else{
                    return bigDecimal;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return BigDecimal.valueOf(0.1428);
    }

    public static void main(String[] args) {
        RestTemplate restTemplate1 = new RestTemplate();
        String object = restTemplate1.getForObject("https://free.ratesdb.com/v1/rates?from=CNY&to=USD", String.class);
        System.out.println(object);
    }
}
