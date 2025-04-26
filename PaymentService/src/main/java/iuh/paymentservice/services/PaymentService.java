package iuh.paymentservice.services;

import iuh.paymentservice.dtos.requests.MomoPaymentRequest;
import iuh.paymentservice.dtos.responses.MomoPaymentResponse;
import iuh.paymentservice.exception.MessageResponse;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    String processPayment(String userId, double amount, String currency);

    MomoPaymentResponse initiatePayment(MomoPaymentRequest request);
    String handleCallback(String orderId, String resultCode, String signature, String paymentTransactionId);
}
