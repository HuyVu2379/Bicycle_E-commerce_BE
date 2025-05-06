package iuh.productservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String addressId;
    private String fullAddress;
    private String city;
    private String district;
    private String street;
    private String ward;
    private String country;
}
