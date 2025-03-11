package iuh.productservice.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document
@Data
@EqualsAndHashCode(of = {"supplierId", "name","phone","email"})
public class Supplier {
    @Id
    private String supplierId;
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;
    private String addressId;
    private String phone;
    private String email;
    private String description;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
