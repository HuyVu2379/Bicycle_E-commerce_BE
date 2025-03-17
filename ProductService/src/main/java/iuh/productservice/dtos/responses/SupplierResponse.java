package iuh.productservice.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponse {
    private String supplierId;
    private String name;
    private String phone;
    private String email;
    private String description;
    private AddressResponse address;
}
