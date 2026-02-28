package com.bitstrat.domain;

import lombok.Data;

@Data
public class MergedExchangeData {
    private String exchangeName;
    private String nodeName;
    private String clientId;
    private Long delay;
    private String ip;
    private String status;

    public MergedExchangeData(String exchangeName, String nodeName, String clientId, Long delay,String ip,String status) {
        this.exchangeName = exchangeName;
        this.nodeName = nodeName;
        this.clientId = clientId;
        this.delay = delay;
        this.ip = ip;
        this.status = status;
    }

    // toString 方法方便输出
    @Override
    public String toString() {
        return "MergedExchangeData{" +
            "exchangeName='" + exchangeName + '\'' +
            ", nodeName='" + nodeName + '\'' +
            ", clientId='" + clientId + '\'' +
            ", delay=" + delay +
            '}';
    }

    // getters 和 setters 根据需要添加
}
