package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.NotifyType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsNotifyConfigVo;
import com.bitstrat.domain.vo.UpdateApiSettingVo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsNotifyConfigService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/18 14:42
 * @Content
 */

@RestController
@RequestMapping("/coinsUser")
public class CoinsUserController {

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    ICoinsNotifyConfigService coinsNotifyConfigService;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;

    @Autowired
    CommonServce commonServce;

    @GetMapping("/getApiSettingStatus")
    @SaCheckLogin
    public R getApiSettingStatus() {
        Long userId = LoginHelper.getUserId();
        //
        List<CoinsApiVo> coinsApiVos = coinsApiService.queryApiByUserId(userId);
        ArrayList<ApiSettingVo> apiSettingVos = new ArrayList<>();
        Map<String, CoinsApiVo> apiVoMap = coinsApiVos.stream().collect(Collectors.toMap(item -> item.getExchangeName().toLowerCase(), item -> item,(a,b)->a));

        for (ExchangeType value : ExchangeType.values()) {
            ApiSettingVo apiSettingVo = new ApiSettingVo();
            apiSettingVo.setExchangeName(value.getName());
            if (apiVoMap.containsKey(value.getName().toLowerCase())) {
                apiSettingVo.setStatus(1);
            }else{
                apiSettingVo.setStatus(2);
            }
            apiSettingVos.add(apiSettingVo);
        }

        return R.ok(apiSettingVos);
    }

    @GetMapping("/getApiSettingDetail")
    @SaCheckLogin
    public R getApiSettingDetail(String exchangeName) {
        Long userId = LoginHelper.getUserId();
        //
        List<CoinsApiVo> coinsApiVos = coinsApiService.queryApiByUserId(userId);
        Map<String, CoinsApiVo> apiVoMap = coinsApiVos.stream().collect(Collectors.toMap(item -> item.getExchangeName().toLowerCase(), item -> item));

        if(apiVoMap.containsKey(exchangeName.toLowerCase())){
            CoinsApiVo coinsApiVo = apiVoMap.get(exchangeName.toLowerCase());
            return R.ok(coinsApiVo);
        }

        return R.ok(new CoinsApiVo());
    }

    /**
     * 弃用
     * @param queryVo
     * @return
     */
    @Deprecated
    @PostMapping("/setApi")
    @SaCheckLogin
    public R setApi(@RequestBody UpdateApiSettingVo queryVo) {
        Long userId = LoginHelper.getUserId();
        //

        CoinsApiVo coinsApiVo = coinsApiService.queryApiByUserIdAndExchange(userId, queryVo.getExchange());
        if (Objects.isNull(coinsApiVo)) {
            CoinsApiBo coinsApiBo = new CoinsApiBo();
            coinsApiBo.setExchangeName(queryVo.getExchange());
            coinsApiBo.setUserId(userId);
            coinsApiBo.setApiKey(queryVo.getApiKey());
            coinsApiBo.setApiSecurity(queryVo.getApiSecurity());
            coinsApiBo.setCreateBy(LoginHelper.getUserId());
            coinsApiBo.setPassphrase(queryVo.getPassphrase());
//            coinsApiService.insertByBo(coinsApiBo);
        }else{
            CoinsApiBo coinsApiBo = new CoinsApiBo();
            coinsApiBo.setExchangeName(queryVo.getExchange());
            coinsApiBo.setUserId(userId);
            coinsApiBo.setApiKey(queryVo.getApiKey());
            coinsApiBo.setApiSecurity(queryVo.getApiSecurity());
            coinsApiBo.setCreateBy(LoginHelper.getUserId());
            coinsApiBo.setId(coinsApiVo.getId());
            coinsApiBo.setPassphrase(queryVo.getPassphrase());
//            coinsApiService.updateByBo(coinsApiBo);
        }

        return R.ok();
    }
    @PostMapping("/chekApi")
    @SaCheckLogin
    public R chekApi(@RequestBody UpdateApiSettingVo queryVo) {
        Long userId = LoginHelper.getUserId();
        CoinsApiBo coinsApiBo = new CoinsApiBo();
        coinsApiBo.setExchangeName(queryVo.getExchange());
        coinsApiBo.setUserId(userId);
        coinsApiBo.setApiKey(queryVo.getApiKey());
        coinsApiBo.setApiSecurity(queryVo.getApiSecurity());
        coinsApiBo.setCreateBy(LoginHelper.getUserId());
        coinsApiBo.setPassphrase(queryVo.getPassphrase());
        coinsApiBo.setType(queryVo.getType());
        CoinsApi coinsApi = new CoinsApi();
        BeanUtils.copyProperties(coinsApiBo, coinsApi);
        Account account = AccountUtils.coverToAccount(coinsApi);
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(queryVo.getExchange());
        APITypeHelper.set(queryVo.getType());
        try{
            HashMap<String, String> checkStatus = new HashMap<>();
            try{
                boolean b = exchangeService.checkApi(account);
                if (b) {
                    checkStatus.put("checkStatus", "true");
                    return R.ok(checkStatus);
                }else{
                    checkStatus.put("checkStatus", "false");
                    return R.ok(checkStatus);
                }
            }catch (Exception e){
                e.printStackTrace();
                checkStatus.put("checkStatus", "false");
                return R.ok(checkStatus);
            }
        }finally {
            APITypeHelper.clear();
        }
    }



    @GetMapping("/getNotifySettingStatus")
    @SaCheckLogin
    public R getNotifySettingStatus() {
        Long userId = LoginHelper.getUserId();
        //
        List<CoinsNotifyConfigVo> coinsNotifyConfigVos = coinsNotifyConfigService.queryConfigByUserId(userId);
        ArrayList<NotifySettingVo> notifySettingVos = new ArrayList<>();
        Map<String, CoinsNotifyConfigVo> apiVoMap = coinsNotifyConfigVos.stream().collect(Collectors.toMap(item -> item.getType().toLowerCase(), item -> item));

        for (NotifyType value : NotifyType.values()) {
            NotifySettingVo apiSettingVo = new NotifySettingVo();
            apiSettingVo.setNotifyType(value.getName());
            if (apiVoMap.containsKey(value.getName().toLowerCase())) {
                apiSettingVo.setStatus(1);
            }else{
                apiSettingVo.setStatus(2);
            }
            notifySettingVos.add(apiSettingVo);
        }

        return R.ok(notifySettingVos);
    }
    @GetMapping("/getWebsocketStatus")
    @SaCheckLogin
    public R getWebsocketStatus() {
        List<WebsocketExStatus> result = coinsApiService.getWebsocketExStatuses(LoginHelper.getUserId());

        return R.ok(result);
    }


}
