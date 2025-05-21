package iuh.userservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@EqualsAndHashCode(of = {"address_id","userId"})
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "address")
public class Address extends BaseEntity{
    @Id
    @Column(name = "address_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String addressId;
    private String fullAddress; // dia chi day du
    private String city; // thanh pho/tinh
    private String street; // duong
    private String ward;  // quan/xa
    private String district; // huyen
    private String country = "Viet Nam";
    @Column(name = "userId",nullable = false)
    private String userId;

    public Address(String fullAddress, String city, String street, String ward, String district, String country) {
        this.fullAddress = fullAddress;
        this.city = city;
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.country = country;
    }
}
