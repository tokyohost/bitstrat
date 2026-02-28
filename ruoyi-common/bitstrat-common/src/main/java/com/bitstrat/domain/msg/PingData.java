package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.MessageData;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 11:07
 * @Content
 */

public class PingData implements MessageData {
    List<ActiveLossPoint> activeLossPoints;

    Long maxRoleSize;

    Long delay;

    public Long getMaxRoleSize() {
        return maxRoleSize;
    }

    public void setMaxRoleSize(Long maxRoleSize) {
        this.maxRoleSize = maxRoleSize;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public List<ActiveLossPoint> getActiveLossPoints() {
        return activeLossPoints;
    }

    public void setActiveLossPoints(List<ActiveLossPoint> activeLossPoints) {
        this.activeLossPoints = activeLossPoints;
    }
}
