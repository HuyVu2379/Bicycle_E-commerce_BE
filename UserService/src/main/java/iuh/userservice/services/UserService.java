package iuh.userservice.services;

import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.requests.UpdateAvatarRequest;
import iuh.userservice.entities.User;
import iuh.userservice.enums.Gender;
import iuh.userservice.enums.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface UserService {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserById(String id);
    Optional<User> registerUser(RegisterRequest registerRequest);
    Optional<User> registerUserForGoogle(RegisterRequest registerRequest);
    Optional<User> updateUser(User user);
    Boolean existsByEmail(String email);
    int existsByPhoneNumber(String phoneNumber);
    boolean updateAvatar(UpdateAvatarRequest updateAvatarRequest);
}
