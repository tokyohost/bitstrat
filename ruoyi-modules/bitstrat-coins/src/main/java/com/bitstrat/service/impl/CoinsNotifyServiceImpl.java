package com.bitstrat.service.impl;

import com.bitstrat.domain.CoinsNotifyLog;
import com.bitstrat.domain.bo.CoinsNotifyLogBo;
import com.bitstrat.domain.vo.CoinsNotifyConfigVo;
import com.bitstrat.service.ICoinsNotifyConfigService;
import com.bitstrat.service.ICoinsNotifyLogService;
import com.bitstrat.service.ICoinsNotifyService;
import com.bitstrat.utils.DingTalkBotClient;
import com.bitstrat.utils.TelegramBotClient;
import org.dromara.common.core.utils.MapstructUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CoinsNotifyServiceImpl implements ICoinsNotifyService {


    @Value("${botNotify.tgBotBaseUrl}")
    private String tgBotBaseUrl;
    @Value("${botNotify.tgBotToken}")
    private String tgBotToken;
    @Value("${botNotify.dingTalkBotBaseUrl}")
    private String dingTalkBotBaseUrl;
    ExecutorService executorService = Executors.newWorkStealingPool(1);
    private final ICoinsNotifyConfigService notifyConfigService;
    private final ICoinsNotifyLogService notifyLogService;
    private final DingTalkBotClient dingTalkBotClient;
    private final TelegramBotClient telegramBotClient;

    public CoinsNotifyServiceImpl(ICoinsNotifyConfigService notifyConfigService,
                                  ICoinsNotifyLogService notifyLogService,
                                  DingTalkBotClient dingTalkBotClient,
                                  TelegramBotClient telegramBotClient) {
        this.notifyConfigService = notifyConfigService;
        this.notifyLogService = notifyLogService;
        this.dingTalkBotClient = dingTalkBotClient;
        this.telegramBotClient = telegramBotClient;

    }

    public void sendNotification(Long userId, String content) {
        // 获取用户的通知配置
        List<CoinsNotifyConfigVo> configs = notifyConfigService.queryConfigByUserId(userId);

        // 遍历配置，根据配置发送通知
        for (CoinsNotifyConfigVo config : configs) {
            String notifyType = config.getType();
            boolean success = false;
            String errorMessage = null;

            try {
                if ("1".equals(notifyType)) {
                    // 钉钉通知
                    success = dingTalkBotClient.sendText(dingTalkBotBaseUrl + config.getDingToken(),
                        config.getDingSecret(), content, null, false);
                } else if ("2".equals(notifyType)) {
                    // Telegram通知
                    success = telegramBotClient.sendMarkdown(tgBotBaseUrl, tgBotToken, config.getTelegramChatId(), content);
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
            }
            boolean finalSuccess = success;
            String finalErrorMessage = errorMessage;
            executorService.submit(()->{
                saveNotifyLog(userId, content, notifyType, finalSuccess, finalErrorMessage);
            });

        }
    }

    private void saveNotifyLog(Long userId, String content, String notifyType, boolean success, String errorMessage) {
        // 记录通知日志
        CoinsNotifyLog log = new CoinsNotifyLog();
        log.setUserId(userId);
        log.setNotifyType(notifyType);
        log.setNotifyContent(content);
        log.setNotifyStatus(success ? "1" : "2");
        log.setErrorMessage(errorMessage);
        CoinsNotifyLogBo coinsNotifyLogBo = new CoinsNotifyLogBo();
        BeanUtils.copyProperties(log,coinsNotifyLogBo);
        notifyLogService.insertByBo(coinsNotifyLogBo);
    }
}
