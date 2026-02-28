package com.bitstrat.domain.vo;

import lombok.Data;

@Data
public class AbTaskFrom {
    ArbitrageFormData buy;
    ArbitrageFormData sell;

    Long taskId;

//    操作类型，1-加仓，2-平仓'
    Integer side;

    /**
     * 批次任务id
     */
    Long batchId;
    boolean batchFlag = false;
    /**
     * 批次任务  -当前批次
     */
    Integer batchCount;

}
