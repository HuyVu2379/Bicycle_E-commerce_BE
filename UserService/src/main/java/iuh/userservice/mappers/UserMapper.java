package iuh.userservice.mappers;

import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.dtos.responses.UserResponse;
import iuh.userservice.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User AuthRequestToUser(AuthRequest authRequest);

    AuthRequest RegisterRequestToAuthRequest(RegisterRequest registerRequest);
    AuthResponse RegisterRequestToAuthResponse(RegisterRequest registerRequest);
    AuthResponse UserToAuthResponse(User user);
    @Mapping(target = "address", ignore = true)
    UserResponse UserToUserResponse(User user);

}
