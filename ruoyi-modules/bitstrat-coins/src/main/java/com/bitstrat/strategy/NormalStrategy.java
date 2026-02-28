package com.bitstrat.strategy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bitstrat.domain.CoinsTask;
import com.bitstrat.domain.vo.CoinsTaskVo;
import org.dromara.common.log.event.OperLogEvent;

import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 10:53
 * @Content
 */

public interface NormalStrategy {

    public String typeName();
    public Integer typeId();

    /**
     * 执行此任务
     * @param task
     */
    public void run(CoinsTaskVo task, OperLogEvent operLog);

    public Logger getLogger();

    public default String fethAllLog( ListAppender<ILoggingEvent> listAppender){
        if (!listAppender.list.isEmpty()){
            String logs = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
            return logs;
        }else{
            return "";
        }

    }
}
