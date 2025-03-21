package iuh.productservice.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class PromotionResponse {
    private String promotionId;
    private String name;
    private int reducePercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int limitValue;
    private boolean isActive;
}
