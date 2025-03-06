package iuh.userservice.dtos.responses;

import iuh.userservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class JWTResponse {
    private String token;
    private String type = "Bearer";
    private String userId;
    private String phoneNumber;
    private String email;
    private Role role;
}
