package iuh.productservice.dtos.requests;

import iuh.productservice.dtos.responses.AddressResponse;
import lombok.Data;

@Data
public class SupplierRequest {
    private String supplierId;
    private String name;
    private String phone;
    private String email;
    private String description;
    private AddressRequest address;
}
