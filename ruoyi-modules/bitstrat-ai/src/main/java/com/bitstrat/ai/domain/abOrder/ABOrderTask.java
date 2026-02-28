// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.ai.domain.abOrder;

import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.ai.distuptor.MarketABPriceEventHandler;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.abOrder.OrderTask;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class ABOrderTask extends OrderTask {
    private Long userId;
    /**
     * see {@link BusinessType#COMPARE_TYPE_SWAP}
     * see {@link BusinessType#COMPARE_TYPE_SPOT}
     */
    @NotNull(message = "B side type cannot be null")
    private String typeB;
    @NotNull(message = "A side exchange cannot be null")
    private String exchangeA;
    @NotNull(message = "B side exchange cannot be null")
    private String exchangeB;
    private String taskId;
    /**
     * see {@link BusinessType#COMPARE_TYPE_SWAP}
     * see {@link BusinessType#COMPARE_TYPE_SPOT}
     */
    private String typeA;
    @NotNull(message = "A side symbol cannot be null")
    private String symbolA;
    private String symbolTmpA;
    private String symbolTmpB;
    @NotNull(message = "B side symbol cannot be null")
    private String symbolB;
    private volatile Long delyA = 0L;
    private volatile Long delyB = 0L;
    private ABOperate operate;
    @NotNull(message = "A side account cannot be null")
    private Account accountA;
    @NotNull(message = "B side account cannot be null")
    private Account accountB;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serverTime;

    //杠杆倍数
    @NotNull(message = "leverage cannot be null")
    private BigDecimal leverage;

    private Long lastOrderTimeStamp;
    private Long lastSellTimeStamp;


    @JsonIgnore
    private MarketABPriceEventHandler marketABPriceEventHandler;
}
