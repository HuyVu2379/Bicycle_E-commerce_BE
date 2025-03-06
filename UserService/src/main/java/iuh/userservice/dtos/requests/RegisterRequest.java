package iuh.userservice.dtos.requests;

import iuh.userservice.enums.Gender;
import iuh.userservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    private String fullName;
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotBlank(message = "Address is required")
    private String addressId;
    private LocalDateTime dob;
    private String avatar;
    private Gender gender;
    private Role role;
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", dob=" + dob +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", addressId='" + addressId + '\'' +
                ", avatar='" + avatar + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", gender=" + gender +
                '}';
    }
}
