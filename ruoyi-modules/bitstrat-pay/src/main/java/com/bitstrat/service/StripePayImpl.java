package com.bitstrat.service;

import com.bitstrat.constant.PayTypeEnum;
import com.bitstrat.domain.bo.QrPayCallBack;
import com.bitstrat.domain.bo.QrPayParams;
import com.bitstrat.domain.bo.QrPayResponse;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.dromara.common.core.validate.StripePayGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/14 15:21
 * @Content
 */

@Service
public class StripePayImpl  implements IPayService{

    @Value("${stripe.domain}")
    private String domain;
    private String secretKey;

    public StripePayImpl(@Value("${stripe.secret-key}")String secretKey) {
        this.secretKey = secretKey;
        // This is your test secret API key.
        Stripe.apiKey = secretKey;
    }

    @Override
    public QrPayResponse createAliPayQrPay(QrPayParams params) {
        return null;
    }

    @SneakyThrows
    @Override
    public QrPayResponse redictPay(HttpServletResponse response, @Validated(StripePayGroup.class) QrPayParams payParams) {
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(domain + "/balance/index")
                .setAutomaticTax(
                    SessionCreateParams.AutomaticTax.builder()
                        .setEnabled(true)
                        .build())
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        // Provide the exact Price ID (for example, price_1234) of the product you want to sell
                        .setPrice(payParams.getPriceId())
                        .build())
                .setClientReferenceId(payParams.getCoinsBalanceLogBo().getId()+"")
                .build();
        Session session = Session.create(params);
        QrPayResponse qrPayResponse = new QrPayResponse();
        qrPayResponse.setRedirectUrl(session.getUrl());
        return qrPayResponse;
    }

    @Override
    public boolean checkAlipayF2fSign(Map<String, String> params, String rsa2) {
        return false;
    }

    @Override
    public QrPayCallBack queryAliF2fPayStatus(String outTradeNo) {
        return null;
    }

    @Override
    public String getPayType() {
        return PayTypeEnum.STRIPE.getType();
    }
}
