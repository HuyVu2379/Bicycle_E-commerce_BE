package iuh.orderservice.entities;

import iuh.orderservice.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"promotionId"})
public class Promotion extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private String promotionId;
    private String name;
    private int reducePercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int maxLimit;
    private boolean isActive;
    @Enumerated(EnumType.STRING)
    private DiscountType applyFor;
}
