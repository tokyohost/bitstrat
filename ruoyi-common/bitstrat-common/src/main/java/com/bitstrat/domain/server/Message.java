package com.bitstrat.domain.server;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 14:49
 * @Content
 */

public class Message {

    Integer type;
    MessageData data;
    String auth;
    String exchangeName;

    Long timestamp;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public MessageData getData() {
        return data;
    }

    public void setData(MessageData data) {
        this.data = data;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
