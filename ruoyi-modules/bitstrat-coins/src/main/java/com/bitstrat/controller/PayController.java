package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.http.ContentType;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.CoinsBalanceStatus;
import com.bitstrat.constant.CoinsBalanceType;
import com.bitstrat.constant.LockConstant;
import com.bitstrat.constant.PayTypeEnum;
import com.bitstrat.domain.bo.*;
import com.bitstrat.domain.vo.CoinsBalanceLogVo;
import com.bitstrat.service.ICoinsBalanceLogService;
import com.bitstrat.service.IPayService;
import com.bitstrat.service.PayManager;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AliPayGroup;
import org.dromara.common.core.validate.StripePayGroup;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.service.ISysDictDataService;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/3 19:49
 * @Content
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Autowired
    PayManager payManager;

    @Autowired
    ICoinsBalanceLogService coinsBalanceLogService;

    private final RedissonClient redissonClient;

    private final ISysDictDataService sysDictDataService;
    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostMapping("/payByQrCode")
//    @ApiEncrypt
    @SaCheckLogin
    public R<QrPayResponse> payByQrCode(@RequestBody @Validated(AliPayGroup.class) QrPayParams qrPayParams) {
        IPayService payService = payManager.getPayService(qrPayParams.getPayType());
        if (Objects.isNull(payService)) {
            return R.fail("unsupported pay type");
        }
        CoinsBalanceLogBo coinsBalanceLogBo = new CoinsBalanceLogBo();
        coinsBalanceLogBo.setUserId(LoginHelper.getUserId());
        coinsBalanceLogBo.setType(CoinsBalanceType.RE_CHARGE.getStatus());
        coinsBalanceLogBo.setStatus(CoinsBalanceStatus.PROCESS.getStatus());
        coinsBalanceLogBo.setChangeAmount(qrPayParams.getPayAmount());
        coinsBalanceLogBo.setBeforeBalance(null);
        coinsBalanceLogBo.setAfterBalance(null);

        coinsBalanceLogService.insertByBo(coinsBalanceLogBo);
        coinsBalanceLogBo.setRemark("支付宝充值订单号:" + coinsBalanceLogBo.getId());
        coinsBalanceLogService.updateByBo(coinsBalanceLogBo);
        qrPayParams.setCoinsBalanceLogBo(coinsBalanceLogBo);
        QrPayResponse aliPayQrPay = payService.createAliPayQrPay(qrPayParams);
        aliPayQrPay.setOutTradeNo(coinsBalanceLogBo.getId());
        return R.ok("ok", aliPayQrPay);
    }
    @GetMapping("/payByRedict")
    public R<QrPayResponse> payByRedict(@Validated(StripePayGroup.class) QrPayParams qrPayParams, HttpServletResponse response) throws IOException, StripeException {
        Price price = Price.retrieve(qrPayParams.getPriceId());
        if(Objects.isNull(price)){
            return R.fail("price not found");
        }
        BigDecimal unitAmountDecimal = price.getUnitAmountDecimal();
        String currency = price.getCurrency();
        BigDecimal amount = unitAmountDecimal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        qrPayParams.setPayAmount(amount);

        IPayService payService = payManager.getPayService(qrPayParams.getPayType());
        if (Objects.isNull(payService)) {
            return R.fail("unsupported pay type");
        }else{
            CoinsBalanceLogBo coinsBalanceLogBo = new CoinsBalanceLogBo();
            coinsBalanceLogBo.setUserId(LoginHelper.getUserId());
            coinsBalanceLogBo.setType(CoinsBalanceType.RE_CHARGE.getStatus());
            coinsBalanceLogBo.setStatus(CoinsBalanceStatus.PROCESS.getStatus());
            coinsBalanceLogBo.setChangeAmount(qrPayParams.getPayAmount());
            coinsBalanceLogBo.setBeforeBalance(null);
            coinsBalanceLogBo.setAfterBalance(null);

            coinsBalanceLogService.insertByBo(coinsBalanceLogBo);
            coinsBalanceLogBo.setRemark("Stripe:" + coinsBalanceLogBo.getId());
            coinsBalanceLogService.updateByBo(coinsBalanceLogBo);
            qrPayParams.setCoinsBalanceLogBo(coinsBalanceLogBo);
            return R.ok("ok", payService.redictPay(response, qrPayParams));
        }
    }

    @PostMapping("/stripe/webhook")
    @SaIgnore
    public String handleWebhook(HttpServletRequest request) throws Exception {

        String payload;
        try (InputStream inputStream = request.getInputStream()) {
            payload = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        log.info("收到stripe 支付回调：{}", payload);
        Event event;

        try {
            event = Webhook.constructEvent(
                payload,
                sigHeader,
                stripeWebhookSecret   // Stripe 后台生成的 webhook secret
            );
        } catch (SignatureVerificationException e) {
            return "invalid signature";
        }
        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        String  rawJson = dataObjectDeserializer.getRawJson();
        Session paymentIntent = ApiResource.GSON.fromJson(rawJson, Session.class);
        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
            case "checkout.session.completed":
            case "checkout.session.async_payment_succeeded":
                String success = stripeSuccess( paymentIntent, rawJson, event);
                if (success != null) return success;
                break;
            case "payment_method.attached":
                PaymentMethod paymentMethod = ApiResource.GSON.fromJson(rawJson, PaymentMethod.class);
                // Then define and call a method to handle the successful attachment of a PaymentMethod.
                // handlePaymentMethodAttached(paymentMethod);
                log.info("payment_method.attached {}",paymentMethod);
                break;
            case "payment_intent.payment_failed":
            case "checkout.session.async_payment_failed":
                String failed = stripePayFailed(paymentIntent, rawJson, event);
                if (failed != null) return failed;
                break;
            case "payment_intent.canceled":
            case "checkout.session.expired":
                String failed2 = stripePayCanceld(paymentIntent, rawJson, event);
                if (failed2 != null) return failed2;
                break;
            default:
                log.warn("Unhandled event type: " + event.getType());
                break;
        }

        return "success";
    }

    private String stripePayCanceld(Session stripeObject, String rawJson, Event event) {
        // Then define and call a method to handle the successful attachment of a PaymentMethod.
        // handlePaymentMethodAttached(paymentMethod);
        String sessionId = stripeObject.getId();
        String paymentStatus = stripeObject.getPaymentStatus();
        String clientReferenceId = stripeObject.getClientReferenceId();
        log.info("stripe 付款失败：sessionId={}, paymentStatus={}, clientReferenceId={} type:{}", sessionId, paymentStatus, clientReferenceId, event.getType());

        CoinsBalanceLogVo coinsBalanceLogVo = coinsBalanceLogService.queryById(Long.valueOf(clientReferenceId));
        // TODO: 更新订单状态（一定要幂等）
        // orderService.markPaid(sessionId);
        RLock lock = redissonClient.getLock(LockConstant.BALANCE_UPDATE_LOCK + coinsBalanceLogVo.getId());
        lock.lock();
        try {
            if (coinsBalanceLogVo.getStatus().equals(CoinsBalanceStatus.SUCCESS.getStatus())){
                return "success";
            }
            coinsBalanceLogService.updateOrderCancel(String.valueOf(coinsBalanceLogVo.getId()),paymentStatus, event.getType());
        } finally {
            lock.unlock();
        }
        return "success";
    }

    @Nullable
    private String stripePayFailed(Session stripeObject, String rawJson, Event event) {
        // Then define and call a method to handle the successful attachment of a PaymentMethod.
        // handlePaymentMethodAttached(paymentMethod);
        String sessionId = stripeObject.getId();
        String paymentStatus = stripeObject.getPaymentStatus();
        String clientReferenceId = stripeObject.getClientReferenceId();
        log.info("stripe 付款失败：sessionId={}, paymentStatus={}, clientReferenceId={} type:{}", sessionId, paymentStatus, clientReferenceId, event.getType());

        CoinsBalanceLogVo coinsBalanceLogVo = coinsBalanceLogService.queryById(Long.valueOf(clientReferenceId));
        // TODO: 更新订单状态（一定要幂等）
        // orderService.markPaid(sessionId);
        RLock lock = redissonClient.getLock(LockConstant.BALANCE_UPDATE_LOCK + coinsBalanceLogVo.getId());
        lock.lock();
        try {
            if(coinsBalanceLogVo.getStatus().equals(CoinsBalanceStatus.SUCCESS.getStatus())){
                return "success";
            }
            coinsBalanceLogService.updateOrderFail(String.valueOf(coinsBalanceLogVo.getId()),paymentStatus, event.getType());
        } finally {
            lock.unlock();
        }
        return "success";
    }

    @Nullable
    private String stripeSuccess(Session stripeObject, String rawJson, Event event) {
        String sessionId = stripeObject.getId();
        String paymentStatus = stripeObject.getPaymentStatus();
        String clientReferenceId = stripeObject.getClientReferenceId();
        log.info("stripe 付款成功：sessionId={}, paymentStatus={}, clientReferenceId={} type:{}", sessionId, paymentStatus, clientReferenceId, event.getType());
        CoinsBalanceLogVo coinsBalanceLogVo = coinsBalanceLogService.queryById(Long.valueOf(clientReferenceId));
        // TODO: 更新订单状态（一定要幂等）
        // orderService.markPaid(sessionId);
        RLock lock = redissonClient.getLock(LockConstant.BALANCE_UPDATE_LOCK + coinsBalanceLogVo.getId());
        lock.lock();
        try {
            if(coinsBalanceLogVo.getStatus().equals(CoinsBalanceStatus.SUCCESS.getStatus())){
                return "success";
            }
            coinsBalanceLogService.updateOrderPaid(String.valueOf(coinsBalanceLogVo.getId()),paymentStatus, event.getType());
        } finally {
            lock.unlock();
        }
        return "success";
    }


    @PostMapping("/f2fCallback")
    @SaIgnore
    public String alipayCallback(HttpServletRequest request) {
        log.info("【支付宝回调】收到请求");
        IPayService payService = payManager.getPayService(PayTypeEnum.ALIPAY.getType());

        // 1. 获取所有参数（支付宝是 form-data 形式）
        Map<String, String> params = convertParams(request);

        log.info("【支付宝回调参数】：{}", params);

        try {
            // 2. 验证签名（RSA2）
            boolean signVerified = payService.checkAlipayF2fSign(params, "RSA2");


            if (!signVerified) {
                log.error("【支付宝回调】签名验证失败");
                return "fail";
            }

            // 3. 解析参数
            String outTradeNo = params.get("out_trade_no");   // 你系统的订单号
            String tradeNo = params.get("trade_no");          // 支付宝订单号
            String tradeStatus = params.get("trade_status");  // 支付状态

            // 4. 支付状态判断
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                RLock lock = redissonClient.getLock(LockConstant.BALANCE_UPDATE_LOCK + outTradeNo);
                lock.lock();
                try {
                    coinsBalanceLogService.updateOrderPaid(outTradeNo, tradeNo, tradeStatus);
                } finally {
                    lock.unlock();
                }

                log.info("【支付宝回调】订单支付成功：outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);

                return "success";
            }

            log.warn("【支付宝回调】收到但未成功的状态：{}", tradeStatus);

        } catch (Exception e) {
            log.error("【支付宝回调】异常：", e);
            return "fail";
        }

        return "success";
    }

    /**
     * 支付宝当面付轮询
     * @param qrPayQueryParams
     * @return
     */
    @PostMapping("/checkPayStatus")
    @SaCheckLogin
    public R<?> checkPayStatus(@RequestBody @Validated QrPayQueryParams qrPayQueryParams) {
        Long userId = LoginHelper.getUserId();
        CoinsBalanceLogVo coinsBalanceLogVo = coinsBalanceLogService.queryById(qrPayQueryParams.getOutTradeNo());
        if (coinsBalanceLogVo == null || coinsBalanceLogVo.getUserId() != userId) {
            return R.fail("unknown order or not your order");
        }
        IPayService payService = payManager.getPayService(PayTypeEnum.ALIPAY.getType());
        if (Objects.isNull(payService)) {
            return R.fail("unsupported pay type");
        }
        if(coinsBalanceLogVo.getStatus().equals(CoinsBalanceStatus.SUCCESS.getStatus())){
            return R.ok();
        }
        QrPayCallBack qrPayCallBack = payService.queryAliF2fPayStatus(coinsBalanceLogVo.getId() + "");
        if (qrPayCallBack.getIsSuccess()) {
            RLock lock = redissonClient.getLock(LockConstant.BALANCE_UPDATE_LOCK + coinsBalanceLogVo.getId());
            lock.lock();
            try {
                if(coinsBalanceLogVo.getStatus().equals(CoinsBalanceStatus.SUCCESS.getStatus())){
                    return R.ok();
                }
                coinsBalanceLogService.updateOrderPaid(String.valueOf(coinsBalanceLogVo.getId()), qrPayCallBack.getTradeNo(), qrPayCallBack.getTradeStatus());
            } finally {
                lock.unlock();
            }

            return R.ok(qrPayCallBack);
        } else {
            return R.ok(qrPayCallBack);
        }
    }

    /**
     * 将 request 中 form-data 转为 Map<String, String>
     */
    private Map<String, String> convertParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }
        return params;
    }
}
