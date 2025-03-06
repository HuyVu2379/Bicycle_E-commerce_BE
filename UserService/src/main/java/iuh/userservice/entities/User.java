package iuh.userservice.entities;

import iuh.userservice.enums.Gender;
import iuh.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@EqualsAndHashCode(of = {"userId", "email", "phoneNumber"})
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
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
    private String addressId;
    @Column(nullable = true)
    private String avatar;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public User(String password, String email, Role role) {
        this.password = password;
        this.email = email;
        this.role = role;
    }
}
