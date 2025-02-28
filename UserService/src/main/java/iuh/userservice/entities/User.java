package iuh.userservice.entities;

import iuh.userservice.enums.Gender;
import iuh.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"userId", "email", "phoneNumber"})
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false,unique = true)
    private String email;
    private LocalDateTime dob;
    @Column(nullable = false,unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String address;
    @Column(nullable = true)
    private String avatar;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;
}
