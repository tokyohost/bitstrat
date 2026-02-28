package com.bitstrat.domain;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SingleClosePosition {
    /**
     * 做多交易所
     */
    private String ex;

    /**
     * 做多币对
     */
    private String symbol;

    private String side;

    private Long accountId;


}
