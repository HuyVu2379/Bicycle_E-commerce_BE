package iuh.paymentservice.services;

import iuh.paymentservice.dtos.requests.PaymentRequest;
import iuh.paymentservice.dtos.responses.PaymentResponse;
import iuh.paymentservice.dtos.responses.VNPayResponse;
import iuh.paymentservice.entities.Payment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest paymentRequest);
    VNPayResponse processVNPayCallback(Map<String, String> vnpParams);
    PaymentResponse getPaymentByOrderId(String orderId);
}
