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
@EqualsAndHashCode(of = {"reviewId"})
public class Review {
    @Id
    private String reviewId;
    @NotNull(message = "User ID không được để trống")
    private String userId;
    @NotNull(message = "Product ID không được để trống")
    private String productId;
    private String content;
    private int rating;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
