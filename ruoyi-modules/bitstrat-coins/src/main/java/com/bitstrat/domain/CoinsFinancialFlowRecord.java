package com.bitstrat.domain;

import com.bitstrat.domain.bo.CoinsFinancialFlowRecordBo;
import com.bitstrat.domain.vo.CoinsFinancialFlowRecordVo;
import lombok.Builder;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.BeanUtils;

import java.io.Serial;

/**
 * 交易所资金流水记录对象 coins_financial_flow_record
 *
 * @author Lion Li
 * @date 2025-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_financial_flow_record")
public class CoinsFinancialFlowRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 交易所原始流水ID
     */
    private String exchangeRecordId;

    /**
     * 交易所名称
     */
    private String exchange;

    /**
     * 资金流水类型
     */
    private String flowType;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 流水发生时间
     */
    private LocalDateTime timestamp;

    /**
     * 金额(正数为收入/负数为支出)
     */
    private BigDecimal amount;

    /**
     * 资产类型
     */
    private String asset;


    public CoinsFinancialFlowRecordBo toBo() {
        CoinsFinancialFlowRecordBo coinsFinancialFlowRecordBo = new CoinsFinancialFlowRecordBo();
        BeanUtils.copyProperties(this, coinsFinancialFlowRecordBo);


        return coinsFinancialFlowRecordBo;
    }

    CoinsFinancialFlowRecordVo toVo() {
        CoinsFinancialFlowRecordVo coinsFinancialFlowRecordVo = new CoinsFinancialFlowRecordVo();
        coinsFinancialFlowRecordVo.setId(this.id);
        coinsFinancialFlowRecordVo.setExchangeRecordId(this.exchangeRecordId);
        coinsFinancialFlowRecordVo.setExchange(this.exchange);
        coinsFinancialFlowRecordVo.setFlowType(this.flowType);
        coinsFinancialFlowRecordVo.setSymbol(this.symbol);
        coinsFinancialFlowRecordVo.setTimestamp(this.timestamp);
        coinsFinancialFlowRecordVo.setAmount(this.amount);
        coinsFinancialFlowRecordVo.setAsset(this.asset);

        return coinsFinancialFlowRecordVo;
    }
}
