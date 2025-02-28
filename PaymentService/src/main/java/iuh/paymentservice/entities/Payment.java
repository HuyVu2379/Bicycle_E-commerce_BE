package iuh.paymentservice.entities;

import iuh.paymentservice.enums.Currency;
import iuh.paymentservice.enums.PaymentMethod;
import iuh.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(of = {"paymentId", "orderId"})
public class Payment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentId;
    @Column(nullable = false,unique = true)
    private String orderId;
    @Column(nullable = false)
    private String userId;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Column(nullable = false)
    private double amount;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private String transactionId;

}
