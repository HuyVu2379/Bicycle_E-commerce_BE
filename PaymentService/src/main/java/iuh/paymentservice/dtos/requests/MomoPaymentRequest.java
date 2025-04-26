/*
 * @ (#) CreateMomoRequest.java       1.0     4/25/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.paymentservice.dtos.requests;
/*
 * @author: Luong Tan Dat
 * @date: 4/25/2025
 */

import iuh.paymentservice.enums.Currency;
import iuh.paymentservice.enums.PaymentMethod;
import lombok.Getter;

@Getter
public class MomoPaymentRequest {
    private String orderId;
    private String userId;
    private Currency currency;
    private double amount;
    private PaymentMethod paymentMethod;
}
