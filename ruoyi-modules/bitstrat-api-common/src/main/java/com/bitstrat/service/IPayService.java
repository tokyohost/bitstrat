package com.bitstrat.service;

import com.bitstrat.domain.bo.QrPayCallBack;
import com.bitstrat.domain.bo.QrPayParams;
import com.bitstrat.domain.bo.QrPayResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/3 19:29
 * @Content
 */

public interface IPayService {

    public QrPayResponse createAliPayQrPay(QrPayParams params);
    default QrPayResponse redictPay(HttpServletResponse response,QrPayParams params){
        return null;
    }

    boolean checkAlipayF2fSign(Map<String, String> params, String rsa2);

    public QrPayCallBack queryAliF2fPayStatus(String outTradeNo);

    public String getPayType();
}
