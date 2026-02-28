// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class HistoryPosition {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    // 计算收益率 (考虑手续费和资金费用)
    public BigDecimal getPnlRate() {
        if (closeTotalPos == null || closeTotalPos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 避免除零
        }

        BigDecimal cost = openAvgPrice.multiply(closeTotalPos); // 成本基数
//        BigDecimal profit = pnl.add(openFee).add(closeFee).add(totalFunding);
        return netProfit.divide(cost, 6, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    // 计算持仓时长（小时）
    public double getDurationHours() {
        return (Long.valueOf(this.utime) - Long.valueOf(this.getCtime())) / (1000.0 * 60 * 60);
    }

    public String getCtimeFormat() {

        return simpleDateFormat.format(new Date(Long.valueOf(this.ctime)));
    }
    public String getUtimeFormat() {

        return simpleDateFormat.format(new Date(Long.valueOf(this.utime)));
    }

}
