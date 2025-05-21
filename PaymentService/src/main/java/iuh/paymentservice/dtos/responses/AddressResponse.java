package iuh.paymentservice.dtos.responses;

import lombok.*;


@Data
public class AddressResponse {
    private String addressId;
    private String fullAddress; // dia chi day du
    private String city; // thanh pho/tinh
    private String street; // duong
    private String ward;  // quan/xa
    private String district; // huyen
    private String country = "Viet Nam";
    private String userId;
}
