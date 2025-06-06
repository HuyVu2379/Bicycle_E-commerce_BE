package iuh.orderservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    private String addressId;
    private String city;
    private String district;
    private String street;
    private String ward;
    private String country;
    private String userId;
}
