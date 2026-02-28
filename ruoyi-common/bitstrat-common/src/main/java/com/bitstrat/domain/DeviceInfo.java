package com.bitstrat.domain;

import com.bitstrat.constant.ConnectionStatus;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 14:43
 * @Content
 */

@Data
public class DeviceInfo {

    private String clientId;

    private String ip;

    private String mac;

    private String exchangeName;

    private ConnectionStatus status;

    private Long delay;
    private Long nodeToServerDelay;

    private Long lastConnectTime;

    private Long maxRoleSize;
    private Long currRoleSize;

}
