package com.bitstrat.task;

import cn.hutool.core.date.DateUtil;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.bo.CoinsAccountBalanceRecordBo;
import com.bitstrat.domain.vo.QueryBalanceBody;
import com.bitstrat.service.ICoinsAccountBalanceRecordService;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICommonService;
import com.bitstrat.service.KeyCryptoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.core.domain.R;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 每日记录用户余额
 * @author caoyang
 * @date 2025-05-08
 */

@Component
@Slf4j
@JobExecutor(name = "crossAccountBalanceRecordTask")
public class CrossAccountBalanceRecordTask {

    @Autowired
    private ICoinsApiService coinsApiService;
    @Autowired
    private ICommonService commonService;

    @Autowired
    private KeyCryptoService keyCryptoService;
    @Autowired
    private ICoinsAccountBalanceRecordService coinsAccountBalanceRecordService;

    public void run(Date recordDate) {
        for (ExchangeType exchangeType : ExchangeType.values()) {
            String exchangeTypeName = exchangeType.getName();
            QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
            LambdaQueryWrapper<CoinsApi> eq = queryWrapper.lambda()
                    .eq(CoinsApi::getExchangeName, exchangeTypeName);
            List<CoinsApi> coinsApis = coinsApiService.getBaseMapper().selectList(eq);
            for (CoinsApi coinsApi : coinsApis) {
                //解密
                keyCryptoService.decryptApi(coinsApi);

                QueryBalanceBody body = new QueryBalanceBody();
                body.setExchange(exchangeTypeName);
                body.setCoin("USDT");
                // 新增重试逻辑
                R<AccountBalance> r = null;
                boolean success = false;
                int retryCount = 0;
                while (!success && retryCount < 3) {
                    try {
                        r = commonService.queryBalanceByEx(coinsApi, body);
                        AccountBalance balance = r.getData();
                        if (balance != null && balance.getBalance() != null) {
                            success = true;
                        } else {
                            log.warn("查询余额失败，balance 为 null 或 balance.getBalance() 为 null，正在重试...");
                            Thread.sleep(5000); // 隔 5 秒重试
                        }
                    } catch (Exception e) {
                        log.error("查询余额时发生异常，正在重试...", e);
                        try {
                            Thread.sleep(5000); // 隔 5 秒重试
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("线程中断异常", ie);
                        }
                    }
                    retryCount++;
                }
                if (!success) {
                    log.error("查询余额失败，已达到最大重试次数");
                    continue; // 跳过当前循环，继续处理下一个 coinsApi
                }
                CoinsAccountBalanceRecordBo bo = getCoinsAccountBalanceRecordBo(recordDate, coinsApi, r, exchangeTypeName);
                try {
                    coinsAccountBalanceRecordService.saveNewBalanceRecord(bo);
                } catch (Exception e) {
                    log.error("保存余额记录时发生异常,记录日期：{},userId：{},交易所：{},异常原因：{}",
                            DateUtil.format(recordDate, "yyyy-MM-dd"),
                            coinsApi.getUserId(), exchangeTypeName, e.getMessage());
                    log.info("----------跳过当前记录----------");
                }
            }
        }
    }


    @NotNull
    private static CoinsAccountBalanceRecordBo getCoinsAccountBalanceRecordBo(Date recordDate, CoinsApi coinsApi,
                                                                              R<AccountBalance> r, String exchangeTypeName) {
        AccountBalance accountBalance = r.getData();
        CoinsAccountBalanceRecordBo bo = new CoinsAccountBalanceRecordBo();
        bo.setExchange(exchangeTypeName);
        bo.setUserId(coinsApi.getUserId());
        bo.setBalance(accountBalance.getBalance());
        bo.setUsdtBalance(accountBalance.getUsdtBalance());
        bo.setFreeBalance(accountBalance.getFreeBalance());
        bo.setCashBalance(accountBalance.getCashBalance());
        Date now = new Date();
        bo.setRecordDate(recordDate);
        bo.setRecordTime(now);
        bo.setCreateTime(now);
        return bo;
    }




    /**
     *
     * @param jobArgs
     * @return
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        Date recordDate;
        JSONObject jobParams = JSONObject.parseObject((String) jobArgs.getJobParams());
        if (jobParams != null && jobParams.containsKey("recordDate")) {
            Object recordDateObj = jobParams.get("recordDate");
            String recordDateStr = "";
            if (recordDateObj != null && StringUtils.isNotEmpty(recordDateObj.toString())) {
                recordDateStr = recordDateObj.toString();
            }
            try {
                recordDate = DateUtil.parseDate(String.valueOf(recordDateStr));
            } catch (Exception e) {
                return ExecuteResult.failure("参数 recordDate 值格式错误，请输入 yyyy-MM-dd 格式！");
            }

        } else {
            recordDate = new Date();
        }
        SnailJobLog.LOCAL.info("开始记录用户余额任务");
        long startTimeStamp = System.currentTimeMillis();
        this.run(recordDate);
        SnailJobLog.LOCAL.info("结束记录用户余额任务 耗时 {} ms",System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("记录用户余额任务成功");
    }
}
