package iuh.productservice.dtos.responses;

import iuh.productservice.entities.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Product product;
    private List<Category> category;
    private List<Inventory> inventory;
    private Supplier supplier;
    private List<Specification> specification;
}
