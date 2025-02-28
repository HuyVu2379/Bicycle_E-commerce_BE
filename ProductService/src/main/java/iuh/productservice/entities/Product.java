package iuh.productservice.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document
@Data
@EqualsAndHashCode(of = {"productId"})
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
    private String description;
    @DecimalMin(value = "0.0", message = "Giá phải lớn hơn hoặc bằng 0")
    private double price;
    private String promotionId;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
