package com.bitstrat.domain.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/6 10:51
 * @Content
 */

@Data
public class WebsocketStatus {
    private String exchange;
    private String wsType;
    private String status;
    private Long dely;
    private String apiName;
    private Long apiId;
    private BigDecimal balance;
    private BigDecimal freeBalance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 节点名称
     */
    private String nodeName;

}
