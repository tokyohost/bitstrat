package org.dromara.web.listener;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2026/1/4 19:17
 * @Content
 */

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;

public class HostIPTurboFilter extends TurboFilter {

    private String hostIP;

    @Override
    public void start() {
        hostIP = System.getenv("HOST_IP");
        if (hostIP == null) hostIP = "UNKNOWN";
        super.start();
    }

    @Override
    public FilterReply decide(org.slf4j.Marker marker, ch.qos.logback.classic.Logger logger,
                              Level level, String format, Object[] params, Throwable t) {
        MDC.put("hostIP", hostIP);
        return FilterReply.NEUTRAL; // 继续正常日志处理
    }
}
