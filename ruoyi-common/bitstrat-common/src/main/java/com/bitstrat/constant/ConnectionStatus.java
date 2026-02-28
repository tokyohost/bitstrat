package com.bitstrat.constant;

public enum ConnectionStatus {
    CONNECTED("ONLINE"),
    DISCONNECTED("OFFLINE");

    private final String connectionStatus;


    ConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }
}
