// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BitgetPositionDetail {
    private String symbol;
    private BigDecimal leverage;
    private BigDecimal keepMarginRate;
    private String breakEvenPrice;
    private BigDecimal available; // 仓位可用(基础币)
    private BigDecimal liquidationPrice; // 预估强平价 小于等于0，代表永不爆仓
    private String posMode;
    private Long cTime;
    /**
     * 	保证金模式
     * crossed:全仓
     * isolated: 逐仓
     */
    private String marginMode;
    private String assetMode;
    private BigDecimal total; //仓位总数量(available + locked)
    private String markPrice;
    private String autoMargin;
    private BigDecimal openPriceAvg;//平均开仓价
    private BigDecimal locked;//仓位冻结(基础币)
    private BigDecimal marginRatio;//维持保证金率（0.1代表10%）
    private String marginCoin;
    private BigDecimal deductedFee; // 已扣手续费，仓位存续期间扣除的交易手续费
    private BigDecimal achievedProfits; // 	已实现盈亏（不包含手续费和资金费用）
    private BigDecimal marginSize; // 保证金数量 (保证金币种)
    private BigDecimal unrealizedPL; //未实现盈亏
    private BigDecimal totalFee;//资金费用，仓位存续期间，资金费用的累加值,初始值为空，表示还没收取过资金费
    private String holdSide;
    private Long uTime;
    private String openDelegateSize;

    //止盈价格
    private BigDecimal takeProfit;

    //止损价格
    private BigDecimal stopLoss;
}
