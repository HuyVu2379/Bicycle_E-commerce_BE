package iuh.productservice.dtos;

import lombok.Data;

@Data
public class AddressDTO {
    private String addressId;
    private String city;
    private String district;
    private String street;
    private String ward;
    private String country;
}
