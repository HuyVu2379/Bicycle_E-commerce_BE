package iuh.productservice.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document
@Data
@EqualsAndHashCode(of = {"inventoryId"})
public class Inventory {
    @Id
    private String inventoryId;
    @NotNull(message = "Product ID không được để trống")
    private String productId;
    private LocalDateTime importDate;
    private int quantity;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
