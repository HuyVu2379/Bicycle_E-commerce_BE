package iuh.productservice.dtos.responses;

import iuh.productservice.entities.Category;
import iuh.productservice.entities.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private String productId;
    private String name;
    private Category category;
    private Supplier supplier;
    private String description;
    private double price;
    private double priceReduced = price;
    private String promotionId;
}
