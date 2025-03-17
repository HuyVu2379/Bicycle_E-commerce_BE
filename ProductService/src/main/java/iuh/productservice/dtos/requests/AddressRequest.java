package iuh.productservice.dtos.requests;

import lombok.Data;

@Data
public class AddressRequest {
    private String addressId;
    private String city;
    private String district;
    private String street;
    private String ward;
    private String country;
    private String userId;
}
