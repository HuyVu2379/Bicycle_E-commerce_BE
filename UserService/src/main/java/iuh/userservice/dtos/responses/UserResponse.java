package iuh.userservice.dtos.responses;

import iuh.userservice.entities.Address;
import iuh.userservice.enums.Gender;
import iuh.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String userId;
    private String email;
    private LocalDateTime dob;
    private String phoneNumber;
    private Address address;
    private String fullName;
    private String avatar;
    private String gender;

}
