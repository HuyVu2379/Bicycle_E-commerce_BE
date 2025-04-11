package iuh.paymentservice.services;

import iuh.paymentservice.entities.Payment;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PaymentService {
    Optional<Payment> createPayment();
}
