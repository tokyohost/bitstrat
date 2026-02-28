package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Coin {
    private String availableToBorrow;
    private BigDecimal bonus;
    private String accruedInterest;
    private String availableToWithdraw;
    private BigDecimal totalOrderIM;
    private String equity;
    private String totalPositionMM;
    private String usdValue;
    private String unrealisedPnl;
    private Boolean collateralSwitch;
    private String spotHedgingQty;
    private String borrowAmount;
    private BigDecimal totalPositionIM;
    private BigDecimal walletBalance;
    private String cumRealisedPnl;
    private BigDecimal locked;
    private Boolean marginCollateral;
    private String coin;
}
