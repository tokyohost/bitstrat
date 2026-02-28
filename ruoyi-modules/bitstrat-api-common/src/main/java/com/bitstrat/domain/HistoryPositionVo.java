// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AutoMapper(target = HistoryPosition.class)
public class HistoryPositionVo {
    private BigDecimal openAvgPrice;
    private String symbol;
    private String utime;
    private BigDecimal openFee;
    private String marginCoin;
    private String posMode;
    private String marginMode;
    private BigDecimal totalFunding;
    private BigDecimal closeFee;
    private BigDecimal netProfit;//包含手续费盈亏收益
    private BigDecimal pnl;
    private BigDecimal openTotalPos;
    private String positionId;
    private String holdSide;
    private String posSide;
    private String leverage;
    private String ctime;
    private BigDecimal closeTotalPos;
    private BigDecimal closeAvgPrice;
    private String idLessThan;


}
