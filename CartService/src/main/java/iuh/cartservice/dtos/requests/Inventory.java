package iuh.cartservice.dtos.requests;

import iuh.cartservice.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
        private String inventoryId;
        private String productId;
        private LocalDateTime importDate;
        private Color color;
        private List<String> imageUrls;
        private int quantity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

}
