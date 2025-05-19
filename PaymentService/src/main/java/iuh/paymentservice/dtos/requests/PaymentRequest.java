package iuh.paymentservice.dtos.requests;

import iuh.paymentservice.enums.Currency;
import iuh.paymentservice.enums.PaymentMethod;
import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private String userId;
    private double amount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private String orderInfo;
    private String ipAddress;
    private String language;
    private String bankCode;
}