package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.MessageData;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 11:07
 * @Content
 */

public class ActiveLossPointData implements MessageData {
    List<ActiveLossPoint> activeLossPoints;
    boolean clearAll = false;
    boolean delete = false;


    public boolean isClearAll() {
        return clearAll;
    }

    public void setClearAll(boolean clearAll) {
        this.clearAll = clearAll;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public List<ActiveLossPoint> getActiveLossPoints() {
        return activeLossPoints;
    }

    public void setActiveLossPoints(List<ActiveLossPoint> activeLossPoints) {
        this.activeLossPoints = activeLossPoints;
    }
}
