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
    private String district;
    private String city;
    private String street;
    private String ward;
    private String country;
    @Column(name = "userId",nullable = false)
    private String userId;
}
