package com.bitstrat.domain;

import com.bitstrat.domain.vo.WebsocketStatus;
import lombok.Data;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/21 18:13
 * @Content
 */

@Data
public class WebsocketExStatus {
    private String exchangeName;
    List<WebsocketStatus> datas;
}
