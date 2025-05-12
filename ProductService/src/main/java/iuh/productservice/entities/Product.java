package iuh.productservice.entities;

import iuh.productservice.enums.Color;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@EqualsAndHashCode(of = {"productId"})
@NoArgsConstructor
public class Product {
    @Id
    private String productId;
    @Indexed
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    @NotNull(message = "Danh mục không được để trống")
    private String categoryId;
    @NotNull(message = "Nhà cung cấp không được để trống")
    private String supplierId;
    @NotNull(message = "Lô nhap không được để trống")
    private String inventoryId;
    private Color color;
    private String description;
    private List<String> imageUrls;
    @DecimalMin(value = "0.0", message = "Giá phải lớn hơn hoặc bằng 0")
    private double price;
    private double priceReduced = price;
    private String promotionId;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
