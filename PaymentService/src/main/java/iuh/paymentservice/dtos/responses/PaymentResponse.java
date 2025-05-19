package iuh.paymentservice.dtos.responses;

import iuh.paymentservice.enums.Currency;
import iuh.paymentservice.enums.PaymentMethod;
import iuh.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String userId;
    private double amount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}