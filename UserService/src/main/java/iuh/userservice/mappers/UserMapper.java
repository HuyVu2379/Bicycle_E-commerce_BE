package iuh.userservice.mappers;

import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public User AuthRequestToUser(AuthRequest authRequest);

    public AuthRequest RegisterRequestToAuthRequest(RegisterRequest registerRequest);
    public AuthResponse RegisterRequestToAuthResponse(RegisterRequest registerRequest);
    public AuthResponse UserToAuthResponse(User user);

}
