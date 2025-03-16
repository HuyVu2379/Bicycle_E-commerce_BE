package iuh.productservice.dtos.requests;

import iuh.productservice.dtos.AddressDTO;
import lombok.Data;

@Data
public class SupplierRequest {
    private String supplierId;
    private String name;
    private String phone;
    private String email;
    private String description;
    private AddressDTO address;
}
