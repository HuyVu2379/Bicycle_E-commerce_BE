package iuh.paymentservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${payment.vnpay.tmnCode}")
    private String tmnCode;

    @Value("${payment.vnpay.secretKey}")
    private String hashSecret;

    @Value("${payment.vnpay.url}")
    private String paymentUrl;

    @Value("${payment.vnpay.returnUrl}")
    private String returnUrl;

    @Value("${payment.vnpay.orderType}")
    private String orderType;

    // Getters
    public String getTmnCode() {
        return tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getOrderType() {
        return orderType;
    }
}