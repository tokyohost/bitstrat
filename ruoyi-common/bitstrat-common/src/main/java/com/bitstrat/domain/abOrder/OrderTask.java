package com.bitstrat.domain.abOrder;

import com.bitstrat.domain.Account;
import lombok.Data;

@Data
public class OrderTask {
    private Long userId;
    private String typeB;
    private String exchangeA;
    private String exchangeB;
    private String taskId;
    private String typeA;
    private String symbolA;
    private String symbolTmpA;
    private String symbolTmpB;
    private String symbolB;
    private volatile Long delyA = 0L;
    private volatile Long delyB = 0L;
    private Account accountA;
    private Account accountB;

}
